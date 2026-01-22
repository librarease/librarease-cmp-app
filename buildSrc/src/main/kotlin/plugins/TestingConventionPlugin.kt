import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("testImplementation", "junit:junit:4.13.2")
                add("testImplementation", "org.jetbrains.kotlin:kotlin-test:2.3.0")
                add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
                add("testImplementation", "io.mockk:mockk:1.13.14")
                add("testImplementation", "app.cash.turbine:turbine:1.2.0")
                
                add("androidTestImplementation", "androidx.test.ext:junit:1.3.0")
                add("androidTestImplementation", "androidx.test.espresso:espresso-core:3.7.0")
                add("androidTestImplementation", "androidx.compose.ui:ui-test-junit4")
                add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
            }
        }
    }
}
