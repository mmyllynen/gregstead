"""A fixed-version installer, not a launcher or a background auto-updater."""
import argparse
import json
from pathlib import Path
import queue
import sys
import threading
from updater import core
from updater.discovery import discover, identify


def bundle_path():
    return Path(getattr(sys,'_MEIPASS',Path(__file__).parent))/'update.zip'


def main():
    parser = argparse.ArgumentParser(description='Gregstead version-specific update installer')
    parser.add_argument('--bundle',type=Path,default=bundle_path())
    parser.add_argument('--check',type=Path,help='Read-only check of a profile')
    parser.add_argument('--report',type=Path,help='Write test result JSON here')
    parser.add_argument('--self-test',action='store_true',help='Exercise the packaged update on synthetic data')
    parser.add_argument('--gui-smoke',action='store_true',help='Create and close the GUI (test display required)')
    parser.add_argument('--recover',metavar='OPERATION')
    parser.add_argument('--profile',type=Path)
    args = parser.parse_args()
    if args.self_test:
        from updater.selftest import run
        if getattr(sys,'frozen',False) and not args.bundle.is_file():
            raise core.UpdateError('Embedded update bundle is missing')
        result = run(args.bundle if args.bundle.is_file() else None)
        if args.report:
            core.atomic(args.report,core.encoded(result))
        if sys.stdout is not None:
            print(json.dumps(result))
        return 0
    if args.recover:
        if not args.profile:
            parser.error('--recover requires --profile')
        print(json.dumps(core.recover(args.profile,args.recover)))
        return 0
    spec, payload = core.read_bundle(args.bundle)
    if args.check:
        result = core.inspect_profile(args.check,spec['target'])
        result.pop('hashes',None)
        print(json.dumps(result,indent=2))
        return 0
    smoke_tmp = None
    smoke_result = {'passed':False}
    if args.gui_smoke:
        import tempfile
        from updater.selftest import fixture
        smoke_tmp = tempfile.TemporaryDirectory(prefix='gregstead-gui-smoke-')
        smoke_profile = Path(smoke_tmp.name)/'Gregstead test profile'
        base,payload,spec = fixture()
        for name,data in base.items():core.atomic(smoke_profile/name,data)
        core.atomic(smoke_profile/'config/preferences.json',b'{"key":"my-key"}\n')
        core.atomic(smoke_profile/'saves/NeoGregTest/level.dat',b'protected world')
    import tkinter as tk
    from tkinter import ttk, filedialog, messagebox
    window = tk.Tk()
    window.title('Gregstead update ' + spec['target']['version'])
    window.geometry('720x440')
    window.minsize(640,400)
    frame = ttk.Frame(window,padding=20)
    frame.pack(fill='both',expand=True)
    ttk.Label(frame,text='Gregstead ' + spec['base']['version'] + ' → ' + spec['target']['version'],font=('',16,'bold')).pack(anchor='w')
    ttk.Label(frame,text='Close Minecraft. Your personal settings and worlds are preserved.').pack(anchor='w',pady=10)
    found = [{'path':str(smoke_profile)}] if args.gui_smoke else discover(spec['base'])
    selected = tk.StringVar(value=found[0]['path'] if len(found)==1 else '')
    ttk.Label(frame,text='CurseForge profile folder').pack(anchor='w')
    combo = ttk.Combobox(frame,textvariable=selected,values=[r['path'] for r in found])
    combo.pack(fill='x',pady=6)
    detail = tk.StringVar(value='Select the profile to update, then check the update.')
    ttk.Label(frame,textvariable=detail,wraplength=650).pack(anchor='w',pady=10)
    events = queue.Queue()
    busy = False
    ready = None
    prepared_payload = None
    def choose():
        folder = filedialog.askdirectory(title='Select your Gregstead profile')
        if folder:
            selected.set(folder)
    browse = ttk.Button(frame,text='Choose folder…',command=choose)
    browse.pack(anchor='w')
    def work(check_only):
        nonlocal busy,ready,prepared_payload
        folder = selected.get().strip()
        if not folder:
            messagebox.showerror('Select profile','Choose your CurseForge profile folder first.')
            return
        if not check_only and (ready != str(Path(folder).resolve())):
            messagebox.showerror('Check first','Check this profile before installing.')
            return
        if not identify(folder,spec['base']):
            messagebox.showerror('Unrecognized profile','This is not a recognized Gregstead profile. Install the full pack with CurseForge first.')
            return
        busy = True
        for widget in (check,install,browse,combo):
            widget.configure(state='disabled')
        def run():
            nonlocal prepared_payload
            try:
                if core.running_game(folder):
                    raise core.UpdateError('Close Minecraft before checking or installing this update.')
                complete = prepared_payload if not check_only and prepared_payload is not None else core.fetch_payload(spec,payload,lambda message:events.put(('progress',message)))
                if check_only:
                    plan = core.plan_update(folder,spec,complete)
                    prepared_payload = complete
                    events.put(('checked',(str(Path(folder).resolve()),len(plan['writes']),len(plan['remove']))))
                else:
                    receipt = core.apply_update(folder,spec,complete,lambda message:events.put(('progress',message)))
                    events.put(('installed',receipt))
            except Exception as error:
                events.put(('error',str(error)))
        threading.Thread(target=run,daemon=True).start()
    check = ttk.Button(frame,text='Check update',command=lambda:work(True))
    check.pack(anchor='w',pady=10)
    install = ttk.Button(frame,text='Install update',state='disabled',command=lambda:work(False))
    install.pack(anchor='w')
    def poll():
        nonlocal busy,ready
        try:
            while True:
                kind,data = events.get_nowait()
                if kind == 'progress':
                    detail.set(data)
                    continue
                busy = False
                combo.configure(state='normal');browse.configure(state='normal');check.configure(state='normal')
                if kind == 'checked':
                    ready,writes,removed = data
                    detail.set(f'Ready: {ready}\n{writes} files to install, {removed} old files to remove.\nTarget: Gregstead {spec["target"]["version"]}')
                    install.configure(state='normal')
                    if args.gui_smoke:window.after(1,install.invoke)
                elif kind == 'installed':
                    ready = None
                    install.configure(state='disabled')
                    detail.set('Installed and verified: Gregstead ' + data['version'] + '. You can now close this window and launch Minecraft.')
                    if args.gui_smoke:
                        smoke_result['passed'] = (core.inspect_profile(smoke_profile,spec['target'])['matches']
                            and (smoke_profile/'config/preferences.json').read_bytes()==b'{"key":"my-key"}\n'
                            and (smoke_profile/'saves/NeoGregTest/level.dat').read_bytes()==b'protected world')
                        window.destroy()
                        return
                else:
                    ready = None
                    install.configure(state='disabled')
                    detail.set(data)
                    if args.gui_smoke:
                        smoke_result['error']=data;window.destroy();return
                    messagebox.showerror('Update stopped',data)
        except queue.Empty:
            pass
        window.after(100,poll)
    def close():
        if busy:
            messagebox.showinfo('Update in progress','Wait for the update to finish before closing.')
        else:
            window.destroy()
    def licenses():
        base = Path(getattr(sys,'_MEIPASS',Path(__file__).parent))/'licenses'
        popup=tk.Toplevel(window);popup.title('Licenses');popup.geometry('700x500')
        text=tk.Text(popup,wrap='word');text.pack(fill='both',expand=True)
        for path in sorted(base.glob('*.txt')) if base.is_dir() else []:
            text.insert('end',path.name+'\n'+path.read_text(encoding='utf-8-sig',errors='replace')+'\n\n')
        text.configure(state='disabled')
    ttk.Button(frame,text='Licenses',command=licenses).pack(anchor='e')
    window.protocol('WM_DELETE_WINDOW',close)
    if args.gui_smoke:
        window.after(50,check.invoke)
        window.after(20000,window.destroy)
    poll()
    window.mainloop()
    if smoke_tmp is not None:
        smoke_tmp.cleanup()
        if args.report:core.atomic(args.report,core.encoded(smoke_result))
        return 0 if smoke_result['passed'] else 1
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
