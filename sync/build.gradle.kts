plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.lyecdevelopers.sync"
    compileSdk = 36

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.20"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
    hilt {
        enableAggregatingTask = false
    }

    configurations.all {
        resolutionStrategy {
            force("com.google.guava:guava:32.1.3-android")
        }
    }
}

dependencies {
    implementation (project(":core"))
    implementation(project(":form"))

    // Compose UI essentials
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    // fhir
    implementation(libs.android.fhir.engine)
    implementation(libs.android.fhir.sdc)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.moshi.converter)

    // logging
    implementation(libs.timber)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // work
    implementation(libs.hilt.work)
    implementation(libs.hilt.work.compiler)
    implementation(libs.work.runtime.ktx)

    // Optional: for previewing Composables
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
}