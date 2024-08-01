plugins {
    id("java")
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("com.modrinth.minotaur") version "2.+"
}

val supportedVersions = listOf("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21")

group = "com.codisimus.plugins"
version = "5.6.5"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/groups/public")
    maven("https://repo.citizensnpcs.co")
    maven("https://maven.enginehub.org/repo/")
    maven("https://dl.auranode.com/repo/")
    maven("https://repo.codemc.io/repository/maven-public")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.battleplugins.org/releases/")
    maven("https://repo.battleplugins.org/snapshots/")
}

dependencies {
    compileOnly(libs.paper.api)

    // Plugin dependencies
    compileOnly(libs.citizens.api)
    compileOnly(libs.worldguard.core)
    compileOnly(libs.worldedit.bukkit)
    compileOnly(libs.vault.api)
    compileOnly(libs.nuvotifier)
    compileOnly(libs.mythicdrops)
    compileOnly(libs.placeholder.api)
    compileOnly(libs.mythicmobs)
    compileOnly(libs.battlearena)

    // Shaded libs
    implementation(libs.bstats.bukkit)
}

tasks {
    runServer {
        minecraftVersion("1.20")
    }

    shadowJar {
        from("src/main/java/resources") {
            include("*")
        }

        // Relocate metrics
        relocate("org.bstats", "com.codisimus.plugins.phatloots.shaded.bstats")

        archiveFileName.set("PhatLoots.jar")
        archiveClassifier.set("")
    }

    jar {
        archiveClassifier.set("unshaded")
    }

    processResources {
        expand("version" to rootProject.version)
    }

    named("build") {
        dependsOn(shadowJar)
    }
}

modrinth {
    val snapshot = "SNAPSHOT" in rootProject.version.toString()

    token.set(System.getenv("MODRINTH_TOKEN") ?: "")
    projectId.set("phatloots")
    versionNumber.set(rootProject.version as String + if (snapshot) "-" + System.getenv("BUILD_NUMBER") else "")
    versionType.set(if (snapshot) "beta" else "release")
    changelog.set(System.getenv("CHANGELOG") ?: "")
    uploadFile.set(tasks.shadowJar)
    gameVersions.set(supportedVersions)
}
