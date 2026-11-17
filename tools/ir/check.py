import json, sys
lv=sys.argv[1]; d=json.load(open(sys.argv[2]))
plan=json.load(open('/home/claude/daftar-ustadh-ds/app/src/teacher/assets/planning/planning.json'))['plans']['IR'][lv]
titles=[t for r in plan if r['k']=='lesson' for t in r['i']]
m={t for l in d['lessons'] for t in l['match']}
print('غير مربوط من التخطيط:', [t for t in dict.fromkeys(titles) if t not in m])
print('مطابقات لا توجد في التخطيط:', [t for t in m if t not in titles])
print('دروس بلا عنوان تخطيط:', [l['title'] for l in d['lessons'] if not l['match']])
pp=[l['pages'] for l in d['lessons']]
print('صفحات متداخلة:', [(a,b) for a,b in zip(pp,pp[1:]) if b[0]<=a[1]])
nf=sum(1 for l in d['lessons'] for s in l['board'] if 'fig' in s); print('دروس',len(d['lessons']),'آيات مقصوصة',nf)
