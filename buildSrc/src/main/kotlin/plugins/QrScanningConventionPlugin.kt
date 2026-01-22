import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class QrScanningConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", "com.google.mlkit:barcode-scanning:17.3.0")
                add("implementation", "androidx.camera:camera-camera2:1.4.1")
                add("implementation", "androidx.camera:camera-lifecycle:1.4.1")
                add("implementation", "androidx.camera:camera-view:1.4.1")
                add("implementation", "com.google.zxing:core:3.5.3")
            }
        }
    }
}
