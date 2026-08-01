plugins {
    alias(libs.plugins.shinytracker.android.library)
}

android {
    namespace = "com.shinytracker.core.testing"
}

dependencies {
    api(project(":core:data"))
    api(project(":core:database"))
    api(project(":core:datastore"))
    api(project(":core:model"))
    api(libs.datastore.preferences)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
