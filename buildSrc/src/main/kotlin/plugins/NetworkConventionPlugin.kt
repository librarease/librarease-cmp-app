import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class NetworkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", "io.ktor:ktor-client-core:3.0.3")
                add("implementation", "io.ktor:ktor-client-content-negotiation:3.0.3")
                add("implementation", "io.ktor:ktor-serialization-kotlinx-json:3.0.3")
                add("implementation", "io.ktor:ktor-client-logging:3.0.3")
                add("implementation", "io.ktor:ktor-client-android:3.0.3")
                add("implementation", "io.ktor:ktor-client-darwin:3.0.3")
                add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            }
        }
    }
}
