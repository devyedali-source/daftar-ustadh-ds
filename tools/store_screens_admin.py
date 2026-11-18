"""لقطات متجر «إدارة DS» (1080×1920): عنوان تسويقي + واجهة التطبيق كما هي (نفس الألوان والنصوص)."""
import os
from playwright.sync_api import sync_playwright
OUT = os.path.join(os.path.dirname(__file__), '../store/admin/screens')
CSS = """
*{box-sizing:border-box;margin:0;padding:0}
body{width:1080px;height:1920px;font-family:'Noto Naskh Arabic','Noto Sans Arabic','DejaVu Sans',sans-serif;direction:rtl;
 background:linear-gradient(160deg,#0D47A1,#1E88E5);overflow:hidden}
.cap{height:330px;display:flex;flex-direction:column;justify-content:center;align-items:center;color:#fff;text-align:center;padding:0 60px}
.cap h1{font-size:68px;font-weight:900;line-height:1.25}.cap p{font-size:36px;opacity:.92;margin-top:14px}
.phone{position:absolute;top:360px;left:90px;width:900px;height:1640px;background:#FDF8F6;border-radius:56px;overflow:hidden;
 box-shadow:0 30px 80px rgba(0,0,0,.35);border:14px solid #10172a}
.bar{background:#6750A4;color:#fff;height:150px;display:flex;align-items:center;padding:0 40px;gap:24px;font-size:38px;font-weight:800}
.bar .ic{width:76px;height:76px;border-radius:50%;background:#fff url(icon.png) center/cover}
.body{padding:30px 34px;display:flex;flex-direction:column;gap:22px}
.card{background:#fff;border-radius:34px;padding:30px 32px;box-shadow:0 3px 10px rgba(0,0,0,.08)}
.t{font-size:36px;font-weight:900;color:#1D1B20}.s{font-size:28px;color:#49454F;margin-top:8px;line-height:1.5}
.row{display:flex;align-items:center;gap:22px}
.n{width:58px;height:58px;border-radius:50%;background:#6750A4;color:#fff;font-weight:900;font-size:30px;display:flex;align-items:center;justify-content:center;flex:none}
.ok{background:#2E7D32}.done{background:#E8F5E9}.var{background:#F3EDF7}
.chip{display:inline-block;padding:8px 22px;border-radius:30px;font-size:26px;font-weight:700}
.green{color:#2E7D32;background:#E8F5E9}.orange{color:#E65100;background:#FFF3E0}.purple{color:#6750A4;background:#EADDFF}
table{width:100%;border-collapse:collapse;font-size:29px}td,th{padding:14px 10px;border-bottom:1px solid #E7E0EC;text-align:center}
th{color:#6750A4;font-weight:900}td.l{text-align:right}
.btn{background:#6750A4;color:#fff;border-radius:40px;text-align:center;padding:26px;font-size:32px;font-weight:800}
.stat{flex:1;text-align:center;background:#EADDFF;border-radius:28px;padding:24px 0}.stat b{display:block;font-size:52px;color:#6750A4}.stat span{font-size:26px;color:#49454F}
"""
def page(cap, sub, title, body):
    return f'<div class=cap><h1>{cap}</h1><p>{sub}</p></div><div class=phone><div class=bar><span>→</span><span>☰</span><div class=ic></div><span>{title}</span></div><div class=body>{body}</div></div>'

def step(n, t, s, cls='', ok=False):
    return f'<div class="card {cls}"><div class=row><div class="n {"ok" if ok else ""}">{"✓" if ok else n}</div><div><div class=t>{t}</div><div class=s>{s}</div></div></div></div>'

S1 = page('كل إدارة المؤسسة في هاتفك', 'الأقسام والتلاميذ والنتائج والكشوف', 'إعدادية النجاح — <bdi dir=ltr>2026-2027</bdi>',
 '<div class=row style="gap:18px"><div class=stat><b>14</b><span>الأقسام</span></div><div class=stat><b>486</b><span>التلاميذ</span></div><div class=stat><b>22</b><span>الأساتذة</span></div></div>'
 + '<div class=t style="color:#6750A4;margin-top:6px">📋 ترتيب العمل</div>'
 + step(1, '👨‍🏫 الأساتذة', 'مسجّلون: 22', 'done', True) + step(2, '🏫 الأقسام', 'مسجّلة: 14', 'done', True)
 + step(3, '📅 الجدول الزمني', 'معتمد', 'done', True) + step(4, '📤 مشاركة سجلات الأساتذة', 'ملف واحد لكل أستاذ عبر واتساب', 'var')
 + step(5, '📥 ملفات نتائج الأساتذة', 'ارفعها كلها بضغطة زر', 'var'))

rows = ''.join(f'<tr><td class=l>{m}</td><td>{t}</td><td><span class="chip {c}">{s}</span></td></tr>' for m, t, c, s in [
 ('الرياضيات', 'أحمد سالم', 'green', 'مكتملة'), ('اللغة العربية', 'مريم محمد', 'green', 'مكتملة'), ('الفيزياء', 'سيدي أحمد', 'green', 'مكتملة'),
 ('العلوم الطبيعية', 'فاطمة الزهراء', 'orange', 'تنقص 3'), ('التربية الإسلامية', 'محمد الأمين', 'green', 'مكتملة'),
 ('التاريخ والجغرافيا', 'عائشة أحمد', 'green', 'مكتملة'), ('الفرنسية', 'خديجة سيدي', 'orange', 'لم تصل'), ('الإنجليزية', 'يحيى عبد الله', 'green', 'مكتملة')])
