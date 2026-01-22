import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class LocalStorageConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", "androidx.datastore:datastore-preferences:1.1.1")
                add("implementation", "androidx.datastore:datastore-preferences-core:1.1.1")
                add("implementation", "androidx.room:room-runtime:2.7.0-alpha12")
                add("implementation", "androidx.room:room-ktx:2.7.0-alpha12")
                add("ksp", "androidx.room:room-compiler:2.7.0-alpha12")
                add("implementation", "com.russhwolf:multiplatform-settings:1.2.0")
                add("implementation", "com.russhwolf:multiplatform-settings-no-arg:1.2.0")
            }
        }
    }
}
