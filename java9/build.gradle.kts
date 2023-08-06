
plugins {
    id("java-library")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        // Azul covers the most platforms for Java 8 toolchains, crucially including MacOS arm64
        vendor.set(JvmVendorSpec.AZUL)
    }
    targetCompatibility = JavaVersion.VERSION_1_9
    sourceCompatibility = JavaVersion.VERSION_1_9
}

dependencies {
    implementation(project(":common"))
}
