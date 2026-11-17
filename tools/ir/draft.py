import json, sys, re
from build import lesson
level=sys.argv[1]; B=json.load(open(f'../ir3/IR_{level}_bounds.json')); TB=json.load(open(f'../ir3/IR_{level}_tables.json'))
FULL=('خلاص','استخلاص','الخلاصة','أسئل','مناقش','تقويم','شرح الكلمات','شرح المفردات','الكلمات','نستفيد','ما يستفاد','العبر','الفوائد')
for n in range(int(sys.argv[2]), int(sys.argv[3])+1):
    b=B[n-1]; secs=lesson(level,b)
    print(f'#### {n} {b["title"][:70]} {b["pages"]}')
    tb=[f'{p}:{t[0]}-{t[1]}({t[2]})' for p in range(b['pages'][0],b['pages'][1]+1) for t in TB.get(str(p),[])]
    if tb: print('TABLES '+' '.join(tb))
    big=0
    for s in secs:
        f=s['fig']; full=any(w in s['h'] for w in FULL)
        fs=''
        if f:
            h=f['rect'][3]-f['rect'][1]
            if h>=0.05 or 'منطلق' in s['h'] or 'نص' in s['h'] or big<1:
                fs=f' [F {f["page"]} {f["rect"][1]} {f["rect"][3]} {f["caption"]}]'; big+=1
        if not s['h'] and not fs and not s['lines']: continue
        gs=''
        if s.get('reg') and ('الكلمات' in s['h'] or 'المفردات' in s['h']):
            r=s['reg']; gs=f' [G {r[0]} {round(r[1]-0.01,3)} {round(r[2]+0.004,3)}]'
        if s['h'] or fs: print('## '+s['h']+fs+gs)
        L=s['lines'] if full else s['lines'][:2]
        for x in L:
            x=re.sub(r'^\s*الدرس.*','',x).strip()
            if x: print('  - '+(x if full else x[:150]+('…' if len(x)>150 else '')))
