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
  api(libs.gson)

  // Use JUnit Jupiter for testing
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Apply a specific Java toolchain to ease working on different environments
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

tasks.named<Test>("test") {
  // Use JUnit Platform for unit tests
  useJUnitPlatform()
}

// Give consumers on the module path a stable module name instead of one derived
// from the jar filename. Tugboat is deliberately not modular: adapters
// contribute classes to the port packages they implement, which JPMS forbids as
// a split package. So this fixes the *name* only -- it does not make ports and
// an adapter usable together on a module path.
tasks.named<Jar>("jar") {
  manifest {
    attributes["Automatic-Module-Name"] = "com.uncommongoods.tugboat.ports"
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      artifactId = "tugboat-ports"
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
