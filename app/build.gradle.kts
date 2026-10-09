plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ujera"

    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.ujera"

        minSdk = 24
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // AndroidX
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)

    // Material Design
    implementation(libs.material)

    // Retrofit - koneksi ke Supabase REST API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // Gson - membaca JSON dari Supabase
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Lifecycle - untuk lifecycleScope
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Unit Test
    testImplementation(libs.junit)

    // Android Test
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}