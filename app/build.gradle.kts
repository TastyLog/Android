import org.jetbrains.kotlin.ir.backend.js.compile

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.googleGmsServices) // Google services 플러그인 추가



}

android {
    namespace = "com.adam.tastylog"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.adam.tastylog"
        minSdk = 28
        targetSdk = 34
        versionCode = 18
//        versionName = "1.0.1-beta"
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false // default == false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false // default == false
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
        viewBinding = true
    } // 뷰 바인딩
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.naver.map.sdk)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.androidyoutubeplayer)
    implementation(libs.circleImageView)
    implementation(libs.play.services.location)
    implementation(libs.lottie)
    implementation(libs.gson)
    implementation(libs.glide)
    implementation(libs.glideCompiler)
    implementation(libs.gson.converter)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.logging.interceptor)
    implementation(libs.shimmer)
    implementation (libs.core.splashscreen)
    implementation(libs.kakao.sdk.share)
    implementation(libs.timber)

    implementation(platform(libs.firebaseBom)) // Firebase BoM
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation(libs.play.services.ads)
    implementation(libs.simpleratingbar)

}
