pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "DaftarUstadhDS"
include(":app")
// حزم الكتب: حزمة لكل (مادة، مستوى) في bookpacks/ تُنزَّل من المتجر عند الطلب (tools/books/packs.py)
file("bookpacks").listFiles { f -> f.isDirectory && File(f, "build.gradle.kts").exists() }
    ?.sortedBy { it.name }
    ?.forEach { include(":${it.name}"); project(":${it.name}").projectDir = it }
