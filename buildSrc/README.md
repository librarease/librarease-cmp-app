# Build Logic Convention Plugins

This directory contains Gradle convention plugins for the Librarease project. These plugins encapsulate common dependency configurations to maintain consistency across modules.

## Available Convention Plugins

### 1. AndroidLibraryConventionPlugin
**Plugin ID:** Apply with `id("AndroidLibraryConventionPlugin")`

Adds core Android dependencies:
- AndroidX Core KTX
- AppCompat
- Material Design Components

### 2. ComposeConventionPlugin
**Plugin ID:** Apply with `id("ComposeConventionPlugin")`

Adds Jetpack Compose dependencies:
- Compose BOM
- Compose UI components
- Material3
- Activity Compose
- UI Tooling (debug)

### 3. NetworkConventionPlugin
**Plugin ID:** Apply with `id("NetworkConventionPlugin")`

Adds networking dependencies:
- Ktor Client (Core, Android, Darwin)
- Content Negotiation
- Kotlinx Serialization JSON
- Logging

**Note:** You must also apply `kotlin("plugin.serialization")` in your module's build.gradle.kts

### 4. LocalStorageConventionPlugin
**Plugin ID:** Apply with `id("LocalStorageConventionPlugin")`

Adds local storage dependencies:
- DataStore Preferences
- Room Database (runtime, KTX, compiler)
- Multiplatform Settings

**Note:** You must also apply KSP plugin for Room: `id("com.google.devtools.ksp")`

### 5. ImageLoadingConventionPlugin
**Plugin ID:** Apply with `id("ImageLoadingConventionPlugin")`

Adds Coil image loading dependencies:
- Coil Core
- Coil Compose
- Coil Network (Ktor3)

### 6. QrScanningConventionPlugin
**Plugin ID:** Apply with `id("QrScanningConventionPlugin")`

Adds QR code scanning dependencies:
- ML Kit Barcode Scanning
- CameraX (Camera2, Lifecycle, View)
- ZXing Core

### 7. DependencyInjectionConventionPlugin
**Plugin ID:** Apply with `id("DependencyInjectionConventionPlugin")`

Adds Koin dependency injection:
- Koin Core
- Koin Android
- Koin Compose
- Koin AndroidX Compose

### 8. NavigationConventionPlugin
**Plugin ID:** Apply with `id("NavigationConventionPlugin")`

Adds navigation dependencies:
- AndroidX Navigation Compose
- Decompose (with Compose extensions)

### 9. TestingConventionPlugin
**Plugin ID:** Apply with `id("TestingConventionPlugin")`

Adds testing dependencies:
- JUnit
- Kotlin Test
- Coroutines Test
- MockK
- Turbine
- Espresso
- Compose UI Test

### 10. PermissionsConventionPlugin
**Plugin ID:** Apply with `id("PermissionsConventionPlugin")`

Adds permission handling:
- Accompanist Permissions

## Usage Example

In your module's `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.3.0"
    id("com.google.devtools.ksp") version "2.3.0-1.0.29"
}

// Apply convention plugins
apply(plugin = "AndroidLibraryConventionPlugin")
apply(plugin = "ComposeConventionPlugin")
apply(plugin = "NetworkConventionPlugin")
apply(plugin = "ImageLoadingConventionPlugin")
apply(plugin = "DependencyInjectionConventionPlugin")
apply(plugin = "NavigationConventionPlugin")
apply(plugin = "TestingConventionPlugin")
```

## BuildConfig Object

A `BuildConfig` object is available with common configuration values:

```kotlin
BuildConfig.compileSdk  // 36
BuildConfig.minSdk      // 24
BuildConfig.targetSdk   // 36

BuildConfig.Versions.kotlin        // 2.3.0
BuildConfig.Versions.ktor          // 3.0.3
BuildConfig.Versions.coil          // 3.0.4
BuildConfig.Versions.koin          // 4.0.1
// ... and more
```

## Notes

- These are **dependency-only** convention plugins
- You still need to apply the base plugins (Android, Kotlin, etc.) in your module
- Convention plugins help maintain consistent dependency versions across modules
- Update versions in the individual plugin files as needed
