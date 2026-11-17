"""صيغة التأليف المختصرة → ملف الدروس JSON.
L عنوان الدرس | عنوان تخطيط ; عنوان تخطيط | من-إلى
O هدف | هدف          I مقدمة         S ملخص       P نقطة | نقطة
A سؤال || جواب
B عنوان | سطر | سطر   (قسم سبورة)
F صفحة أعلى أسفل تعليق   (آية مقصوصة من الكتاب، تُلحق بآخر قسم أو تفتح قسما جديدا)
T خلية | خلية           (صف جدول في آخر قسم، الأول عناوين)
"""
import json, sys, re
def conv(src, subject, level, title):
    lessons=[]; cur=None; sec=None
    for raw in open(src, encoding='utf-8'):
        line=raw.rstrip('\n')
        if not line.strip() or line.startswith('#'): continue
        tag, _, rest = line.partition(' ')
        rest=rest.strip()
        if tag=='L':
            t,m,pg=[x.strip() for x in rest.split('|')]
            a,b=pg.split('-')
            cur=dict(title=t, match=[x.strip() for x in m.split(';') if x.strip()], pages=[int(a),int(b)],
                     objectives=[], intro='', summary='', points=[], application=dict(q='',a=''), board=[])
            lessons.append(cur); sec=None
        elif tag=='O': cur['objectives']=[x.strip() for x in rest.split('|') if x.strip()]
        elif tag=='I': cur['intro']=rest
        elif tag=='S': cur['summary']=rest
        elif tag=='P': cur['points']=[x.strip() for x in rest.split('|') if x.strip()]
        elif tag=='A':
            q,_,a=rest.partition('||'); cur['application']=dict(q=q.strip(),a=a.strip())
        elif tag=='B':
            parts=[x.strip() for x in rest.split('|')]
            sec=dict(h=parts[0], lines=[x for x in parts[1:] if x]); cur['board'].append(sec)
        elif tag=='F':
            p=rest.split(None,3); page,top,bot=int(p[0]),float(p[1]),float(p[2]); cap=p[3] if len(p)>3 else ''
            fig=dict(page=page, rect=[0.07,top,0.935,bot], caption=cap)
            if sec is None or 'fig' in sec:
                sec=dict(h='', lines=[]); cur['board'].append(sec)
            sec['fig']=fig
        elif tag=='T':
            sec.setdefault('table',[]).append([x.strip() for x in rest.split('|')])
        else: raise SystemExit(f'سطر غير مفهوم: {line}')
    for l in lessons:
        assert l['objectives'] and l['intro'] and l['summary'] and l['points'] and l['application']['q'] and l['board'], l['title']
    return dict(subject=subject, level=level, book=f'books/{subject}_{level}.pdf', bookTitle=title, lessons=lessons)
if __name__=='__main__':
    src,subject,level,title,out=sys.argv[1:6]
    d=conv(src,subject,level,title)
    json.dump(d,open(out,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
    print(len(d['lessons']),'دروس')
