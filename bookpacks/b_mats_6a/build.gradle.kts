// حزمة كتاب واحد (مادة + مستوى): تُنزَّل من المتجر عند الطلب فقط.
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
