plugins {
    alias(libs.plugins.shinytracker.android.library)
    alias(libs.plugins.shinytracker.hilt)
}

android {
    namespace = "com.shinytracker.feature.scan.api"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
