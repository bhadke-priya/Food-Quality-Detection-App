import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.navigation)

}

android {
    namespace = "com.example.eatsure"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.eatsure"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // ... other configurations

        // Load API key from local.properties
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String", "GEMINI_API_KEY", "\"${properties.getProperty("GEMINI_API_KEY")}\"")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx) // Changed from androidx-navigation-fragment-ktx
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.legacy.support.v4)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.barcode.scanner)

    //show ingredients info
    implementation(libs.fragment)
    implementation(libs.retro.fit)
    implementation(libs.retrofit.gson)
    implementation(libs.glide)
    implementation(libs.cardview)
    implementation(libs.material.floating)

    implementation(libs.room.runtime)
    implementation(libs.room)
    annotationProcessor(libs.roomCompiler) // Replace kapt(libs.roomCompiler)
    implementation(libs.coil)

    implementation(libs.okhttp)
    implementation(libs.gson)
//    implementation(libs.logging.interceptor)
    // Firebase BoM - manages all Firebase library versions
    implementation(platform(libs.firebase.bom))

    // Firebase Authentication
    implementation(libs.firebase.auth.ktx)

    // Firebase Realtime Database
    implementation(libs.firebase.database.ktx)
    // For pdf download
    implementation(libs.text.pdf)

//    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines)

    implementation(libs.google.api)

    implementation(libs.playstore)
    implementation(libs.playservice)

//    implementation(libs.phil.jay)
}