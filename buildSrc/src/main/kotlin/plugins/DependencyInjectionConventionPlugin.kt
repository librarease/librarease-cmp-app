import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class DependencyInjectionConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", "io.insert-koin:koin-core:4.0.1")
                add("implementation", "io.insert-koin:koin-android:4.0.1")
                add("implementation", "io.insert-koin:koin-androidx-compose:4.0.1")
                add("implementation", "io.insert-koin:koin-compose:4.0.1")
            }
        }
    }
}
