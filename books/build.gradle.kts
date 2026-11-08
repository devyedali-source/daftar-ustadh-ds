// حزمة صور الكتب: تُنزَّل تلقائيًا من المتجر بعد تثبيت تطبيق الأستاذ (fast-follow)،
// فيصغر التطبيق نفسه وتبقى الكتب تعمل بلا إنترنت بعد تحميلها
plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("books")
    dynamicDelivery {
        deliveryType.set("fast-follow")
    }
}
