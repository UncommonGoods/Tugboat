// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

import java.security.MessageDigest
import java.time.Instant

plugins {
    java
    application
    alias(libs.plugins.javafx)
    alias(libs.plugins.shadow)
}

val appDisplayName = "Tugboat Bridge"
val msiBaseName = "TugboatBridge"
val appIcon = "tugboat.ico"
// Stable UpgradeCode: never change this, or new MSIs will install
// side-by-side instead of upgrading in place.
val upgradeUuid = "6FF3F6E4-D520-4682-ABC6-F89D22890799"
val updateChannel = project.findProperty("updateChannel")?.toString() ?: "bridge"
val updateBucket = project.findProperty("updateBucket")?.toString()
    ?: System.getenv("TUGBOAT_UPDATE_BUCKET") ?: "tugboat-updates"
val updateRegion = project.findProperty("updateRegion")?.toString() ?: "us-east-1"
val updateBaseUrl = "https://$updateBucket.s3.$updateRegion.amazonaws.com/updates"
val appVersion = project.findProperty("versionOverride")?.toString() ?: project.version.toString()

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("com.uncommongoods.tugboat.bridge.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":tugboat-engine"))
    implementation(project(":tugboat-hooks"))
    implementation(libs.gson)
    // Every adapter module on disk (see settings.gradle.kts). Naming them here
    // one by one is what used to make a private adapter require an edit to this
    // file; the Bridge now builds against whatever adapters/ contains.
    findProject(":adapters")?.subprojects.orEmpty().forEach { implementation(it) }

    // Drop-in adapters: any jar in the repo-root libs/ directory joins the
    // runtime classpath, and shadowJar's mergeServiceFiles() carries its
    // META-INF/services registration into the fat jar. runtimeOnly rather than
    // implementation -- the Bridge reaches adapters only through the ports
    // interfaces and ServiceLoader, never by class, so they have no business on
    // the compile classpath. An absent or empty libs/ resolves to nothing.
    runtimeOnly(fileTree(rootProject.layout.projectDirectory.dir("libs")) { include("*.jar") })

    implementation(libs.bundles.javafx)

    implementation(libs.atlantafx.base)
    implementation(libs.bundles.ikonli)

    implementation(libs.jserialcomm)

    implementation(libs.pdfbox)

    implementation(libs.slf4j.simple)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    systemProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO")
    systemProperty("org.slf4j.simpleLogger.log.com.uncommongoods.tugboat", "DEBUG")
    systemProperty("javafx.preloader", "com.uncommongoods.tugboat.bridge.BridgePreloader")
}

// Fat JAR with all dependencies, for jpackage.
//
// This MUST merge META-INF/services rather than deduplicate it. Every shipping
// adapter registers a provider at the same path
// (META-INF/services/...ShippingClientFactory), so a plain Jar task with
// DuplicatesStrategy.EXCLUDE keeps only whichever jar it copies first and
// silently drops the rest -- the packaged MSI would then offer a single
// shipping client while `gradlew run` (real classpath, all files visible to
// ServiceLoader) still showed all of them. Shadow's mergeServiceFiles()
// concatenates them instead, which is also how :tugboat-xo packages.
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("fat")

    mergeServiceFiles()

    manifest {
        attributes["Main-Class"] = "com.uncommongoods.tugboat.bridge.Main"
        attributes["Implementation-Title"] = appDisplayName
        attributes["Implementation-Version"] = appVersion
    }

    // The Bridge ships as a classpath application; a stray module-info from a
    // modular dependency (e.g. gson) has no meaning here. Signature files are
    // invalid once jars are merged.
    exclude("module-info.class")
    exclude("META-INF/versions/*/module-info.class")
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

// Shadow pairs with the application plugin to offer start-script distributions.
// The Bridge ships as an MSI built by jpackage from the fat jar, so these are
// ~36MB of archives nobody consumes; keep them off the `build` path.
tasks.named("shadowDistTar") { enabled = false }
tasks.named("shadowDistZip") { enabled = false }

