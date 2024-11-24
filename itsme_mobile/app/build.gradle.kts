import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleGmsGoogleServices)
}


android {
    namespace = "com.project.kakao_login"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.project.kakao_login"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.kakao.sdk:v2-user:2.20.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.squareup.retrofit2:retrofit:2.6.1")    // request 모듈
    implementation ("com.squareup.retrofit2:converter-gson:2.6.1")

    implementation("com.faendir.rhino:rhino-android:1.6.0")
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation("com.google.firebase:firebase-messaging:23.1.0")

    implementation("androidx.cardview:cardview:1.0.0") // CardView 의존성 추가
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.tbuonomo:dotsindicator:4.3")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.kakao.sdk:v2-share:2.19.0")

    implementation("com.kakao.sdk:v2-talk:2.19.0")
    implementation("com.kakao.sdk:v2-common:2.19.0")
}