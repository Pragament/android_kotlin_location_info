// Root-level build.gradle.kts

// Typically left empty unless applying global plugins
plugins {
    // No plugins are usually applied here for modern Android projects using version catalogs
}

// Optional: clean task for convenience
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
