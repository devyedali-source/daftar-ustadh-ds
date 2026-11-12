"""أدوات مشتركة لتوليد تمارين الرياضيات بالفرنسية مع حلول محسوبة (لا خطأ حسابي)."""
from decimal import Decimal as D, ROUND_HALF_UP
from fractions import Fraction as F

def fmt(x):
    """عدد بالكتابة الفرنسية: فاصلة عشرية ومسافة كل ثلاثة أرقام (ابتداءً من 1 000)."""
    if isinstance(x, F):
        x = D(x.numerator) / D(x.denominator)
    if not isinstance(x, D):
        x = D(str(x))
    s = format(x, 'f')
    if '.' in s:
        s = s.rstrip('0').rstrip('.')
    neg = s.startswith('-'); s = s.lstrip('-')
    ip, _, dp = s.partition('.')
    if len(ip) >= 4:
        g = []
        while len(ip) > 3:
            g.insert(0, ip[-3:]); ip = ip[:-3]
        g.insert(0, ip); ip = ' '.join(g)
    return ('−' if neg else '') + ip + (',' + dp if dp else '')

def fr(x):
    f = F(x)
    return str(f.numerator) if f.denominator == 1 else f"{f.numerator}/{f.denominator}"

def rnd(x, n=1):
    return D(str(x)).quantize(D(1).scaleb(-n), rounding=ROUND_HALF_UP)

PI = D('3.14')
