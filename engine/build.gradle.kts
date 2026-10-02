// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

plugins {
  `java-library`
  `maven-publish`
  alias(libs.plugins.test.logger)
}

repositories {
  mavenCentral()
}

dependencies {
  api(project(":tugboat-ports"))
  compileOnly(libs.easypost.java)
  compileOnly(project(":adapters:tugboat-easypost"))
  implementation(libs.pdfbox)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.easypost.java)
  testImplementation(libs.jedis)
  testImplementation(libs.bundles.testcontainers)
  testImplementation(project(":adapters:tugboat-redis"))
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
    attributes["Automatic-Module-Name"] = "com.uncommongoods.tugboat.engine"
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      artifactId = "tugboat-engine"
      // groupId/version inherit from the root build (allprojects); do not pin here.

      pom {
        name = "Tugboat Engine"
        description = "Shipping workflow state machine over EasyPost-compatible APIs."
        licenses {
          license {
            name = "Mozilla Public License 2.0"
            url = "https://www.mozilla.org/MPL/2.0/"
            distribution = "repo"
            comments = "File-level copyleft: modifications to these files must be published " +
              "when distributed. Linking from independent modules under terms of your choice " +
              "is permitted (MPL 2.0 section 3.3). See LICENSE in the engine module."
          }
        }
      }
    }
  }

}
