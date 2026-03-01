plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.data"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
}

dependencies {
    implementation(project(":pula-domain"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}