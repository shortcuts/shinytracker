plugins {
    alias(libs.plugins.shinytracker.android.application)
    alias(libs.plugins.shinytracker.hilt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.shinytracker.app"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity.compose)
}
