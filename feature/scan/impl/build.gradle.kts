plugins {
    alias(libs.plugins.shinytracker.android.feature)
}

android {
    namespace = "com.shinytracker.feature.scan.impl"
}

dependencies {
    implementation(project(":feature:scan:api"))
    implementation(project(":core:sprites"))
    implementation(project(":core:data"))
    implementation(libs.kotlinx.coroutines.android)
}
