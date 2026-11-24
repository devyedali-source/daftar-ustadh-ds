# Sciences naturelles 5e A (IPN). Le manuel 5A traite les échanges cellulaires et l’alimentation-digestion
# (mêmes notions que le manuel 5D) : les leçons reprennent celles du 5D, avec les pages du manuel 5A.
import importlib.util as _u, os as _os, copy as _c
BOOK = "SN_5A"
_s = _u.spec_from_file_location("_s5d", _os.path.join(_os.path.dirname(_os.path.abspath(__file__)), "SN_5D.py"))
_m = _u.module_from_spec(_s); _s.loader.exec_module(_m)
def _k(i, title, pages):
    x = _c.deepcopy(_m.LESSONS[i]); x.update(title=title, match=[title.split(" : ", 1)[-1]], pages=pages); return x
LESSONS = [
    _k(0, "Unité I : Les échanges cellulaires — échanges d’eau (osmose)", [9, 16]),
    _k(1, "Unité I : Substances dissoutes, transport membranaire, endocytose", [17, 28]),
    _k(2, "Unité II : L’alimentation", [29, 40]),
    _k(3, "Unité II : La digestion chez l’homme et l’hygiène alimentaire", [41, 59]),
]
