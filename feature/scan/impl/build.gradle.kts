plugins {
    alias(libs.plugins.shinytracker.android.feature)
}

android {
    namespace = "com.shinytracker.feature.scan.impl"
}

dependencies {
    implementation(project(":feature:scan:api"))
    implementation(libs.kotlinx.coroutines.android)
}
