import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
}

val apiBaseUrl = localProperties["API_BASE_URL"] as String? ?: ""
val apiClientId = localProperties["API_CLIENT_ID"] as String? ?: ""
val apiServerUsername = localProperties["API_SERVER_USERNAME"] as String? ?: ""
val apiServerPassword = localProperties["API_SERVER_PASSWORD"] as String? ?: ""

android {
    namespace = "com.lyecdevelopers.core"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")


        // url config
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "API_CLIENT_ID", "\"$apiClientId\"")
        buildConfigField("String", "API_SERVER_USERNAME", "\"$apiServerUsername\"")
        buildConfigField("String", "API_SERVER_PASSWORD", "\"$apiServerPassword\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }

    hilt {
        enableAggregatingTask = false
    }
}

dependencies {
    // Compose UI essentials
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.navigation.compose)



    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.datastore.preferences.core.android)
    ksp(libs.hilt.compiler)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.moshi.converter)

    // preferences
    implementation(libs.androidx.datastore.preferences)

    // security
    implementation(libs.androidx.security.crypto)


    // Optional: for previewing Composables
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
}