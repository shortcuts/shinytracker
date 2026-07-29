import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.shinytracker.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("shinyJvmLibrary") {
            id = "shinytracker.jvm.library"
            implementationClass = "ShinyJvmLibraryConventionPlugin"
        }
        register("shinyAndroidApplication") {
            id = "shinytracker.android.application"
            implementationClass = "ShinyApplicationConventionPlugin"
        }
        register("shinyAndroidLibrary") {
            id = "shinytracker.android.library"
            implementationClass = "ShinyLibraryConventionPlugin"
        }
        register("shinyAndroidFeature") {
            id = "shinytracker.android.feature"
            implementationClass = "ShinyFeatureConventionPlugin"
        }
        register("shinyAndroidLibraryCompose") {
            id = "shinytracker.android.library.compose"
            implementationClass = "ShinyComposeConventionPlugin"
        }
        register("shinyHilt") {
            id = "shinytracker.hilt"
            implementationClass = "ShinyHiltConventionPlugin"
        }
    }
}
