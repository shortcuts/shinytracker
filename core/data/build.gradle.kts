plugins {
    alias(libs.plugins.shinytracker.android.library)
    alias(libs.plugins.shinytracker.hilt)
}

android {
    namespace = "com.shinytracker.core.data"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:model"))

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
