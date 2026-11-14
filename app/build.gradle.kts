plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.elyedali.daftarustadh"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.elyedali.daftarustadh"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // تطبيقان من مشروع واحد: «إدارة DS» (وضع الإدارة) و«دفتر الأستاذ DS» (وضع الأستاذ)
    // الإدارة تحتفظ بالمعرّف القديم فتبقى بيانات النسخ المثبتة؛ والأستاذ تطبيق جديد يُثبَّت بجانبها.
    flavorDimensions += "role"
    productFlavors {
        create("admin") {
            dimension = "role"
            applicationId = "com.elyedali.daftarustadh"
            buildConfigField("String", "ROLE", "\"admin\"")
        }
        create("teacher") {
            dimension = "role"
            applicationId = "com.elyedali.daftarustadh.teacher"
            buildConfigField("String", "ROLE", "\"teacher\"")
        }
    }

    androidResources {
        generateLocaleConfig = false
    }

    // صور الكتب في حزمة منفصلة «books» تنزّلها المتجر تلقائيًا بعد التثبيت (لتطبيق الأستاذ فقط).
    // نسخة التجربة (APK من GitHub) بلا صور الكتب حتى يصغر تنزيلها (الإنترنت ضعيف)؛ تُضاف في نسخة المتجر.
    val buildingAdmin = gradle.startParameter.taskNames.any { it.contains("Admin") }
    if (!buildingAdmin) assetPacks += ":books"

    // R8: تصغير الشيفرة وحذف الموارد غير المستعملة (يُطبَّق على النسخة التي يبنيها GitHub)
    // مفتاح ثابت لنسخ التجربة: بصمته SHA-1 مسجلة في Firebase فيعمل الدخول بحساب Google في كل بناء
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            // نسخة التجربة لهواتف أندرويد الحقيقية فقط (أصغر حجمًا)؛ نسخة المتجر يقسّمها المتجر لكل هاتف تلقائيًا
            ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // قاعدة البيانات المحلية الخاصة بدفتر الأستاذ DS
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // تنزيل حزمة الكتب من المتجر ومتابعة تقدّمها (تطبيق الأستاذ)
    implementation("com.google.android.play:asset-delivery:2.2.2")
    // تنبيه التحديث من متجر Google Play (كما في دفتر المعلم)
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // تدوير صور الطلاب حسب اتجاه الكاميرا
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // الخادم: الدخول بحساب Google وقاعدة البيانات (مشروع Firebase خاص بـ DS)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // أمثلة الكشوف (اختبار على المحاكي فقط، لا يدخل في التطبيق)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}
