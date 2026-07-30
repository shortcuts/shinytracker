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
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.compose.material.icons.extended)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
