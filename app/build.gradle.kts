plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
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

    buildTypes {
        release {
            isMinifyEnabled = false
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

    // قراءة أرقام النداء والنقاط من صورة ورقة الأستاذ (على الهاتف، بلا إنترنت)
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // تدوير صور الطلاب حسب اتجاه الكاميرا
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // أمثلة الكشوف (اختبار على المحاكي فقط، لا يدخل في التطبيق)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}
