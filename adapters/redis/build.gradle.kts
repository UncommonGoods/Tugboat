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
  implementation(project(":tugboat-ports"))
  // jedis pulls slf4j-api transitively. Hosts bring their own SLF4J binding
  // (the Bridge slf4j-simple, XO logback), and two api artifacts on one
  // classpath produce the "multiple bindings" warning and a coin-flip over
  // which one wins. Exclude it here rather than in each host: the conflict is
  // this adapter's to declare, and a new host should not have to rediscover it.
  api(libs.jedis) {
    exclude(group = "org.slf4j", module = "slf4j-api")
  }

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

// Stable module name for consumers on the module path (see ports/build.gradle.kts).
tasks.named<Jar>("jar") {
  manifest {
    attributes["Automatic-Module-Name"] = "com.uncommongoods.tugboat.adapters.redis"
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      artifactId = "tugboat-redis-adapter"
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
