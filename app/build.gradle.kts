plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.hasanzade.calixy_mobile"
    compileSdk = 36

    buildFeatures{
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.hasanzade.calixy_mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --------------------
    // CameraX (Image + Video Capture)
    // --------------------
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")
    implementation("androidx.camera:camera-video:1.3.3")

    // --------------------
    // Image / Video Processing
    // --------------------
    implementation("com.github.bumptech.glide:glide:4.17.0")
    implementation("org.bytedeco:javacv-platform:1.5.8") // Video frame extraction

    // --------------------
    // Networking (Retrofit + OkHttp)
    // --------------------
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")


    // --------------------
    // Coroutines
    // --------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --------------------
    // DI (Hilt)
    // --------------------
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0-alpha01")

    // --------------------
    // Navigation (Compose + Graph)
    // --------------------
    implementation("androidx.navigation:navigation-compose:2.7.3")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0-alpha01")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.3")

    // --------------------
    // ViewModel + LiveData
    // --------------------
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.3")



    // --------------------
    // Subscription / Billing
    // --------------------
    implementation("com.android.billingclient:billing-ktx:6.1.0")


    // Kotlin Coroutines (DataStore üçün)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
// Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.0-alpha06")

    implementation ("com.google.firebase:firebase-bom:32.2.2")
    implementation ("com.google.firebase:firebase-auth-ktx")







}