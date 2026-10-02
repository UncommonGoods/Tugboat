// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MIT

plugins {
  alias(libs.plugins.test.logger) apply false
}

allprojects {
  group = "com.uncommongoods.tugboat"
  version = "3.0.0"
}

subprojects {
  apply(plugin = "java-library")
  apply(plugin = "com.adarshr.test-logger")

  repositories {
    mavenCentral()
  }

  configure<JavaPluginExtension> {
    toolchain {
      languageVersion = JavaLanguageVersion.of(21)
      vendor = JvmVendorSpec.ADOPTIUM
    }
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }
}
