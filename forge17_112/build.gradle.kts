import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.RunConfigurationContainer

plugins {
  id("java-library")
  id("maven-publish")
  id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
  id("eclipse")
  id("com.gtnewhorizons.retrofuturagradle") version "1.3.21"
}

// Set the toolchain version to decouple the Java we run Gradle with from the Java used to compile and run the mod
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
    // Azul covers the most platforms for Java 8 toolchains, crucially including MacOS arm64
    vendor.set(org.gradle.jvm.toolchain.JvmVendorSpec.AZUL)
  }
}

// Most RFG configuration lives here, see the JavaDoc for com.gtnewhorizons.retrofuturagradle.MinecraftExtension
minecraft {
  mcVersion.set("1.12.2")

  // Username for client run configurations
  username.set("Developer")

  // Generate a field named VERSION with the mod version in the injected Tags class
  injectedTags.put("VERSION", project.version)

  // If you need the old replaceIn mechanism, prefer the injectTags task because it doesn't inject a javac plugin.
  // tagReplacementFiles.add("RfgExampleMod.java")

  // Enable assertions in the mod's package when running the client or server
  extraRunJvmArguments.add("-ea:${project.group}")

  // If needed, add extra tweaker classes like for mixins.
  // extraTweakClasses.add("org.spongepowered.asm.launch.MixinTweaker")

  // Exclude some Maven dependency groups from being automatically included in the reobfuscated runs
  groupsToExcludeFromAutoReobfMapping.addAll("com.diffplug", "com.diffplug.durian", "net.industrial-craft")
}

// Generates a class named rfg.examplemod.Tags with the mod version in it, you can find it at
tasks.injectTags.configure {
  outputClassName.set("${project.group}.Tags")
}

// Put the version from gradle into mcmod.info, and include the filter file
tasks.processResources.configure {
  inputs.property("version", project.version)

  filesMatching("mcmod.info") {
    expand(mapOf("modVersion" to project.version))
  }
}

// Create a new dependency type for runtime-only dependencies that don't get included in the maven publication
val runtimeOnlyNonPublishable: Configuration by configurations.creating {
  description = "Runtime only dependencies that are not published alongside the jar"
  isCanBeConsumed = false
  isCanBeResolved = false
}
listOf(configurations.runtimeClasspath, configurations.testRuntimeClasspath).forEach {
  it.configure {
    extendsFrom(
      runtimeOnlyNonPublishable
    )
  }
}

// Add an access tranformer
// tasks.deobfuscateMergedJarToSrg.configure {accessTransformerFiles.from("src/main/resources/META-INF/mymod_at.cfg")}

// Dependencies
repositories {
  maven {
    name = "OvermindDL1 Maven"
    url = uri("https://gregtech.overminddl1.com/")
    mavenContent {
      excludeGroup("net.minecraftforge") // missing the `universal` artefact
    }
  }
  maven {
    name = "GTNH Maven"
    url = uri("http://jenkins.usrv.eu:8081/nexus/content/groups/public/")
    isAllowInsecureProtocol = true
  }
}

dependencies {
  embed(project(":common"))
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
    }
  }
}

// IDE Settings
eclipse {
  classpath {
    isDownloadSources = true
    isDownloadJavadoc = true
  }
}

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
    inheritOutputDirs = true // Fix resources in IJ-Native runs
  }
}

tasks.processIdeaSettings.configure {
  dependsOn(tasks.injectTags)
}

tasks.named<Jar>("jar") {
  manifest {
    attributes(
            "FMLCorePlugin" to ("info.mmpa.pipeblocker.PipeLoadingPlugin"),
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true"
    )
  }
  for (projectName in arrayOf(":java9", ":dummy")) {
    from(project(projectName).tasks.compileJava.get().outputs) {
      include("**/*.class")
    }
    dependsOn(project(projectName).tasks.compileJava.get())
  }

  archiveBaseName.set("pipeblocker-forge17_112")
}