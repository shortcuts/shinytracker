plugins {
    alias(libs.plugins.shinytracker.android.library)
    alias(libs.plugins.shinytracker.hilt)
}

android {
    namespace = "com.shinytracker.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
