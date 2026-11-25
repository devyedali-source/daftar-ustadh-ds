# Physique-Chimie 7e D — partie Physique (IPN). Les chapitres du manuel de 7e D sont communs avec la 7e C
# (sans auto-induction, condensateur, oscillations électriques) : on reprend les leçons de PC_7C_PHY avec les pages et
# les numéros de chapitre du manuel de 7e D.
import importlib.util, os

BOOK = "PC_7D_PHY"
_s = importlib.util.spec_from_file_location("PC_7C_PHY_src", os.path.join(os.path.dirname(os.path.abspath(__file__)), "PC_7C_PHY.py"))
_c = importlib.util.module_from_spec(_s)
_s.loader.exec_module(_c)

_P = {"cin": [7, 15], "rfd": [16, 24], "proj": [25, 30], "plan": [31, 38], "champE": [39, 48], "sat": [49, 61],
      "lor": [62, 87], "osc": [88, 102], "lap": [103, 115], "ind": [116, 130], "prop": [131, 150],
      "interm": [151, 162], "intl": [163, 177], "niv": [178, 189], "photo": [190, 204], "nuc": [205, 226]}
_N = {"cin": "I", "rfd": "I", "proj": "II", "plan": "II", "champE": "II", "sat": "II", "lor": "II", "osc": "III",
      "lap": "IV", "ind": "V", "prop": "VI", "interm": "VII", "intl": "VIII", "niv": "IX", "photo": "X", "nuc": "XI"}
LESSONS = _c.lessons(_P, _N)
