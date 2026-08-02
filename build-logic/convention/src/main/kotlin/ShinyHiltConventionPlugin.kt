import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

class ShinyHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }

            val libs =
                extensions
                    .getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                    .named("libs")

            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-android-compiler").get())
            }

            // Hilt's aggregated-deps class always compiles into the test source set,
            // so Gradle 9's stricter empty-test-task check fails modules that apply
            // this plugin but don't have any real unit tests yet.
            tasks.withType<Test>().configureEach {
                failOnNoDiscoveredTests.set(false)
            }
        }
    }
}
