// Ortak (Android + iOS) çekirdek: veri modeli, film/dizi kataloğu, çark havuzu, arama.
// iOS hedefleri yalnızca macOS'ta derlenir (GitHub Actions: .github/workflows/ios-build.yml).
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
  androidLibrary {
    namespace = "com.example.shared"
    compileSdk = 36
    minSdk = 24
  }

  // Linux'ta ortak testleri hızlı çalıştırmak için
  jvm()

  listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
      baseName = "Shared"
      isStatic = true
    }
  }

  sourceSets {
    commonTest.dependencies {
      implementation(kotlin("test"))
    }
  }
}
