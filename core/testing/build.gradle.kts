plugins {
    alias(libs.plugins.shinytracker.android.library)
}

android {
    namespace = "com.shinytracker.core.testing"
}

dependencies {
    api(project(":core:database"))
    api(project(":core:model"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
