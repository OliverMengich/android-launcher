plugins {
    kotlin("plugin.serialization") version "2.1.21"
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.android_launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.android_launcher"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val apiKey = project.findProperty("API_KEY") as String? ?: "default_key"
        val secretToken = project.findProperty("SECRET_TOKEN") as String? ?: ""
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        buildConfigField("String", "SECRET_TOKEN", "\"$secretToken\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        compose = true
        buildConfig = true
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
    implementation(libs.material3)
    implementation(libs.androidx.lifecycle.service)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Navigation 3
    implementation(libs.androidx.navigation.compose)

    //Koin
    implementation(libs.koin.androidx.compose)

    //Splash screen
    implementation(libs.androidx.core.splashscreen)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    //Google cloud credentials
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    //Preference Datastore
    implementation(libs.androidx.datastore.preferences)

    //Datastore
    implementation(libs.androidx.datastore)

    //Serialization Json
    implementation(libs.kotlinx.serialization.json)


    //Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)

    //Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    //compose rich editor
    implementation(libs.richeditor.compose)
}