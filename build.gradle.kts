import xyz.jpenilla.runpaper.RunPaperExtension

plugins {
    java
    idea
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

group = "de.cikles"
version = "1.3-ALPHA"

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

val discordJDA = "5.3.2"
val targetJavaVersion = 21

dependencies {
    paperweight.foliaDevBundle("1.21.4-R0.1-SNAPSHOT")
    implementation("net.dv8tion:JDA:$discordJDA") {
        exclude(module = "opus-java")
    }
}
runPaper.folia.registerTask()
runPaper.folia.pluginsMode.set(RunPaperExtension.Folia.PluginsMode.INHERIT_ALL)
tasks {
    runServer {
        downloadPlugins {
            modrinth("viaversion", "5.3.2")
            modrinth("viabackwards", "5.3.2")
        }
        minecraftVersion("1.21.4")
        jvmArgs("-Xlog:gc*:logs/gc.log:time,uptime:filecount=5,filesize=1M")
    }

    assemble {
        paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }

    named<ProcessResources>("processResources") {
        val props = mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to (project.description ?: ""),
                "apiVersion" to "1.21"
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }

    }
}

java {
    disableAutoTargetJvm()
    runPaper.folia.registerTask()
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
