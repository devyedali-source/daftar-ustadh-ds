# رسوم الشرح للسبورة

رسوم تُضاف إلى السبورة للدروس التي تحتاج رسما، حتى لو لم يكن في الكتاب.

- `mats.py` : رسوم الرياضيات (matplotlib).
- `autres.py` : التكنولوجيا (مخططات السير)، الفيزياء، العلوم.
- `arabe.py` : الرسوم العربية (تُصوَّر بمتصفح Chromium لسلامة الحروف).
- `carte.txt` : أي رسم يوضع في أي درس وأي قسم (`+قسم` = قسم جديد).
- `appliquer.py` : يضع الرسوم في ملفات الدروس (حقل `draw`).

الصور في `app/src/teacher/assets/lessons/draw/`.
إعادة التوليد: `python3 mats.py && python3 autres.py && python3 arabe.py && python3 appliquer.py`
