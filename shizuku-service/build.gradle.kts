plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.vectorpatch.shizuku"
    compileSdk = 35
    defaultConfig { minSdk = 28 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { aidl = true }
}

dependencies {
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
}
