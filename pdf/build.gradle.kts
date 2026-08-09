plugins {
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.android)
//    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.kapt)
}

android {
    ndkVersion = "28.0.12674087"  // NDK r28c for 16KB page size support
    namespace = "com.pulleymath.android.pdf"
    compileSdk = 36

    defaultConfig {
        minSdk = 25
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            abiFilters += setOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
        externalNativeBuild {
            // For ndk-build, instead use the ndkBuild block.
            cmake {
                // Passes optional arguments to CMake.
                arguments += listOf("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    packaging {
        jniLibs {
            // 네이티브 라이브러리 압축 해제
            useLegacyPackaging = false
            // 디버그 심볼 유지
            keepDebugSymbols += "**/*.so"
        }
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // java 암호화 모듈 (TOML에 없으므로 유지)
    implementation(group = "javax.xml.bind", name = "jaxb-api", version = "2.2.12-b140109.1041")
    implementation(group = "xerces", name = "xercesImpl", version = "2.12.0")

    implementation("com.artifex.mupdf:fitz:1.26.11")

    // retrofit (버전 업데이트 됨)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.adapter.rxjava2)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
//    ksp(libs.androidx.databinding.compiler)

    // UI 및 기타 (버전 업데이트 됨)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.android.joda)

    implementation(libs.kotlinx.coroutines.android)

    // Test (버전 명시 및 업데이트 됨)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}