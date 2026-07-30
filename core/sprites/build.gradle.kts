plugins {
    alias(libs.plugins.shinytracker.android.library)
    alias(libs.plugins.shinytracker.hilt)
}

android {
    namespace = "com.shinytracker.core.sprites"
}

dependencies {
    implementation(project(":core:model"))

    testImplementation(libs.junit)
}
