plugins {
    `kotlin-dsl` // Required for custom Gradle plugins
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

val myPluginVersion = "1.0.0" // Define once for consistency

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:8.11.1")
    implementation("com.google.code.gson:gson:2.13.1")
}

gradlePlugin {
    plugins {
        create("mnotifyplugin") {
            id = "com.github.mnotifysdk.mnotifyplugin"
//            id = "com.convex.mnotifyplugin" // Match your package
            implementationClass = "com.convex.mnotifyplugin.MainGradlePlugin"
            version = myPluginVersion// Add version here
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "com.github.mnotifysdk"
            artifactId = "mnotifyplugin"
            version = myPluginVersion

            artifact(tasks.register("pluginMarker", Jar::class) {
                archiveClassifier.set("plugin-marker")
            })
        }
    }
    repositories {
        maven {
            url = uri("$rootDir/build/repo")
        }
    }
}

sourceSets["main"].java.srcDirs("src/main/kotlin")

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
