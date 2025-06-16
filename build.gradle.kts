plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "de.dp_coding"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", "2025.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Add necessary plugin dependencies for compilation here, example:
        bundledPlugin("Git4Idea")
    }

    // HTTP client for REST API communication
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }

        changeNotes = """
      Initial version
    """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    // Create a zip distribution of the plugin
    register<Zip>("createPluginZip") {
        dependsOn("buildPlugin")
        archiveBaseName.set("zammad-plugin")
        archiveVersion.set(project.version.toString())
        archiveExtension.set("zip")

        from("$buildDir/distributions") {
            include("*.zip")
        }
        destinationDirectory.set(file("$buildDir/distributions/zip"))
    }
}
