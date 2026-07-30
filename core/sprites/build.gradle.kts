plugins {
    alias(libs.plugins.shinytracker.android.library)
    alias(libs.plugins.shinytracker.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.shinytracker.core.sprites"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
