pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "shinytracker"

include(":app")

include(":core:common")
include(":core:model")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:designsystem")
include(":core:sprites")
include(":core:testing")

include(":feature:scan:api")
include(":feature:scan:impl")
include(":feature:checklist:api")
include(":feature:checklist:impl")
