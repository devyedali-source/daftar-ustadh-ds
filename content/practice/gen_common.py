"""أدوات مشتركة: محطات التخطيط (ما يُراجَع في كل أسبوع إدماج) وبناء السلاسل والمقترحات من بنوك المواضيع."""
import json, random
PLAN = json.load(open("../../app/src/teacher/assets/planning/planning.json"))["plans"]

def stations(subject, level):
    """لكل أسبوع eval/final/exam: الدروس التي سبقته منذ المحطة السابقة (final = السنة كلها، exam = دروس الفصل)."""
    rows = PLAN[subject][level]; out = []; since = []; term_lessons = {1: [], 2: [], 3: []}; allx = []
    for r in rows:
        if r["k"] == "lesson":
            since += r["i"]; term_lessons[r["t"]] += r["i"]; allx += r["i"]
        elif r["k"] == "eval":
            out.append(dict(week=r["l"], term=r["t"], kind="eval", label=r["i"][0], lessons=list(dict.fromkeys(since)) or list(dict.fromkeys(allx))))
            since = []
        elif r["k"] == "final":
            out.append(dict(week=r["l"], term=r["t"], kind="final", label=r["i"][0], lessons=list(dict.fromkeys(allx))))
        elif r["k"] == "exam":
            out.append(dict(week=r["l"], term=r["t"], kind="exam", label=r["i"][-1], lessons=list(dict.fromkeys(term_lessons[r["t"]]))))
    return out

def draw(bank_items, n, seed, used, partial=False):
    """n عنصر مختلف من قائمة عناصر/قوالب (القالب دالة تأخذ مولّدًا عشوائيًا)."""
    r = random.Random(seed); out = []; pool = list(bank_items); r.shuffle(pool); i = 0; guard = 0
    while len(out) < n and guard < 5000:
        guard += 1
        it = pool[i % len(pool)]; i += 1
        x = it(random.Random(r.randint(0, 10**9))) if callable(it) else it
        if x["text"] in used: continue
        used.add(x["text"]); out.append(x)
    assert partial or len(out) == n, f"bank too small ({len(out)}/{n})"
    return out

def draw_fill(primary, fallback, n, seed, used):
    """من بنك المحطة أولًا، ثم يُكمَّل من الدروس السابقة إن لم يكفِ."""
    u = set(used); a = draw(primary, n, seed, u, partial=True)
    if len(a) < n: a += draw([x for x in fallback if x not in primary], n - len(a), seed + 1, u)
    used.update(x["text"] for x in a); return a

def exam_props(pools, n, seed, title_fmt, ex_word="Exercice"):
    """n مقترح امتحان، كل مقترح يأخذ عنصرًا من كل مجموعة (pools)، دون تكرار نفس التركيبة."""
    r = random.Random(seed); props = []; seen = set(); guard = 0
    while len(props) < n and guard < 5000:
        guard += 1
        parts = []
        for p in pools:
            it = r.choice(p); parts.append(it(random.Random(r.randint(0, 10**9))) if callable(it) else it)
        if len({p["text"] for p in parts}) < len(parts): continue
        key = "|".join(sorted(p["text"] for p in parts))
        if key in seen: continue
        seen.add(key); k = len(props) + 1
        props.append({"title": title_fmt.format(k),
                      "text": "\n\n".join(f"{ex_word} {i+1} : {p['title']}\n{p['text']}" for i, p in enumerate(parts)),
                      "solution": "\n\n".join(f"{ex_word} {i+1}\n{p['solution']}" for i, p in enumerate(parts))})
    assert len(props) == n, f"exam pools too small ({len(props)})"
    return props

def save(subject, level, lang, series, exams):
    for b in series + exams: assert len(b["items"]) == 20, (b["week"], len(b["items"]))
    json.dump({"subject": subject, "level": level, "lang": lang, "series": series, "exams": exams},
              open(f"../../app/src/teacher/assets/practice/{subject}_{level}.json", "w"), ensure_ascii=False, indent=1)
    print(subject, level, "series", len(series), "exams", len(exams))
