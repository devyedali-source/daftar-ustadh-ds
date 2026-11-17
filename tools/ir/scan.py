import pdfplumber, re, sys, json
def isq(c):
    f=c['fontname'].split('+')[-1]
    return re.fullmatch(r'P\d+',f) is not None or f=='BSML' or re.search(r'[\u06D6-\u06ED\u08F0-\u08FF]',c['text']) is not None
def iswm(c):
    f=c['fontname'].split('+')[-1]
    return c['text'] in 'IPN' and f in ('Helvetica','Arial')
TASH=re.compile(r'[ً-ْٰـ]')
def page_lines(pg):
    W,H=pg.width,pg.height
    chars=sorted([c for c in pg.chars if not iswm(c)], key=lambda c:c['top'])
    lines=[]
    for c in chars:
        if lines and abs(lines[-1][0]-c['top'])<4: lines[-1][1].append(c)
        else: lines.append([c['top'],[c]])
    out=[]
    for top,cs in lines:
        cs.sort(key=lambda c:-c['x0'])
        s=''; q=False; qb=None; size=max(c['size'] for c in cs)
        for c in cs:
            if isq(c):
                if not q: s+=' ⟪Q⟫ '; q=True
                b=[c['x0'],c['top'],c['x1'],c['bottom']]
                qb=[min(qb[0],b[0]),min(qb[1],b[1]),max(qb[2],b[2]),max(qb[3],b[3])] if qb else b
            else:
                q=False; s+=c['text']
        s=s.replace('ـ','')
        s=re.sub(r'\s+',' ',s).strip()
        bare=TASH.sub('',s).strip()
        if not bare or re.fullmatch(r'(IP|N|IPN|NIP|P|I|\d{1,3}|[ﭽﭼ\s]+)',bare): 
            if not qb: continue
        out.append(dict(top=round(top/H,4),bottom=round(max(c['bottom'] for c in cs)/H,4),text=s,size=round(size,1),
            q=[round(qb[0]/W,4),round(qb[1]/H,4),round(qb[2]/W,4),round(qb[3]/H,4)] if qb else None))
    return out
if __name__=='__main__':
    pdf=pdfplumber.open(sys.argv[1]); res=[]
    for i,pg in enumerate(pdf.pages): res.append(page_lines(pg))
    json.dump(res,open(sys.argv[2],'w'),ensure_ascii=False)
