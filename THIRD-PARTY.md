# Third-Party Components and Licenses

Tugboat's source code is licensed under the terms described in [LICENSING.md](LICENSING.md). For a complete list of project dependencies, run `./gradlew dependencies` in the project root.

The Tugboat Bridge application is distributed as a compiled binary that bundles third-party components. This document provides the required notices and source code availability for components licensed under the GNU General Public License v2 (GPLv2).

## GPLv2 Components with Classpath Exception

The Bridge installer includes a bundled Java runtime and specific JavaFX libraries. These components are distributed under the **GPLv2 with the Classpath Exception**, which allows Tugboat's MPL- and MIT-licensed code to link against them and be distributed under Tugboat's own terms. The exception does not waive the GPLv2 obligations that apply to the components themselves, which is why the source citations below are required.

### OpenJFX 21
* **Components:** `org.openjfx:javafx-controls`, `org.openjfx:javafx-fxml`
* **Source Code:** Shipped as unmodified upstream releases. The corresponding source code for the exact version utilized is available at: [https://github.com/openjdk/jfx/tree/21-ga](https://github.com/openjdk/jfx/tree/21-ga)

### Java 21 Runtime Image
* **Component:** A custom Java runtime environment generated from **Eclipse Temurin** (Adoptium) JDK 21.
* **Source Code:** The exact version linked during the build is substituted into the license notice carried inside the installer, from the template at `bridge/legal/installer-license.txt`. Adoptium publishes source archives for every release, which can be found at: [https://github.com/adoptium/temurin21-binaries/releases](https://github.com/adoptium/temurin21-binaries/releases)

<!--
MAINTAINER NOTES:
1. Ensure the OpenJFX source link above remains synced with the version pinned in `bridge/build.gradle.kts`.
2. Do not un-pin `JvmVendorSpec.ADOPTIUM` in the build files. The dynamic source citation depends on a known vendor; removing it would require reinstating a standard GPLv2 "Written Offer for Source Code".
3. `jpackageMsi` is configured to fail rather than emit a notice containing an unsubstituted `@TOKEN@` or `[PLACEHOLDER]`.
-->
