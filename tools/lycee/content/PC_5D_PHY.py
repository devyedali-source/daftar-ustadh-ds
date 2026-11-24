# Physique 5e D (IPN). Le manuel 5D reprend le manuel 5C (mêmes chapitres et même contenu, mise en page différente).
# Les leçons reprennent celles du manuel 5C avec les pages du manuel 5D.
import importlib.util as _u, os as _os, copy as _c
BOOK = "PC_5D_PHY"
_s = _u.spec_from_file_location("_p5c", _os.path.join(_os.path.dirname(_os.path.abspath(__file__)), "PC_5C_PHY.py"))
_m = _u.module_from_spec(_s); _s.loader.exec_module(_m)
_PAGES = [[9, 24], [25, 38], [39, 52], [53, 71], [72, 86], [87, 101], [102, 119], [120, 131], [132, 152], [153, 172]]
LESSONS = []
for _l, _p in zip(_m.LESSONS, _PAGES):
    _x = _c.deepcopy(_l); _x["pages"] = _p; LESSONS.append(_x)
