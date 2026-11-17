"""رسوم الشرح بالعربية: تُكتب HTML وتُصوَّر بمتصفح Chromium (تشكيل الحروف العربية سليم)."""
import os, sys, json
from playwright.sync_api import sync_playwright
OUT = os.path.join(os.path.dirname(__file__), '../../app/src/teacher/assets/lessons/draw')
CSS = """body{margin:0;background:#fff;font-family:'Noto Naskh Arabic','Noto Sans Arabic','DejaVu Sans',sans-serif;direction:rtl}
.w{padding:18px;display:inline-block} .t{font-size:20px;font-weight:bold;text-align:center;margin-bottom:10px}"""

def climat():
    nat = [('دائرة العرض', 'قرب خط الاستواء ← حرارة أكبر'), ('التضاريس', 'الارتفاع ← حرارة أقل وأمطار أكثر'),
           ('القرب من البحار والمحيطات', 'يلطف الحرارة'), ('التيارات البحرية', 'تؤثر في حرارة المناطق التي تمر بها'),
           ('الغطاء النباتي', 'يلطف الحرارة ويزيد الرطوبة')]
    hum = ['حرق الوقود الأحفوري', 'التوسع العمراني', 'قطع الغابات', 'الرعي الجائر', 'وسائل النقل والصناعة الملوثة']
    def col(title, items, c, bg):
        rows = ''.join(f'<div style="border:2px solid {c};background:{bg};border-radius:10px;padding:6px 10px;margin:7px 0;font-size:15px">'
                       + (f'<b>{a}</b><br><span style="font-size:13px;color:#555">{b}</span>' if isinstance(a := it, tuple) is False and False else '')
                       + (f'<b>{it[0]}</b><br><span style="font-size:13px;color:#555">{it[1]}</span>' if isinstance(it, tuple) else f'<b>{it}</b>')
                       + '</div>' for it in items)
        return f'<div style="width:300px"><div style="text-align:center;font-weight:bold;font-size:17px;color:{c};margin-bottom:4px">{title}</div>{rows}</div>'
    html = f"""<div class=w><div class=t>العوامل المؤثرة في المناخ</div>
    <div style="display:flex;gap:26px;align-items:center">
    {col('عوامل طبيعية', nat, '#2E7D32', '#E8F5E9')}
    <div style="font-size:30px;color:#888">⟵</div>
    <div style="border:3px solid #1565C0;background:#E3F2FD;border-radius:50%;width:120px;height:120px;display:flex;align-items:center;justify-content:center;font-size:24px;font-weight:bold;color:#1565C0">المناخ</div>
    <div style="font-size:30px;color:#888">⟶</div>
    {col('عوامل بشرية', hum, '#C62828', '#FFEBEE')}
    </div></div>"""
    return 'HG_1AS_facteurs_climat', html

def chrono(name, title, events, color='#1565C0'):
    n = len(events)
    cells = ''.join(f'''<div style="flex:1;text-align:center;position:relative;padding:0 6px">
      <div style="font-weight:bold;color:{color};font-size:15px;white-space:nowrap">{d}</div>
      <div style="width:16px;height:16px;border-radius:50%;background:{color};margin:8px auto;position:relative;z-index:2;border:3px solid #fff;box-shadow:0 0 0 2px {color}"></div>
      <div style="font-size:13.5px;line-height:1.5">{e}</div></div>''' for d, e in events)
    html = f"""<div class=w style="width:{max(760, n * 165)}px"><div class=t>{title}</div>
    <div style="position:relative;display:flex">
    <div style="position:absolute;top:36px;left:0;right:0;height:4px;background:{color}"></div>{cells}</div></div>"""
    return name, html

def independance():
    return chrono('HG_3AS_independance', 'الطريق إلى الاستقلال (1958 – 1961م)', [
        ('مارس 1958م', 'مؤتمر ألاك: حزب التجمع الموريتاني'), ('28 نوفمبر 1958م', 'إعلان الجمهورية الإسلامية الموريتانية'),
        ('20 مارس 1959م', 'أول دستور للبلاد'), ('28 نوفمبر 1960م', 'إعلان الاستقلال التام'),
        ('19 يونيو 1961م', 'اتفاقيات مع فرنسا'), ('20 أغسطس 1961م', 'انتخاب المختار ولد داداه أول رئيس للجمهورية')])

def islam_mauritanie():
    return chrono('HG_2AS_islam_mauritanie', 'دخول الإسلام إلى موريتانيا', [
        ('بداية القرن 8م', 'دخول الإسلام عن طريق التجار المسلمين'),
        ('1035م', 'عبد الله بن ياسين يؤسس رباطه في جزيرة تيدره'),
        ('القرن 11م', 'الدولة المرابطية تنشر الإسلام السني على المذهب المالكي')], '#2E7D32')

def render(items):
    os.makedirs(OUT, exist_ok=True)
    with sync_playwright() as p:
        b = p.chromium.launch(); pg = b.new_page(device_scale_factor=2, viewport={'width': 1200, 'height': 800})
        for name, html in items:
            pg.set_content(f'<html><head><meta charset=utf-8><style>{CSS}</style></head><body>{html}</body></html>')
            pg.wait_for_timeout(150)
            pg.locator('.w').screenshot(path=os.path.join(OUT, name + '.png'))
        b.close()

if __name__ == '__main__':
    render([climat(), independance(), islam_mauritanie()]); print('3 رسوم')
