"""يكمل السبورة بما في التحضير: النقاط (والتعريفات في الرياضيات) غير الموجودة على السبورة، وتمرين التطبيق نفسه."""
import json, re, glob, os, unicodedata
D = os.path.join(os.path.dirname(__file__), '../../app/src/teacher/assets/lessons/')
H = {'ar': ('أهم ما يُحفظ', 'تطبيق'), 'fr': ('À retenir', 'Exercice d’application'), 'en': ('Remember', 'Practice')}
LANG = {'AR': 'ar', 'IR': 'ar', 'HG': 'ar', 'IC': 'ar', 'ANG': 'en'}
def toks(s):
    s = unicodedata.normalize('NFKC', s.lower())
    return set(w for w in re.findall(r'[\w²³ⁿ√π]+', s) if len(w) > 2)
def board_text(b):
    return ' '.join(s['h'] + ' ' + ' '.join(s['lines']) + ' ' + ' '.join(' '.join(r) for r in s.get('table', [])) for s in b)
stats = {}
for path in sorted(glob.glob(D + '*.json')):
    f = os.path.basename(path)[:-5]; subj = f.split('_')[0]; lang = LANG.get(subj, 'fr'); hr, ha = H[lang]
    d = json.load(open(path, encoding='utf-8')); na = nr = 0
    for l in d['lessons']:
        b = l['board']
        b[:] = [s for s in b if not s.get('auto')]  # إعادة التشغيل دون تكرار
        B = toks(board_text(b)); add = []
        cands = list(l['points'])
        if subj == 'MATS':
            cands += [x.strip() for x in re.split(r'(?<=\.)\s+', l['summary']) if len(x.strip()) > 20]
        for p in cands:
            T = toks(p)
            if T and len(T & B) / len(T) < 0.6 and p not in add:
                add.append(p); B |= T
        if add: b.append({'h': hr, 'lines': add, 'auto': True}); nr += 1
        q = l.get('application', {}).get('q', '').strip()
        if q and q[:25] not in board_text(b):
            b.append({'h': ha, 'lines': [q], 'auto': True}); na += 1
    json.dump(d, open(path, 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
    stats[f] = (len(d['lessons']), nr, na)
for k, v in stats.items(): print(k, 'دروس', v[0], '| أضيف «ما يحفظ»', v[1], '| أضيف التطبيق', v[2])
