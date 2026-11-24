# Physique-Chimie 6e C — partie Physique (IPN). Même programme (9 chapitres) que la 6e D :
# on reprend les leçons de PC_6D_PHY avec les pages du manuel de 6e C.
import importlib.util, os

BOOK = "PC_6C_PHY"

_spec = importlib.util.spec_from_file_location("PC_6D_PHY_src", os.path.join(os.path.dirname(os.path.abspath(__file__)), "PC_6D_PHY.py"))
_d = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_d)

LESSONS = _d.lessons({"cin1": [7, 18], "cin2": [19, 34], "trav": [35, 45], "ec": [46, 51], "ep": [52, 62],
                      "therm": [63, 75], "mag": [76, 90], "elec1": [91, 96], "elec2": [97, 106],
                      "puiss": [107, 124], "cond": [125, 138], "alt": [139, 160]})
