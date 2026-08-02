plugins {
    // ponytail: id(...) not alias(libs.plugins.shinytracker.jvm.library) -- the alias form
    // breaks with "plugin already on the classpath with an unknown version" once a second
    // project applies this convention plugin (Gradle quirk tied to the catalog's
    // version = "unspecified" placeholder); the id() form resolves the same plugin fine.
    id("shinytracker.jvm.library")
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.shinytracker.core.sprites.descriptors.tool.MainKt")
}

// Main.kt's paths are repo-root-relative (matches scripts/sync_sprites.py's convention).
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

// ConfusablePairs.kt's paths are also repo-root-relative; mirrors the `run` task above.
tasks.register<JavaExec>("confusablePairs") {
    group = "application"
    mainClass.set("com.shinytracker.core.sprites.descriptors.tool.ConfusablePairsKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
}

// WeightSearch.kt's paths are also repo-root-relative; mirrors the `confusablePairs` task above.
tasks.register<JavaExec>("weightSearch") {
    group = "application"
    mainClass.set("com.shinytracker.core.sprites.descriptors.tool.WeightSearchKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
