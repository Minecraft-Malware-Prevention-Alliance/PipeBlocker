plugins {
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
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    compileOnly("net.fabricmc:fabric-loader:0.14.21")
    embed(project(":common"))
}

repositories {
    mavenCentral()
}

// Put the version from gradle into fabric.mod.json
tasks.processResources.configure {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

tasks.named<Jar>("jar") {
    for (projectName in arrayOf(":java9")) {
        from(project(projectName).tasks.compileJava.get().outputs) {
            include("**/*.class")
        }
        dependsOn(project(projectName).tasks.compileJava.get())
    }

    archiveBaseName.set("pipeblocker-fabric")
}