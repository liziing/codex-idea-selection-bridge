plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.wuhongtao.codex"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

val ideaPath = providers.gradleProperty("ideaPath")
val platformVersion = providers.gradleProperty("platformVersion")

dependencies {
    intellijPlatform {
        if (ideaPath.isPresent) {
            local(ideaPath.get())
        } else {
            intellijIdea(platformVersion)
        }
    }

    testImplementation("junit:junit:4.13.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        name = "Codex Selection Bridge"
        version = project.version.toString()

        ideaVersion {
            sinceBuild = "251"
            untilBuild = "251.*"
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.processResources {
    from(rootProject.file("../LICENSE")) {
        into("META-INF")
    }
}

tasks.test {
    useJUnit()
}
