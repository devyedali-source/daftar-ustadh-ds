import json, re, sys
TASH=re.compile(r'[ً-ْٰ]')
HEAD_WORDS=r'(المنطلق|نص الآيات|نص الآية|نص الحديث|النص|شرح الكلمات|شرح المفردات|الشرح|التعليق|المحاور|الخلاصة|الاستخلاص|الأسئلة|المناقشة|التقويم|العرض|الأهداف|ما يستفاد|الفوائد|العبر|الدروس والعبر|المضمون|المعنى الإجمالي|التحليل|تعريف\w*|أولا|ثانيا|ثالثا|رابعا|خامسا|سادسا|سابعا|ثامنا|مراجع|الهدف|تمهيد|مقدمة)'
QSECT=r'^(الأسئلة|المناقشة|التقويم|أسئلة|نشاط|أنشطة|التمارين|تمارين)'
def clean(s):
    s=s.replace('⟪Q⟫','').replace('ﭽ','').replace('ﭼ','')
    s=re.sub(r'(?<=[؀-ۿ])�+(?=[؀-ۿ])','',s)
    s=re.sub(r'\s+',' ',s).strip()
    return s
def bare(s): return TASH.sub('',s).strip()
def lesson(level, b):
    P=json.load(open(f'../ir3/IR_{level}_lines.json'))
    items=[]
    for p in range(b['pages'][0], b['pages'][1]+1):
        for l in P[p-1]: items.append(dict(l, page=p))
    # skip header lines (الدرس / title)
    i=0
    while i<len(items) and i<4 and (re.search(r'الدرس',bare(items[i]['text'])) or (i>0 and not items[i]['q'] and len(bare(items[i]['text']))<70 and not re.match(HEAD_WORDS,bare(items[i]['text'])))):
        i+=1
    items=items[i:]
    secs=[]; cur=None
    def new(h, it=None):
        nonlocal cur
        cur=dict(h=h,lines=[],fig=None,reg=[it['page'],it['top'],it['bottom']] if it else None); secs.append(cur)
    new('')
    k=0; para=None
    while k<len(items):
        l=items[k]; t=clean(l['text']); bt=bare(t)
        if cur.get('reg') and l['page']==cur['reg'][0]: cur['reg'][2]=max(cur['reg'][2],l['bottom'])
        if l['q']:
            # verse block: consecutive q lines (and tiny bracket-only lines) on same page
            j=k; top=l['top']; bot=l['bottom']
            while j+1<len(items) and items[j+1]['page']==l['page'] and (items[j+1]['q'] or (len(bare(clean(items[j+1]['text'])))<22 and re.search(r'[\[\]]',items[j+1]['text']))) and items[j+1]['top']-bot<0.03:
                j+=1; bot=max(bot,items[j]['bottom']); top=min(top,items[j]['top'])
            cap=' '.join(re.findall(r'\[[^\]]*\]',' '.join(it['text'] for it in items[k:j+1])))
            fig=dict(page=l['page'],rect=[0.07,round(max(0,top-0.012),3),0.935,round(min(1,bot+0.003),3)],caption=bare(cap.replace('[','').replace(']','')).strip(' .،'))
            if cur['fig']: new('')
            cur['fig']=fig; para=None
            k=j+1; continue
        if not bt: k+=1; continue
        ishead = (len(bt)<=45 and (re.match(HEAD_WORDS,bt) or (bt.endswith(':') and len(bt)<=40))) and not re.match(r'^[\-–\d]',bt)
        if ishead:
            new(bt.rstrip(': ').strip(), l); para=None; k+=1; continue
        if cur['fig'] is not None: new('')
        starts_new = para is None or re.match(r'^([\-–•]|\d+\s*[\-–ـ)]|[أبجده]\s*[\-–ـ)])',bt) or cur['lines'][-1].endswith(('.',':','؛','»',')'))
        if starts_new: cur['lines'].append(t); para=True
        else: cur['lines'][-1]+=' '+t
        k+=1
    named=[x for x in secs if x.get('reg')]
    for a,b2 in zip(named,named[1:]):
        if b2['reg'][0]==a['reg'][0]: a['reg'][2]=max(a['reg'][2], b2['reg'][1]-0.006)
        else: a['reg'][2]=max(a['reg'][2], max((it['bottom'] for it in items if it['page']==a['reg'][0]), default=a['reg'][2]))
    secs=[s for s in secs if s['lines'] or s['fig']]
    return secs
if __name__=='__main__':
    level=sys.argv[1]; B=json.load(open(f'../ir3/IR_{level}_bounds.json'))
    for n in map(int,sys.argv[2:]):
        b=B[n-1]; secs=lesson(level,b)
        print(f'######## {n} {b["title"]} {b["pages"]}')
        for s in secs:
            print(f'## {s["h"]}' + (f'  [FIG p{s["fig"]["page"]} {s["fig"]["rect"][1]}-{s["fig"]["rect"][3]} {s["fig"]["caption"]}]' if s['fig'] else ''))
            for x in s['lines']: print('  - '+x)
