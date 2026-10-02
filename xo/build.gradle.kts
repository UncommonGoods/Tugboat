// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

plugins {
    java
    application
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.shadow)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.test.logger)
}

repositories {
    mavenCentral()
}

val queueSuffix = ""
val pathSuffix = ""

dependencies {
    implementation(project(":tugboat-engine"))
    implementation(project(":tugboat-hooks"))
    // Every adapter module on disk (see settings.gradle.kts). XO discovers
    // shipping clients via ServiceLoader, so absent adapters simply are not
    // registered and naming them here individually bought nothing.
    findProject(":adapters")?.subprojects.orEmpty().forEach { implementation(it) }

    // Drop-in adapters: any jar in the repo-root libs/ directory joins the
    // runtime classpath, and shadowJar's mergeServiceFiles() carries its
    // META-INF/services registration into the fat jar. runtimeOnly rather than
    // implementation -- XO reaches adapters through the ports interfaces and
    // ServiceLoader, so they have no business on the compile classpath. An
    // absent or empty libs/ resolves to nothing.
    runtimeOnly(fileTree(rootProject.layout.projectDirectory.dir("libs")) { include("*.jar") })

    // Versions for these come from the Micronaut BOM pinned in the micronaut
    // block below, so their catalog aliases carry no version of their own.
    annotationProcessor(libs.micronaut.http.validation)
    annotationProcessor(libs.micronaut.serde.processor)
    annotationProcessor(libs.micronaut.servlet.processor)
    implementation(libs.micronaut.http.client.jdk)
    implementation(libs.micronaut.management)
    implementation(libs.micronaut.jms.sqs)
    implementation(libs.micronaut.serde.jackson)
    implementation(libs.jul.to.slf4j)
    implementation(libs.slf4j.simple.managed)
    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.snakeyaml)
}


application {
    mainClass.set("com.uncommongoods.tugboat.xo.Application")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Keep the deployable jar name stable across version bumps (e.g. tugboat-xo-all-optimized.jar)
// so the Dockerfiles never need editing. The version still lives in the root build.gradle.kts
// and is baked into the jar manifest.
tasks.withType<AbstractArchiveTask>().configureEach {
    archiveVersion.set("")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

testlogger {
    theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}

// Configure resource processing for queue and path suffix
tasks.named<ProcessResources>("processResources") {
    val qSuffix = queueSuffix
    val pSuffix = pathSuffix
    inputs.property("queueSuffix", qSuffix)
    inputs.property("pathSuffix", pSuffix)
    filesMatching("application*.yml") {
        filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to mapOf(
            "queueSuffix" to qSuffix,
            "pathSuffix" to pSuffix
        ))
    }
}

// Configure logging
tasks.named<JavaExec>("run") {
    systemProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO")
    systemProperty("org.slf4j.simpleLogger.log.com.uncommongoods.tugboat", "DEBUG")
}

graalvmNative.toolchainDetection = true

micronaut {
    version(libs.versions.micronaut.get())
    runtime("jetty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.uncommongoods.tugboat.xo.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = true
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = false
        optimizeClassLoading = true
        deduceEnvironment = false
        optimizeNetty = true
        replaceLogbackXml = true
    }
}