// ---------------------------------------------------------------------------
// Windows packaging + S3 update publishing. Usage:
//   gradlew :tugboat-bridge:jpackageMsi                      (build MSI only)
//   gradlew :tugboat-bridge:publishVariant                   (build + upload)
//   gradlew :tugboat-bridge:publishVariant -PupdateChannel=bridge-test
// ---------------------------------------------------------------------------

val fatJarTask = tasks.named<Jar>("shadowJar")
val distDir = layout.buildDirectory.dir("dist")
val msiFileName = "$msiBaseName-$appVersion.msi"

val jpackageInput = tasks.register<Sync>("jpackageInput") {
    // jpackage bundles the entire --input directory, so it must contain
    // exactly the fat jar and nothing else.
    from(fatJarTask)
    into(layout.buildDirectory.dir("jpackage-input"))
}

// The installer's legal notice is a template: the exact Temurin build that
// jlink bakes into the runtime image is only known at execution time, so the
// GPLv2 corresponding-source URL is substituted in then. See THIRD-PARTY.md.
val licenseTemplate = layout.projectDirectory.file("legal/installer-license.txt")
val renderedLicense = layout.buildDirectory.file("jpackage/installer-license.txt")

val jpackageMsi = tasks.register<Exec>("jpackageMsi") {
    group = "distribution"
    description = "Builds the \"$appDisplayName\" MSI installer with jpackage (Windows only)."
    dependsOn(jpackageInput)
    inputs.file(licenseTemplate).withPropertyName("installerLicenseTemplate")
    notCompatibleWithConfigurationCache("release task; resolves jpackage from the toolchain at execution time")

    doFirst {
        if (!System.getProperty("os.name").lowercase().contains("win")) {
            throw GradleException("jpackageMsi must run on Windows: MSI packaging requires jpackage's WiX Toolset backend (install WiX 3.x and ensure it is on PATH).")
        }
        // Resolve the same toolchain the runtime image will be linked from.
        // The vendor is pinned here as well as in the root build: a launcher
        // spec that omits it can select a different JDK than the compile
        // toolchain, and the notice below would then name the wrong source.
        val jdk = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
            vendor.set(JvmVendorSpec.ADOPTIUM)
        }.get().metadata

        val runtimeVersion = jdk.javaRuntimeVersion
        val build = Regex("""^(\d+\.\d+\.\d+)\+(\d+)""").find(runtimeVersion)
            ?: throw GradleException(
                "Cannot parse Temurin build from '$runtimeVersion'. The installer's GPLv2 " +
                    "source notice is derived from it, so packaging stops rather than ship a wrong URL."
            )
        val (version, buildNum) = build.destructured
        val tag = "jdk-$version+$buildNum".replace("+", "%2B")
        val jdkSource = "https://github.com/adoptium/temurin21-binaries/releases/tag/$tag\n" +
            "    (source archive: OpenJDK21U-jdk-sources_${version}_$buildNum.tar.gz)"

        val rendered = renderedLicense.get().asFile
        rendered.parentFile.mkdirs()
        rendered.writeText(
            licenseTemplate.asFile.readText()
                .replace("@JDK_VERSION@", "$version+$buildNum")
                .replace("@JDK_SOURCE@", jdkSource)
        )
        Regex("""\[[A-Z][A-Z-]+]|@[A-Z_]+@""").find(rendered.readText())?.let {
            throw GradleException(
                "$rendered still contains the ${it.value} placeholder. " +
                    "Replace it before building a distributable installer."
            )
        }

        val jdkHome = jdk.installationPath
        val dest = distDir.get().asFile
        dest.mkdirs()
        dest.listFiles { f -> f.name.endsWith(".msi") }?.forEach { it.delete() }

        executable = jdkHome.file("bin/jpackage.exe").asFile.absolutePath
        args(
            "--type", "msi",
            "--input", layout.buildDirectory.dir("jpackage-input").get().asFile.absolutePath,
            "--dest", dest.absolutePath,
            "--name", appDisplayName,
            "--app-version", appVersion,
            "--main-jar", fatJarTask.get().archiveFileName.get(),
            "--main-class", "com.uncommongoods.tugboat.bridge.Main",
            "--icon", layout.projectDirectory.file("src/main/resources/icons/$appIcon").asFile.absolutePath,
            "--license-file", renderedLicense.get().asFile.absolutePath,
            "--vendor", "Uncommon Goods LLC",
            "--description", "Tugboat Bridge Shipping Application",
            "--win-per-user-install",
            "--win-menu",
            "--win-shortcut",
            "--win-menu-group", "Tugboat",
            "--win-upgrade-uuid", upgradeUuid,
            "--java-options", "-Dtugboat.update.channel=$updateChannel",
            "--java-options", "-Dtugboat.update.baseUrl=$updateBaseUrl",
        )
    }

    doLast {
        // jpackage names the MSI "<display name>-<version>.msi"; rename to a
        // space-free artifact name for S3.
        val produced = distDir.get().file("$appDisplayName-$appVersion.msi").asFile
        if (!produced.exists()) {
            throw GradleException("Expected jpackage output not found: $produced")
        }
        val target = distDir.get().file(msiFileName).asFile
        target.delete()
        if (!produced.renameTo(target)) {
            throw GradleException("Failed to rename $produced to $target")
        }
        println("Built installer: $target")
    }
}

