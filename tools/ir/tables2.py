import pdfplumber, json, sys
a,b=int(sys.argv[3]),int(sys.argv[4]); out={}
with pdfplumber.open(sys.argv[1], pages=list(range(a,b+1))) as pdf:
    for pg in pdf.pages:
        try: ts=pg.find_tables()
        except Exception: ts=[]
        bb=[[round(t.bbox[1]/pg.height,3),round(t.bbox[3]/pg.height,3),len(t.rows)] for t in ts if (t.bbox[3]-t.bbox[1])/pg.height>0.03]
        if bb: out[pg.page_number]=bb
        pg.flush_cache()
json.dump(out,open(sys.argv[2],'w'))
