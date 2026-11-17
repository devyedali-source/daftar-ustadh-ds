import sys,glob,os
from PIL import Image,ImageDraw
files=sorted(glob.glob(os.path.join(os.path.dirname(__file__),'../../app/src/teacher/assets/lessons/draw/'+sys.argv[1])))
W=440;ims=[]
for f in files:
    im=Image.open(f).convert('RGB'); r=W/im.width; im=im.resize((W,int(im.height*r)))
    lab=Image.new('RGB',(W,14),'yellow');ImageDraw.Draw(lab).text((2,1),os.path.basename(f),fill='black'); ims+=[lab,im]
cols=2; colh=[0]*cols; place=[]
for i in range(0,len(ims),2):
    c=colh.index(min(colh)); place.append((c,colh[c],ims[i],ims[i+1])); colh[c]+=ims[i].height+ims[i+1].height+6
S=Image.new('RGB',(W*cols+10,max(colh)),'#888')
for c,y,a,b in place: S.paste(a,(c*(W+10),y)); S.paste(b,(c*(W+10),y+a.height))
S.save(sys.argv[2])
