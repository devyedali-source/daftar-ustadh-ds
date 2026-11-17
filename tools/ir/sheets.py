import json, sys, subprocess, os
from PIL import Image, ImageDraw
lv=sys.argv[1]; d=json.load(open(sys.argv[2])); out=sys.argv[3]; os.makedirs(out,exist_ok=True)
pdf=f'../ir3/IR_{lv}_src.pdf'; cache={}
def page(p):
    if p not in cache:
        f=f'/tmp/pg_{lv}_{p}'
        if not os.path.exists(f+'.png'): subprocess.run(['pdftoppm','-f',str(p),'-l',str(p),'-r','60','-png','-singlefile',pdf,f],check=True)
        cache[p]=Image.open(f+'.png').convert('RGB')
    return cache[p]
crops=[]
for i,l in enumerate(d['lessons']):
    for s in l['board']:
        if 'fig' in s:
            f=s['fig']; im=page(f['page']); W,H=im.size; r=f['rect']
            c=im.crop((int(r[0]*W),int(r[1]*H),int(r[2]*W),int(r[3]*H)))
            crops.append((f'{i+1} p{f["page"]}',c))
n=0
for k in range(0,len(crops),14):
    part=crops[k:k+14]; h=sum(c.height+14 for _,c in part); w=max(c.width for _,c in part)
    sh=Image.new('RGB',(w,h),'white'); y=0; dr=ImageDraw.Draw(sh)
    for lab,c in part:
        dr.rectangle((0,y,w,y+13),fill='black'); dr.text((3,y),lab,fill='yellow'); sh.paste(c,(0,y+14)); y+=c.height+14
    sh.save(f'{out}/s{n}.png'); n+=1
print(len(crops),n)
