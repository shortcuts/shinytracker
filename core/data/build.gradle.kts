plugins {
    alias(libs.plugins.shinytracker.android.library)
    alias(libs.plugins.shinytracker.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.shinytracker.core.data"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))
    implementation(project(":core:sprites"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
