plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.project.stampy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.project.stampy"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.google.android.material:material:1.11.0")  // Material Design
    implementation ("androidx.fragment:fragment-ktx:1.6.2") // Fragment
    implementation ("androidx.recyclerview:recyclerview:1.3.2") // RecyclerView
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")  // ConstraintLayout
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")    // CoordinatorLayout
}