import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", "androidx.core:core-ktx:1.17.0")
                add("implementation", "androidx.appcompat:appcompat:1.7.1")
                add("implementation", "com.google.android.material:material:1.13.0")
            }
        }
    }
}
