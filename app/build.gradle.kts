import java.io.ByteArrayOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.services)
    alias(libs.plugins.crashlytics)
}

fun getVersionCode(): Int {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim().toInt()
}

fun getVersionName(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags", "--abbrev=0")
        standardOutput = stdout
        isIgnoreExitValue = true
    }
    val tag = stdout.toString().trim()
    return tag.ifEmpty { "0.1.0" }
}

val localProperties = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
}

android {
    namespace = "com.lyecdevelopers.ugandaemrmobile"
    compileSdk = 36

    android {
        splits {
            abi {
                reset()
                include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            }
        }
    }


    defaultConfig {
        applicationId = "com.lyecdevelopers.ugandaemrmobile"
        minSdk = 28
        versionCode = getVersionCode()
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("KEYSTORE_FILE") ?: ""
            val storePasswordProp = localProperties.getProperty("KEYSTORE_PASSWORD") ?: ""
            val keyAliasProp = localProperties.getProperty("KEY_ALIAS") ?: ""
            val keyPasswordProp = localProperties.getProperty("KEY_PASSWORD") ?: ""

            println("🔑 [Signing] Using keystore at: $storeFilePath")

            if (storeFilePath.isNotBlank()) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = storePasswordProp
                keyAlias = keyAliasProp
                keyPassword = keyPasswordProp
            } else {
                println("⚠️  Skipping release signing config because keystore path is missing.")
            }
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources.excludes.addAll(
            listOf("META-INF/ASL-2.0.txt", "META-INF/LGPL-3.0.txt")
        )
    }


    kotlin {
        jvmToolchain(17)
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
    implementation(project(":core"))
    implementation(project(":sync"))
    implementation(project(":settings"))
    implementation(project(":worklist"))
    implementation(project(":auth"))
    implementation(project(":core-navigation"))
    implementation(project(":main"))
    implementation(project(":form"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.material.icons.extended)
    implementation(libs.splashscreen)
    implementation(libs.androidx.appcompat)

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
    implementation(libs.work.runtime)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.work.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
