plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.diffplug.spotless") version "7.2.1"
    id("io.freefair.lombok") version "9.0.0"
}

group = "nl.rutgerkok"
version = "1.14.2-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://rutgerkok.nl/repo")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")

    // Third party plugins (compileOnly as they are provided at runtime)
    compileOnly("com.palmergames:towny:0.96.7.0")
    compileOnly("com.massivecraft:MassiveCore:2.7.5")
    compileOnly("com.massivecraft:Factions:2.7.5")
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.1.218")
    compileOnly("me.glaremasters:guilds:3.5.3.5-RELEASE")
    compileOnly("net.sacredlabyrinth.phaed.simpleclans:SimpleClans:2.15.1")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.3.1")
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("commons-lang:commons-lang:2.6")
    testImplementation("org.mockito:mockito-core:2.23.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

// Resource filtering to replace @version@ in plugin.yml
tasks.processResources {
    val props = mapOf(
        "project" to mapOf(
            "name" to rootProject.name,
            "version" to version
        )
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    manifest {
        attributes(
            "paperweight-mappings-namespace" to "mojang"
        )
    }
}

// Spotless configuration (standard java config)
spotless {
    java {
        googleJavaFormat("1.30.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// Run-paper configuration
tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
}