S2 = page('نتائج كل قسم في نظرة واحدة', 'ما ينقص فقط، ومن تأخر من الأساتذة', '3AS2 — النتائج',
 '<div class=row style="gap:14px"><span class="chip purple">الفصل 1</span><span class="chip var" style="color:#49454F">الفصل 2</span><span class="chip var" style="color:#49454F">الفصل 3</span></div>'
 '<div class=card><table><tr><th style="text-align:right">المادة</th><th>الأستاذ</th><th>الحالة</th></tr>' + rows + '</table></div>'
 '<div class=btn>⚡ إدخال سريع بأرقام النداء</div><div class=btn style="background:#fff;color:#6750A4;border:3px solid #CAC4D0">📥 رفع ملفات الأساتذة</div>')

g = ''.join(f'<tr><td>{i}</td><td class=l>{n}</td><td><b style="color:#6750A4">{v}</b></td></tr>' for i, n, v in [
 (1, 'آمنة أحمد', '15,5'), (2, 'إبراهيم محمد', '12'), (3, 'أم كلثوم سيدي', '17,25'), (4, 'الحسن عبد الله', '9,5'), (5, 'خديجة محمد الأمين', '14'),
 (6, 'زينب أحمد سالم', '16'), (7, 'سيدي محمد', '11,75'), (8, 'عائشة يحيى', '13,5'), (9, 'محمد المختار', '10')])
S3 = page('إدخال النتائج يدويًا بسرعة', 'بأرقام النداء، أو بتصوير ورقة النقاط', 'إدخال سريع بأرقام النداء',
 '<div class=row style="gap:14px;flex-wrap:wrap"><span class="chip purple">الفصل 1</span><span class="chip purple">الرياضيات</span><span class="chip purple">الامتحان</span></div>'
 '<div class=row style="gap:18px"><div class=btn style="flex:1">📷 تصوير الورقة</div><div class=btn style="flex:1;background:#fff;color:#6750A4;border:3px solid #CAC4D0">🖼️ صورة من الهاتف</div></div>'
 '<div class=card><table><tr><th>رقم النداء</th><th style="text-align:right">الاسم</th><th>النقطة</th></tr>' + g + '</table></div><div class=btn>💾 حفظ 9 نقاط</div>')

k = ''.join(f'<tr><td class=l>{m}</td><td>{c}</td><td>{v}</td><td>{v2}</td><td style="font-size:24px">{r}</td></tr>' for m, c, v, v2, r in [
 ('الرياضيات', 4, '15,50', '62,00', 'Bien'), ('اللغة العربية', 3, '14,00', '42,00', 'جيد'), ('الفيزياء', 3, '13,25', '39,75', 'Assez bien'),
 ('العلوم الطبيعية', 3, '16,00', '48,00', 'Très bien'), ('التربية الإسلامية', 2, '17,00', '34,00', 'جيد جدا'), ('الفرنسية', 3, '12,50', '37,50', 'Assez bien')])
S4 = page('كشوف الدرجات جاهزة للطباعة', 'المعدلات والرتب تُحسب تلقائيًا وفق الضوارب الرسمية', 'كشوف الدرجات — 3AS2',
 '<div class=card style="padding:26px"><div style="text-align:center;font-size:24px;color:#49454F">الجمهورية الإسلامية الموريتانية<br>شرف - إخاء - عدل</div>'
 '<div class=t style="text-align:center;margin:16px 0;color:#0D47A1">كشف درجات الفصل الأول</div>'
 '<div class=s style="text-align:center">التلميذة: آمنة أحمد — القسم: 3AS2 — رقم النداء: 1</div>'
 '<table style="font-size:26px;margin-top:14px"><tr><th style="text-align:right">المادة</th><th>الضارب</th><th>المعدل</th><th>المجموع</th><th>الملاحظة</th></tr>' + k + '</table>'
 '<div class=row style="justify-content:space-around;margin-top:22px"><span class="chip purple" style="font-size:30px">المعدل: 14,77</span><span class="chip green" style="font-size:30px">الرتبة: 3 / 38</span></div></div>'
 '<div class=btn>📄 إنتاج كشوف القسم (PDF)</div><div class=btn style="background:#2E7D32">📤 إرسال كشوف الدرجات</div>')

if __name__ == '__main__':
    import shutil; shutil.copy(os.path.join(OUT, '../icon-512.png'), os.path.join(OUT, 'icon.png'))
    with sync_playwright() as p:
        b = p.chromium.launch(); pg = b.new_page(viewport={'width': 1080, 'height': 1920})
        for i, h in enumerate([S1, S2, S3, S4], 1):
            pg.goto('file://' + os.path.abspath(OUT) + '/x.html') if False else None
            open(os.path.join(OUT, 'tmp.html'), 'w').write(f'<html><head><meta charset=utf-8><style>{CSS}</style></head><body>{h}</body></html>')
            pg.goto('file://' + os.path.abspath(os.path.join(OUT, 'tmp.html'))); pg.wait_for_timeout(200)
            pg.screenshot(path=os.path.join(OUT, f'screen-{i}.png'))
        b.close()
    os.remove(os.path.join(OUT, 'tmp.html')); os.remove(os.path.join(OUT, 'icon.png'))
    print('4 لقطات')
