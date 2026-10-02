// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MIT

plugins {
  `java-library`
  `maven-publish`
}

repositories {
  mavenCentral()
}

dependencies {
  api(project(":tugboat-ports"))
  api(libs.easypost.java)

  testImplementation(libs.junit.jupiter)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}

// Stable module name for consumers on the module path (see ports/build.gradle.kts).
tasks.named<Jar>("jar") {
  manifest {
    attributes["Automatic-Module-Name"] = "com.uncommongoods.tugboat.adapters.easypost"
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      artifactId = "tugboat-easypost-adapter"
      // groupId/version inherit from the root build (allprojects); do not pin here.

      pom {
        licenses {
          license {
            name = "MIT License"
            url = "https://opensource.org/licenses/MIT"
            distribution = "repo"
          }
        }
      }
    }
  }

}
