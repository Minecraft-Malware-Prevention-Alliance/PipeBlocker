plugins {
  id("java-library")
  id("maven-publish")
}

allprojects {
  apply(plugin = "java-library")
  // Project properties
  group = "info.mmpa.pipeblocker"
  version = "1.1.1"

  val embed: Configuration by configurations.creating {
    description = "Included in output JAR"
  }

  listOf(configurations.implementation).forEach {
    it.configure {
      extendsFrom(embed)
    }
  }

  tasks.named<Jar>("jar") {
    dependsOn(configurations["embed"])
    from(provider { configurations["embed"].map { if (it.isDirectory) it else zipTree(it) } })
  }
}