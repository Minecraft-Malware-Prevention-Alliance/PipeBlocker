import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        // Azul covers the most platforms for Java 8 toolchains, crucially including MacOS arm64
        vendor.set(org.gradle.jvm.toolchain.JvmVendorSpec.AZUL)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-core:2.0-beta9")!!
    implementation("org.apache.logging.log4j:log4j-api:2.0-beta9")!!

    embed(project(":common"))
}

repositories {
    mavenCentral()
}


tasks.named<Jar>("jar") {
    for (projectName in arrayOf(":java9")) {
        from(project(projectName).tasks.compileJava.get().outputs) {
            include("**/*.class")
        }
        dependsOn(project(projectName).tasks.compileJava.get())
    }

    manifest {
        attributes["Premain-Class"] = "info.mmpa.pipeblocker.PipeBlockerAgent"
        attributes["Implementation-Title"] = "PipeBlocker Java Agent"
        attributes["Implementation-Version"] = rootProject.version
    }

    archiveBaseName.set("pipeblocker-javaagent")

    dependsOn(tasks.shadowJar)
}

tasks.named<ShadowJar>("shadowJar") {
    for (projectName in arrayOf(":java9")) {
        from(project(projectName).tasks.compileJava.get().outputs) {
            include("**/*.class")
        }
        dependsOn(project(projectName).tasks.compileJava.get())
    }

    exclude("META-INF/LICENSE")
}
