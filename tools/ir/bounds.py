import json, re, sys
TASH=re.compile(r'[ً-ْٰ]')
def bounds(level):
    pages=json.load(open(f'../ir3/IR_{level}_lines.json'))
    starts=[]
    for i,ls in enumerate(pages):
        for j,l in enumerate(ls[:4]):
            t=TASH.sub('',l['text'])
            if re.search(r'^\W*الدرس\b',t) and len(t)<45:
                # title = next non-empty lines until a section word
                title=''
                for k in ls[j+1:j+3]:
                    tt=TASH.sub('',k['text']).strip()
                    if tt and not re.match(r'^(المنطلق|نص|العرض|المحاور|تعريف|أولا|شرح)',tt) and '⟪Q⟫' not in tt:
                        title=(title+' '+tt).strip()
                    else: break
                starts.append([i+1,title]); break
    # section divider pages
    div=[i+1 for i,ls in enumerate(pages) if len(ls)<=3 and any(re.search(r'^دروس',TASH.sub('',l['text'])) for l in ls)]
    out=[]
    for n,(p,t) in enumerate(starts):
        end=(starts[n+1][0]-1) if n+1<len(starts) else len(pages)
        while end>p and (end in div or len(pages[end-1])<2): end-=1
        out.append(dict(n=n+1,title=t,pages=[p,end]))
    return out,div
if __name__=='__main__':
    for lv in sys.argv[1:]:
        b,div=bounds(lv); json.dump(b,open(f'../ir3/IR_{lv}_bounds.json','w'),ensure_ascii=False)
        print(lv,len(b),'div',div)
        for x in b: print(' ',x['n'],x['pages'],x['title'])
