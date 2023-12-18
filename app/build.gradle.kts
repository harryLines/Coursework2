plugins {
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.trailblazer"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.TrailBlazer"
        minSdk = 31
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        dataBinding = true
    }
}


dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.work:work-runtime:2.7.0")
    implementation("com.google.android.gms:play-services-location:17.1.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference:1.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
        constraints {
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0") {
                because("kotlin-stdlib-jdk7 is now a part of kotlin-stdlib")
            }
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0") {
                because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib")
            }
        }
    }