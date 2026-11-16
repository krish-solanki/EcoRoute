plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ecoroute"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.ecoroute"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
    }
    sourceSets {
        getByName("main") {
            res.setSrcDirs(
                listOf(
                    "src/main/res",
                    "src/main/res/drawables/onBoardingScreen",
                    "src/main/res/drawables/homeScreen",
                    "src/main/res/drawables/bottomNavView",

                    "src/main/res/layouts/activities",
                    "src/main/res/layouts/fragments",
                    "src/main/res/layouts/dialogs"
                )
            )
        }
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
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("com.github.Dimezis:BlurView:version-3.2.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.13.1")
}