plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")
    alias(libs.plugins.google.service)
    id("com.google.firebase.crashlytics")

}

android {

    signingConfigs {
        create("release") {
            keyAlias = "pulley_v2"
            keyPassword = "<REDACTED>"
            storeFile = file("./key/pulley_v2.jks")

            storePassword = "<REDACTED>"
        }
//        config {
//            keyAlias = "pulley_v2"
//            keyPassword = "<REDACTED>"
//            storeFile = file("./key/pulley_v2.jks")
//
//            storePassword = "<REDACTED>"
//        }
    }
    namespace = "com.freewheelin.pulley"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.freewheelin.pulley"
        minSdk = 25
        targetSdk = 35
        versionCode = 539
        versionName = "1.6.119"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            isRenderscriptDebuggable = true
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            resValue("string", "PORT_NUMBER", "8089")
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
//    dataBinding {
//        isEnabled = true
//    }
//    viewBinding {
//        isEnabled = true
//    }

    flavorDimensions.add("type")

    productFlavors {
        create("prod") {
            dimension = "type"

            resValue("string", "app_name", "풀리수학")
            resValue("string", "facebook_app_id", "1045837815850615")
            resValue("string", "provider_id", "com.freewheelin.fileprovider")
        }

        create("beta") {
            applicationIdSuffix = ".beta"

            dimension = "type"

            resValue("string", "app_name", "풀리수학-베타")
            resValue("string", "facebook_app_id", "3498422050272145")
            resValue("string", "provider_id", "com.freewheelin.beta.fileprovider")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    lint {
        abortOnError = false
    }

}

//repositories {
//    mavenCentral()
//    google()
//}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "includes" to listOf("*.jar"))))

    implementation(libs.androidx.core.ktx)
    implementation(libs.org.jetbrain.kotlin.reflect)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    // rx
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)
    implementation(libs.rxbinding.kotlin)
    implementation(libs.rxbinding.appcompat.v7.kotlin)
    implementation(libs.rxbinding.recyclerview.v7.kotlin)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.adapter.rxjava2)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.okhttp3.urlconnection)

    implementation(libs.picasso)
    implementation(libs.mp.androidchart)

    implementation(libs.androidx.cardview)
    implementation(libs.material)

    implementation(libs.android.joda)
    implementation(libs.glide)
    implementation(libs.androidx.activity)
    kapt(libs.glide.compiler)

    implementation(libs.androidx.viewpager2)

    implementation(libs.kotlin.stdlib)

    implementation(libs.tedpermission.normal)

    implementation(libs.lottie)
    implementation(libs.android.recycleradapters)

    // 채널톡에서 사용함
    implementation(libs.flexbox)

    // 파이어베이스
    implementation(platform(libs.google.firebase))
    implementation(libs.google.firebase.crashlytics)
    implementation(libs.google.firebase.analytics)
    implementation(libs.google.firebase.messaging)

    // facebook sdk
    implementation(libs.facebook.android.sdk)

    implementation(libs.asset.delivery.ktx)
    implementation(libs.app.update.ktx)
    implementation(libs.feature.delivery.ktx)




    // pdf
    implementation(project(":pdf"))

    //jsoup
    implementation(libs.jsoup)

    //paging ktx
    val paging_version = "3.1.1"
    implementation(libs.androidx.paging.runtime.ktx)
    testImplementation(libs.androidx.paging.common.ktx)
    implementation(libs.androidx.paging.rxjava2.ktx)

    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.viewmodel.livedata)
    implementation(libs.androidx.fragment)

    // exoplayer
    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.dash)
    implementation(libs.exoplayer.ui)

    kapt(libs.databinding.compiler)

    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.rxjava2)
    kapt(libs.androidx.room.compiler)
    kapt(libs.sqlite.jdbc)

    implementation(libs.channel.talk.plugin.android)
    implementation(libs.channel.talk.plugin.android.fcm)

    implementation(libs.androidx.browser)

    implementation(libs.installreferrer)

//    implementation("androidx.core:core-splashscreen:1.1.0-alpha01"
    // lesson pagecall
    // host 처리 문제로 사용안함
    // implementation("com.github.pplink.pagecall-android-sdk:sdk:0.0.3"

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("org.mockito:mockito-core:3.9.0")
//    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.robolectric:robolectric:4.1")
    testImplementation("joda-time:joda-time:2.10.1")
    testImplementation("org.mockito:mockito-inline:2.13.0")

    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

    debugImplementation("com.github.amitshekhariitbhu.Android-Debug-Database:debug-db:v1.0.6")
}

