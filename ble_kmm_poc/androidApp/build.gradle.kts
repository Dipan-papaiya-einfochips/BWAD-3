plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "kaz.bpmandroid"
    compileSdk = 33
    defaultConfig {
        applicationId = "kaz.bpmandroid"
        minSdk = 24
        targetSdk = 33
        versionCode = 22
        versionName = "2.2.2.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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
    implementation(project(":shared"))
    /*   implementation("androidx.compose.ui:ui:1.3.1")
       implementation("androidx.compose.ui:ui-tooling:1.3.1")
       implementation("androidx.compose.ui:ui-tooling-preview:1.3.1")
       implementation("androidx.compose.foundation:foundation:1.3.1")
       implementation("androidx.compose.material:material:1.3.1")
       implementation("androidx.activity:activity-compose:1.6.1")*/
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

}