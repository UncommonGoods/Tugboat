// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MIT

plugins {
  `java-library`
  `maven-publish`
  alias(libs.plugins.test.logger)
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":tugboat-engine"))
  implementation(project(":adapters:tugboat-easypost"))
  // Optional vendor adapters (see settings.gradle.kts).
  findProject(":adapters:tugboat-esw-package")?.let { implementation(it) }
  findProject(":adapters:tugboat-parsel")?.let { implementation(it) }

  testImplementation(libs.junit.jupiter)
  testImplementation(project(":tugboat-engine"))
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

testlogger {
  theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}

// Stable module name for consumers on the module path (see ports/build.gradle.kts).
tasks.named<Jar>("jar") {
  manifest {
    attributes["Automatic-Module-Name"] = "com.uncommongoods.tugboat.hooks"
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      artifactId = "tugboat-hooks"
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
