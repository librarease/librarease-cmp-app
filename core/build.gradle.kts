plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    kotlin("plugin.serialization") version "2.3.0"
}

apply(plugin = "DependencyInjectionConventionPlugin")

android {
    namespace = "com.example.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Networking
    api("io.ktor:ktor-client-android:3.0.3")
    api("io.ktor:ktor-client-core:3.0.3")
    api("io.ktor:ktor-client-content-negotiation:3.0.3")
    api("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    api("io.ktor:ktor-client-logging:3.0.3")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    
    // DataStore
    api("androidx.datastore:datastore-preferences:1.1.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}