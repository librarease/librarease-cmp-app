import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class NavigationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", "org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
                add("implementation", "com.arkivanov.decompose:decompose:3.2.0-alpha05")
                add("implementation", "com.arkivanov.decompose:extensions-compose:3.2.0-alpha05")
            }
        }
    }
}
