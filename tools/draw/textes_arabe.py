"""يضع نص القراءة (مقصوصا من الكتاب) في أول سبورة دروس النصوص في العربية، حسب textes_arabe.txt."""
import json, collections, os
D = os.path.join(os.path.dirname(__file__), '../../app/src/teacher/assets/lessons/')
g = collections.OrderedDict()
for line in open(os.path.join(os.path.dirname(__file__), 'textes_arabe.txt'), encoding='utf-8'):
    if not line.strip(): continue
    f, li, _, p, y0, y1, x0, x1, _ = line.rstrip('\n').split('|')
    g.setdefault((f, int(li)), []).append((int(p), float(y0), float(y1), float(x0), float(x1)))
cache = {}
for (f, li), crops in g.items():
    d = cache.setdefault(f, json.load(open(D + f + '.json', encoding='utf-8')))
    b = [s for s in d['lessons'][li]['board'] if not s['h'].startswith('النص')]
    d['lessons'][li]['board'] = [{'h': 'النص' if k == 0 else 'النص (تابع)', 'lines': [], 'fig': {'page': p, 'rect': [x0, y0, x1, y1], 'caption': ''}}
                                 for k, (p, y0, y1, x0, x1) in enumerate(crops)] + b
for f, d in cache.items(): json.dump(d, open(D + f + '.json', 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print(len(g), 'درس نص')