val releaseNotes = project.findProperty("releaseNotes")?.toString() ?: ""

val updateMetadata = tasks.register("updateMetadata") {
    group = "distribution"
    description = "Generates latest.json update metadata for the built MSI."
    dependsOn(jpackageMsi)
    notCompatibleWithConfigurationCache("release task")

    doLast {
        val msi = distDir.get().file(msiFileName).asFile
        val digest = MessageDigest.getInstance("SHA-256")
        msi.inputStream().use { input ->
            val buffer = ByteArray(1 shl 16)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        val sha256 = digest.digest().joinToString("") { byte -> "%02x".format(byte) }
        val metadata = linkedMapOf(
            "version" to appVersion,
            "releaseDate" to Instant.now().toString(),
            "installerUrl" to "$updateBaseUrl/windows/$updateChannel/$msiFileName",
            "sha256" to sha256,
            "size" to msi.length(),
            "releaseNotes" to releaseNotes,
        )
        val latestJson = distDir.get().file("latest.json").asFile
        latestJson.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(metadata)))
        println("Wrote update metadata: $latestJson")
    }
}

tasks.register("publishVariant") {
    group = "distribution"
    description = "Builds the \"$appDisplayName\" MSI and uploads it plus latest.json to s3://$updateBucket/updates/windows/$updateChannel/ (requires AWS CLI)."
    dependsOn(updateMetadata)
    notCompatibleWithConfigurationCache("release task; shells out to the AWS CLI")

    doLast {
        // Use full path to AWS CLI to ensure it's found regardless of PATH
        val awsExe = "C:\\Program Files\\Amazon\\AWSCLIV2\\aws.exe"
        fun aws(vararg awsArgs: String) {
            val command = listOf(awsExe) + awsArgs + listOf("--region", updateRegion)
            println(command.joinToString(" "))
            val process = ProcessBuilder(command).inheritIO().start()
            if (process.waitFor() != 0) {
                throw GradleException("Command failed: ${command.joinToString(" ")}")
            }
        }
        val channelUri = "s3://$updateBucket/updates/windows/$updateChannel"
        // MSI first, metadata last: a fetched latest.json always references an
        // installer that already exists.
        aws("s3", "cp", distDir.get().file(msiFileName).asFile.absolutePath, "$channelUri/$msiFileName", "--no-progress")
        aws(
            "s3", "cp", distDir.get().file("latest.json").asFile.absolutePath, "$channelUri/latest.json",
            "--content-type", "application/json", "--cache-control", "no-cache",
        )
        println("Published $appDisplayName $appVersion to $channelUri/")
    }
}
