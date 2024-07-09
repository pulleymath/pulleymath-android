plugins {
//    alias(libs.plugins.android.library)
    id("com.android.library")
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")
}

//group = 'com.pulleymath.android.pdf'
//version = '0.0.1'

android {
    namespace = "com.pulleymath.android.pdf"
    compileSdk = 34

    defaultConfig {
        minSdk = 25
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
//    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    // java 암호화 모듈
//    implementation group: 'javax.xml.bind', name: 'jaxb-api', version: '2.2.12-b140109.1041'
    implementation(group = "javax.xml.bind", name = "jaxb-api", version = "2.2.12-b140109.1041")

//    implementation group: 'xerces', name: 'xercesImpl', version: '2.12.0'
    implementation(group = "xerces", name = "xercesImpl", version = "2.12.0")

    implementation("com.artifex.mupdf:fitz:1.18.0")
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.4.0")
    implementation("com.squareup.retrofit2:converter-gson:2.4.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.10.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.4.0")
    // Room
    val room_version = "2.3.0"

    implementation("androidx.room:room-runtime:${room_version}")
    annotationProcessor("androidx.room:room-compiler:${room_version}")

    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:${room_version}")
    kapt("org.xerial:sqlite-jdbc:3.34.0")
    // Gson
    implementation("com.squareup.retrofit2:converter-gson:2.4.0")
    
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("net.danlew:android.joda:2.10.1.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

//repositories {
//    mavenCentral()
//}