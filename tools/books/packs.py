#!/usr/bin/env python3
"""حزم كتب تطبيق الأستاذ: حزمة مستقلة لكل (مادة، مستوى) تُنزَّل عند الطلب من المتجر.

اسم ملف الكتاب: <المادة>_<المستوى>[_<جزء>].pdf  مثل MATS_1AS.pdf أو HG_1AS_GEO.pdf
في الثانوية يكون المستوى رمز الشعب التي يخدمها الكتاب: MATS_5C.pdf أو AR_5CD.pdf أو IR_5ACDO.pdf (كتاب مشترك = حزمة واحدة).
فيذهب إلى الحزمة b_<مادة>_<مستوى> (بحروف صغيرة)، والأجزاء (التاريخ/الجغرافيا) في نفس الحزمة.

الاستعمال:
  python3 tools/books/packs.py make [ملفات PDF أو مجلدات...]
      ينقل الكتب إلى bookpacks/<الحزمة>/src/main/assets/books/ ويكتب ملف الحزمة وفهرس التطبيق.
      بلا وسائط: ينقل ما في books_incoming/ (مكان وضع الكتب الجديدة، مثل كتب الثانوية).
  python3 tools/books/packs.py check
      يتأكد أن الفهرس يطابق الكتب الموجودة فعلًا (يُشغَّل قبل كل بناء للمتجر).

حد المتجر: 100 حزمة على الأكثر لكل تطبيق، و1.5 جيغا للحزمة الواحدة.
"""
import json, os, re, shutil, subprocess, sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
PACKS = os.path.join(ROOT, "bookpacks")
INCOMING = os.path.join(ROOT, "books_incoming")
INDEX = os.path.join(ROOT, "app", "src", "teacher", "assets", "book_packs.json")
NAME_RE = re.compile(r"^([A-Z]+)_([0-9A-Z]+)(?:_([A-Z0-9]+))?\.pdf$")
MAX_PACKS = 100
MAX_PACK_BYTES = 1_500_000_000

GRADLE = """// حزمة كتاب واحد (مادة + مستوى): تُنزَّل من المتجر عند الطلب فقط.
// أُنشئت بـ tools/books/packs.py — لا تُعدَّل يدويًا.
plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set(project.name)
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
"""


def pack_of(filename):
    m = NAME_RE.match(filename)
    if not m:
        sys.exit(f"اسم غير صالح: {filename} (المطلوب مثل MATS_1AS.pdf أو HG_1AS_GEO.pdf)")
    return f"b_{m.group(1).lower()}_{m.group(2).lower()}", m.group(1), m.group(2)


def tracked(path):
    r = subprocess.run(["git", "ls-files", "--error-unmatch", path], cwd=ROOT, capture_output=True)
    return r.returncode == 0


def move(src, dst):
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    if os.path.exists(dst):
        sys.exit(f"موجود من قبل: {os.path.relpath(dst, ROOT)} (احذفه أولًا إن كان الكتاب الجديد يعوّضه)")
    if tracked(src):
        subprocess.run(["git", "mv", src, dst], cwd=ROOT, check=True)
    else:
        shutil.move(src, dst)


def scan():
    """الحزم الموجودة: {name: {subject, level, files:[(asset_path, bytes)]}}"""
    out = {}
    if not os.path.isdir(PACKS):
        return out
    for name in sorted(os.listdir(PACKS)):
        d = os.path.join(PACKS, name, "src", "main", "assets", "books")
        if not os.path.isdir(d):
            continue
        files = sorted(f for f in os.listdir(d) if f.endswith(".pdf"))
        if not files:
            sys.exit(f"حزمة بلا كتب: {name}")
        for f in files:
            p, s, l = pack_of(f)
            if p != name:
                sys.exit(f"{f} في الحزمة الخطأ {name} (مكانه {p})")
        _, subj, lvl = pack_of(files[0])
        out[name] = {"subject": subj, "level": lvl,
                     "files": [("books/" + f, os.path.getsize(os.path.join(d, f))) for f in files]}
        if not os.path.isfile(os.path.join(PACKS, name, "build.gradle.kts")):
            sys.exit(f"ملف الحزمة ناقص: bookpacks/{name}/build.gradle.kts")
    return out


LYCEE_KEY = re.compile(r"^([567])([ACDO]+)$")


def levels_of(key):
    """رمز كتاب الثانوية المشترك يُفكّ إلى شعبه: 5CD → 5C و5D، و5ACDO → كل شعب الخامسة."""
    m = LYCEE_KEY.match(key)
    return [m.group(1) + c for c in m.group(2)] if m else [key]


def index_data(packs):
    out = []
    for n, v in sorted(packs.items()):
        e = {"name": n, "subject": v["subject"], "level": v["level"],
             "files": [p for p, _ in v["files"]], "bytes": sum(b for _, b in v["files"])}
        lv = levels_of(v["level"])
        if lv != [v["level"]]:
            e["levels"] = lv
        out.append(e)
    return {"packs": out}


def limits(packs):
    if len(packs) > MAX_PACKS:
        sys.exit(f"عدد الحزم {len(packs)} يتجاوز حد المتجر ({MAX_PACKS})")
    for n, v in packs.items():
        if sum(b for _, b in v["files"]) > MAX_PACK_BYTES:
            sys.exit(f"الحزمة {n} أكبر من 1.5 جيغا")


def make(args):
    sources = []
    for a in (args or [INCOMING]):
        a = os.path.abspath(a)
        if os.path.isdir(a):
            sources += [os.path.join(a, f) for f in sorted(os.listdir(a)) if f.endswith(".pdf")]
        elif a.endswith(".pdf"):
            sources.append(a)
    for src in sources:
        name, _, _ = pack_of(os.path.basename(src))
        base = os.path.join(PACKS, name)
        move(src, os.path.join(base, "src", "main", "assets", "books", os.path.basename(src)))
        g = os.path.join(base, "build.gradle.kts")
        if not os.path.exists(g):
            with open(g, "w", encoding="utf-8") as fh:
                fh.write(GRADLE)
        print(f"{os.path.basename(src)} → {name}")
    packs = scan()
    limits(packs)
    os.makedirs(os.path.dirname(INDEX), exist_ok=True)
    with open(INDEX, "w", encoding="utf-8") as fh:
        json.dump(index_data(packs), fh, ensure_ascii=False, indent=1)
        fh.write("\n")
    total = sum(sum(b for _, b in v["files"]) for v in packs.values())
    print(f"{len(packs)} حزمة، {total / 1e6:.1f} ميغا — الفهرس: {os.path.relpath(INDEX, ROOT)}")


def check():
    packs = scan()
    limits(packs)
    try:
        with open(INDEX, encoding="utf-8") as fh:
            current = json.load(fh)
    except FileNotFoundError:
        sys.exit("الفهرس غير موجود: شغّل packs.py make")
    if current != index_data(packs):
        sys.exit("الفهرس لا يطابق الكتب: شغّل packs.py make")
    print(f"الفهرس مطابق: {len(packs)} حزمة")


if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] not in ("make", "check"):
        sys.exit(__doc__)
    make(sys.argv[2:]) if sys.argv[1] == "make" else check()
