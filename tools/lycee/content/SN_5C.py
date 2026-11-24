# Sciences naturelles 5e C (IPN). Le manuel 5C traite les échanges cellulaires, l’alimentation-digestion et séisme-volcanisme,
# avec les mêmes activités que le manuel 5D : les leçons reprennent celles du 5D pour ces notions, avec les pages du manuel 5C.
import importlib.util as _u, os as _os, copy as _c
BOOK = "SN_5C"
_s = _u.spec_from_file_location("_s5d", _os.path.join(_os.path.dirname(_os.path.abspath(__file__)), "SN_5D.py"))
_m = _u.module_from_spec(_s); _s.loader.exec_module(_m)
def _k(i, title, pages):
    x = _c.deepcopy(_m.LESSONS[i]); x.update(title=title, match=[title.split(" : ", 1)[-1]], pages=pages); return x
LESSONS = [
    _k(0, "Unité I : Les échanges cellulaires — les échanges d’eau", [9, 14]),
    _k(1, "Unité I : Substances dissoutes, pression osmotique et perméabilité", [15, 24]),
    _k(2, "Unité II : L’alimentation de l’homme", [25, 40]),
    _k(3, "Unité II : La digestion et le devenir des nutriments", [41, 62]),
    _k(6, "Unité III : Séisme et volcanisme", [63, 96]),
]
