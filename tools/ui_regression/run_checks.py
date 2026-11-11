"""Local network regression driver. Requires both ui-regression Gradle runs."""
from pathlib import Path
import re,time,json,sys
ROOT=Path(__file__).resolve().parents[2]
CLIENT=ROOT/'.test-client';SERVER=ROOT/'.build/lathe-server';REPORT=ROOT/'.build/lathe-test-reports';REPORT.mkdir(exist_ok=True)
def wait(predicate,seconds=30):
 deadline=time.monotonic()+seconds
 while time.monotonic()<deadline:
  if predicate():return
  time.sleep(.1)
 raise RuntimeError('Timed out waiting for regression probe')
def command(root,text,response=True):
 file=root/'ui-command.txt';result=root/'ui-result.txt';assert not file.exists(),file
 old=result.stat().st_mtime_ns if result.exists() else 0
 file.write_text(text+'\n');wait(lambda:not file.exists())
 if not response:return ''
 wait(lambda:result.exists() and result.stat().st_mtime_ns!=old)
 out=result.read_text();assert not out.startswith('ERROR'),out
 return out
def maps(out):return re.findall(r'^\s*(\S+) id=(.*?) menuIndex=(\d+) storageIndex=(\d+)$',out,re.M)
def slots(out):return {int(i):(int(n),item) for i,n,item in re.findall(r'^(\d+) storageIndex=\d+ class=\S+ item=(\d+) (\S+)$',out,re.M)}
def pair(label):
 server=command(SERVER,'dump');time.sleep(.2);client=command(CLIENT,'dump')
 (REPORT/(label+'-server.txt')).write_text(server);(REPORT/(label+'-client.txt')).write_text(client)
 assert maps(server)==maps(client),(label,'widget/slot mapping mismatch')
 assert slots(server)==slots(client),(label,'content mismatch')
 return slots(server)
def open_ui(x,label):
 command(SERVER,f'open {x} 160 10');time.sleep(.4)
 return pair(label)
def click(index,kind='PICKUP'):command(CLIENT,f'click {index} {kind}');time.sleep(.25)
def run():
 wait(lambda:(CLIENT/'ui-screen.txt').exists() and (CLIENT/'ui-screen.txt').read_text()=='world',120)
 results={}
 if '--network-only' not in sys.argv:
  report=command(CLIENT,'templates');(REPORT/'fixed-template-checks.txt').write_text(report)
  assert report.count(' PASS ')==5,report
  results['templates']=report.splitlines()
 for n in range(10):
  data=open_ui(1,f'fixed-lathe-{n+1}');assert len(data)==41
  command(CLIENT,'close',False);time.sleep(.15)
 results['latheRepeatedOpens']=10
 data=open_ui(4,'fixed-mixer');assert len(data)==45
 for i in range(37,44):
  if data[i][0]:click(i,'QUICK_MOVE')
 data=pair('fixed-mixer-collected');empty=next(i for i in range(9,36) if data[i][0]==0)
 command(SERVER,'seed 37 minecraft:iron_ingot 2');click(37);click(empty)
 assert pair('fixed-mixer-pickup')[empty]==(2,'minecraft:iron_ingot')
 click(empty,'QUICK_MOVE');assert pair('fixed-mixer-shift')[37]==(2,'minecraft:iron_ingot')
 command(SERVER,'seed 43 minecraft:diamond 2');before=pair('fixed-mixer-output-seeded');click(43,'QUICK_MOVE');after=pair('fixed-mixer-output-taken')
 assert after[43][0]==0
 assert sum(n for n,it in before.values() if it=='minecraft:diamond')==sum(n for n,it in after.values() if it=='minecraft:diamond')
 click(37,'QUICK_MOVE');results['mixerTransfers']='PASS'
 command(CLIENT,'close',False);time.sleep(.15)
 data=open_ui(1,'fixed-lathe-transfer-start')
 command(SERVER,'disable')
 # Keep previous fixture progress: collect leftovers through normal network clicks.
 for i in (36,37,38,39):
  if data[i][0]:click(i,'QUICK_MOVE')
 data=pair('fixed-fixture-collected')
 assert all(data[i][0]==0 for i in (36,37,38,39)),data
 inv0,inv1,inv2=[i for i in range(9,36) if data[i][0]==0][:3]
 command(SERVER,'seed 37 minecraft:iron_ingot 4');time.sleep(.2)
 click(37);click(inv0);data=pair('fixed-input-pickup');assert data[inv0]==(4,'minecraft:iron_ingot') and data[37][0]==0
 click(inv0,'QUICK_MOVE');data=pair('fixed-input-shift');assert data[37]==(4,'minecraft:iron_ingot') and data[inv0][0]==0
 command(SERVER,'seed 38 minecraft:diamond 3');command(SERVER,'seed 39 minecraft:redstone 2');time.sleep(.2)
 before=pair('fixed-output-seeded');click(38,'QUICK_MOVE');click(39);click(inv1)
 after=pair('fixed-output-transfers');assert after[38][0]==after[39][0]==0
 for item in ('minecraft:diamond','minecraft:redstone'):
  assert sum(n for n,it in before.values() if it==item)==sum(n for n,it in after.values() if it==item)
 command(SERVER,f'seed {inv2} gtceu:lv_lithium_battery 1');click(inv2);click(36);assert pair('fixed-battery-insert')[36]==(1,'gtceu:lv_lithium_battery')
 click(36);click(inv2);assert pair('fixed-battery-remove')[36][0]==0
 click(37);click(inv0);assert pair('fixed-input-retrieve')[37][0]==0
 command(SERVER,'seed 37 minecraft:iron_ingot 1');command(SERVER,'power')
 wait(lambda:slots(command(SERVER,'dump'))[38][0]>0,60)
 data=pair('fixed-recipe-finished');assert data[37][0]==0 and data[38][1]=='gtceu:iron_rod',data
 results['transfersAndRecipe']='PASS'
 command(CLIENT,'capture lathe-shaders-on');command(CLIENT,'close',False);time.sleep(.15)
 (REPORT/'fixed-checks.json').write_text(json.dumps(results,indent=2)+'\n');print(json.dumps(results,indent=2))
if __name__=='__main__':run()
