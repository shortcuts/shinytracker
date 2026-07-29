plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":app"))
    kover(project(":core:common"))
    kover(project(":core:model"))
    kover(project(":core:database"))
    kover(project(":core:datastore"))
    kover(project(":core:data"))
    kover(project(":core:designsystem"))
    kover(project(":core:sprites"))
    kover(project(":core:testing"))
    kover(project(":feature:scan:api"))
    kover(project(":feature:scan:impl"))
    kover(project(":feature:checklist:api"))
    kover(project(":feature:checklist:impl"))
}
