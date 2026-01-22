import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ImageLoadingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", "io.coil-kt.coil3:coil:3.0.4")
                add("implementation", "io.coil-kt.coil3:coil-compose:3.0.4")
                add("implementation", "io.coil-kt.coil3:coil-network-ktor3:3.0.4")
            }
        }
    }
}
