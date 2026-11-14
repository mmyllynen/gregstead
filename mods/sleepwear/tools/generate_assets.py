"""Generate original pixel-art pajamas, equipment variants, and wool recipes."""
from pathlib import Path
from PIL import Image, ImageDraw
import json
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
COLORS=dict(zip(['white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black'],['F9FFFE','F9801D','C74EBD','3AB3DA','FED83D','80C71F','F38BAA','474F52','9D9D97','169C9C','8932B8','3C44AA','835432','5E7C16','B02E26','1D1D21']))
def write(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,separators=(',',':'))+'\n')
def save(path,im):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def rgb(c):return tuple(bytes.fromhex(COLORS[c]))
def shade(c,k):return tuple(max(0,min(255,int(v*k))) for v in c)+(255,)
def color(style,a,b,row,height):return rgb(a if style=='solid' or (row<height/2 if style=='split' else (row//2)%2==0) else b)
for style in ['solid','split','stripes']:
 for a in COLORS:
  for b in ([a] if style=='solid' else COLORS):
   design=f'{style}_{a}_{b}'
   for kind in ['humanoid','humanoid_leggings']:
    im=Image.new('RGBA',(64,32));pix=im.load()
    # Standard humanoid UV rectangles: body, arms, or waist and legs.
    rects=[(16,16,40,32),(40,16,56,32)] if kind=='humanoid' else [(16,16,40,20),(0,20,16,32)]
    for x0,y0,x1,y1 in rects:
     for y in range(y0,y1):
      for x in range(x0,x1):
       # Waist corresponds to the top of pants; leg UV continues below it.
       row=max(0,y-20)
       c=color(style,a,b,row,12)
       pix[x,y]=shade(c,.75 if x in [x0,x1-1] or y==y1-1 else 1)
    # Small shirt buttons on the chest front.
    if kind=='humanoid':
     for y in [22,26,30]:pix[24,y]=(225,215,190,255)
    save(Path(f'assets/sleepwear/textures/entity/equipment/{kind}/{design}.png'),im)
   write(Path(f'assets/sleepwear/equipment/{design}.json'),{'layers':{k:[{'texture':f'sleepwear:{design}'}] for k in ['humanoid','humanoid_leggings']}})
   for garment in ['shirt','pants']:
    im=Image.new('RGBA',(16,16));p=im.load()
    for y in range(2,15):
     for x in range(2,14):
      inside=(y>=5 and 4<=x<=11) or (y<6 and (2<=x<=5 or 10<=x<=13)) if garment=='shirt' else (y<=5 and 3<=x<=12) or (y>5 and (3<=x<=6 or 9<=x<=12))
      if inside:p[x,y]=shade(color(style,a,b,y-2,13),.7 if x in [2,3,12,13] or y==14 else 1)
    stem=f'{garment}/{design}';save(Path(f'assets/sleepwear/textures/item/{stem}.png'),im)
    write(Path(f'assets/sleepwear/models/item/{stem}.json'),{'parent':'minecraft:item/generated','textures':{'layer0':f'sleepwear:item/{stem}'}})
    write(Path(f'assets/sleepwear/items/{stem}.json'),{'model':{'type':'minecraft:model','model':f'sleepwear:item/{stem}'}})
for garment in ['shirt','pants']:
 write(Path(f'assets/sleepwear/items/pajama_{garment}.json'),{'model':{'type':'minecraft:model','model':f'sleepwear:item/{garment}/solid_white_white'}})
 for c in COLORS:
  d=f'solid_{c}_{c}'
  write(Path(f'data/sleepwear/recipe/{garment}_{c}.json'),{'type':'minecraft:crafting_shaped','category':'equipment','pattern':['X X','XXX','XXX'] if garment=='shirt' else ['XXX','X X','X X'],'key':{'X':f'minecraft:{c}_wool'},'result':{'id':f'sleepwear:pajama_{garment}','components':{'sleepwear:design':d,'minecraft:item_model':f'sleepwear:{garment}/{d}','minecraft:equippable':{'slot':'chest' if garment=='shirt' else 'legs','asset_id':f'sleepwear:{d}','damage_on_hurt':False}}}})
write(Path('data/sleepwear/recipe/colour.json'),{'type':'sleepwear:colour'})
for lang,names in [('en_us',['Pajama Shirt','Pajama Pants']),('fi_fi',['Yöpaita','Yöhousut'])]:
 write(Path(f'assets/sleepwear/lang/{lang}.json'),{f'item.sleepwear.pajama_{k}':v for k,v in zip(['shirt','pants'],names)})

for lang,hints in [('en_us',['Wear both pieces to sleep automatically in a nearby bed.','Dye: one = solid; above/below = split; left/right = stripes.']),('fi_fi',['Pue molemmat osat nukkuaksesi automaattisesti lähellä olevaan sänkyyn.','Värjäys: yksi = tasaväri; yllä/alla = kaksivärinen; sivuilla = raidat.'])]:
 p=ROOT/f'assets/sleepwear/lang/{lang}.json';data=json.loads(p.read_text());data.update(dict(zip(['sleepwear.hint.sleep','sleepwear.hint.dye'],hints)));p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
