"""يضيف رسوم الشرح إلى سبورة الدروس حسب carte.txt (حقل draw في القسم)."""
import json, os
D = os.path.join(os.path.dirname(__file__), '../../app/src/teacher/assets/lessons/')
cache = {}
for line in open(os.path.join(os.path.dirname(__file__), 'carte.txt'), encoding='utf-8'):
    if not line.strip() or line.startswith('#'): continue
    f, li, sec, img, cap = line.rstrip('\n').split('|')
    assert os.path.exists(D + 'draw/' + img + '.png'), img
    d = cache.setdefault(f, json.load(open(D + f + '.json', encoding='utf-8')))
    b = d['lessons'][int(li)]['board']; dr = {'src': 'draw/' + img + '.png', 'caption': cap}
    if sec.startswith('+'):
        s = [x for x in b if x['h'] == sec[1:]]
        if s: s[0]['draw'] = dr
        else:
            # يوضع القسم الجديد بعد آخر قسم فيه رسم من الكتاب أو في الآخر
            b.append({'h': sec[1:], 'lines': [], 'draw': dr})
    else:
        s = [x for x in b if x['h'] == sec]; assert s, (f, li, sec)
        s[0]['draw'] = dr
for f, d in cache.items():
    json.dump(d, open(D + f + '.json', 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print('تم:', ', '.join(cache))
