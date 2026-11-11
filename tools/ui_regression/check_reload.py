import sys,time,json
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent));import run_checks as r
checks={}
for n in range(10):
 d=r.open_ui(1,f'final-lathe-{n+1}');assert len(d)==41
 r.command(r.CLIENT,'close',False);time.sleep(.15)
checks['latheRepeatedOpens']=10
assert len(r.open_ui(4,'final-mixer'))==45
r.command(r.CLIENT,'close',False);time.sleep(.15)
(r.CLIENT/'ui-reload-complete.txt').unlink(missing_ok=True)
r.command(r.CLIENT,'reload',False)
r.wait(lambda:(r.CLIENT/'ui-reload-complete.txt').exists(),60)
report=r.command(r.CLIENT,'templates');assert report.count(' PASS ')==5,report
(r.REPORT/'final-template-checks-after-reload.txt').write_text(report)
assert len(r.open_ui(1,'final-lathe-after-reload'))==41
r.command(r.CLIENT,'close',False);time.sleep(.15)
checks['resourceReload']='PASS'
r.command(r.SERVER,'command kick GregsteadTest regression reconnect')
r.wait(lambda:'DisconnectedScreen' in (r.CLIENT/'ui-screen.txt').read_text())
r.command(r.CLIENT,'connect',False)
r.wait(lambda:(r.CLIENT/'ui-screen.txt').read_text()=='world',60)
assert len(r.open_ui(1,'final-lathe-after-reconnect'))==41
checks['reconnect']='PASS'
r.command(r.CLIENT,'capture lathe-shaders-off-final')
(r.REPORT/'final-checks.json').write_text(json.dumps(checks,indent=2)+'\n');print(json.dumps(checks))
