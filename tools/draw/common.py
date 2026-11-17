"""أدوات رسوم الشرح للسبورة (matplotlib) — كل رسم صورة PNG بخلفية بيضاء."""
import os, matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Polygon, Circle, Rectangle, FancyArrowPatch, Arc, Ellipse, FancyBboxPatch
import numpy as np
OUT = os.path.join(os.path.dirname(__file__), '../../app/src/teacher/assets/lessons/draw')
BLUE, RED, GREEN, ORANGE, GRAY = '#1565C0', '#C62828', '#2E7D32', '#EF6C00', '#757575'
plt.rcParams.update({'font.size': 13, 'font.family': 'DejaVu Sans', 'mathtext.fontset': 'dejavusans'})

def fig(w=6, h=3, xlim=None, ylim=None):
    f, ax = plt.subplots(figsize=(w, h), dpi=150)
    ax.set_aspect('equal'); ax.axis('off')
    if xlim: ax.set_xlim(*xlim)
    if ylim: ax.set_ylim(*ylim)
    return f, ax

def save(f, name):
    os.makedirs(OUT, exist_ok=True)
    f.savefig(os.path.join(OUT, name + '.png'), dpi=150, bbox_inches='tight', pad_inches=0.12, facecolor='white')
    plt.close(f)

def arrow(ax, p, q, color='black', lw=1.8, style='-|>', ms=14, ls='-'):
    ax.add_patch(FancyArrowPatch(p, q, arrowstyle=style, mutation_scale=ms, color=color, lw=lw, linestyle=ls, shrinkA=0, shrinkB=0))

def seg(ax, p, q, color='black', lw=1.8, ls='-'):
    ax.plot([p[0], q[0]], [p[1], q[1]], color=color, lw=lw, ls=ls, solid_capstyle='round')

def pt(ax, p, label=None, off=(0, 0.25), color='black', size=5, ha='center', fs=13):
    ax.plot(*p, 'o', color=color, ms=size)
    if label: ax.text(p[0] + off[0], p[1] + off[1], label, ha=ha, va='center', color=color, fontsize=fs)

def numline(ax, a, b, y=0, step=1, labels=None, sub=0, arrow_end=True, color='black', fmt=None):
    """مستقيم مدرّج من a إلى b."""
    seg(ax, (a - 0.4 * step, y), (b + 0.2 * step, y), color)
    if arrow_end: arrow(ax, (b + 0.1 * step, y), (b + 0.6 * step, y), color)
    v = a
    while v <= b + 1e-9:
        seg(ax, (v, y - 0.12), (v, y + 0.12), color, 1.4)
        lab = labels(v) if labels else (fmt(v) if fmt else ('%g' % v).replace('.', ','))
        ax.text(v, y - 0.38, lab, ha='center', va='center', fontsize=11)
        if sub:
            for k in range(1, sub):
                w = v + k * step / sub
                if w < b: seg(ax, (w, y - 0.06), (w, y + 0.06), color, 0.9)
        v += step

def tick_code(ax, p, q, n=1, size=0.12, color='black'):
    """علامة تساوي الأطوال على قطعة."""
    m = ((p[0] + q[0]) / 2, (p[1] + q[1]) / 2); d = np.array(q) - np.array(p); d = d / np.linalg.norm(d)
    nrm = np.array([-d[1], d[0]])
    for i in range(n):
        c = np.array(m) + d * (i - (n - 1) / 2) * 0.08
        seg(ax, c - nrm * size, c + nrm * size, color, 1.4)

def right_angle(ax, v, d1, d2, s=0.25, color='black'):
    d1 = np.array(d1) / np.linalg.norm(d1); d2 = np.array(d2) / np.linalg.norm(d2); v = np.array(v)
    ax.plot(*zip(v + d1 * s, v + d1 * s + d2 * s, v + d2 * s), color=color, lw=1.3)

def box(ax, c, w, h, text, fc='#E3F2FD', ec=BLUE, fs=12, shape='round'):
    if shape == 'diamond':
        x, y = c
        ax.add_patch(Polygon([(x - w / 2, y), (x, y + h / 2), (x + w / 2, y), (x, y - h / 2)], closed=True, fc=fc, ec=ec, lw=1.6))
    else:
        bs = 'round,pad=0.02,rounding_size=0.15' if shape == 'round' else ('round,pad=0.02,rounding_size=%g' % (h / 2) if shape == 'pill' else 'square,pad=0.02')
        ax.add_patch(FancyBboxPatch((c[0] - w / 2, c[1] - h / 2), w, h, boxstyle=bs, fc=fc, ec=ec, lw=1.6))
    ax.text(c[0], c[1], text, ha='center', va='center', fontsize=fs)
