plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.project.kakao_login"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.project.kakao_login"
        minSdk = 33
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "34.0.0"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.services.wearable)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.appcompat)
    //    implementation("androidx.appcompat:appcompat:1.7.0")
    //    implementation("androidx.compose.ui:ui:1.4.0")
    //    implementation("androidx.compose.material:material:1.4.0")
    //    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    //    implementation("androidx.activity:activity-compose:1.6.1")
    //    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0")
    //    implementation("androidx.activity:activity-compose:1.3.1")
    //    implementation("androidx.compose.foundation:foundation:1.0.5")
    //    implementation("androidx.compose.material:material:1.0.5")
    //    implementation("androidx.compose.ui:ui-tooling:1.0.5")
}