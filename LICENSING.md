# Licensing

Tugboat uses a dual-license structure to provide the flexibility to build proprietary extensions while ensuring improvements to the core engine remain open. The repository is split between Mozilla Public License 2.0 (MPL 2.0) and MIT. The applicable license is determined at the file and module level. Each module directory contains an authoritative `LICENSE` file that governs everything in that directory, and files governed by MPL 2.0 include an `SPDX-License-Identifier` header.

## License Summary

| License | Applies to |
|---|---|
| **Mozilla Public License 2.0 (MPL 2.0)** | `engine`, `bridge`, `xo` |
| **MIT License** | `hooks`, `ports`, `adapters/*`, and the three shared build files at the repository root |

## Module Licensing Details

### The Core Applications (MPL 2.0)
**Modules:** `engine`, `bridge`, `xo`

The core engine and host applications are licensed under MPL 2.0, a file-level copyleft license. 
- **Internal Use:** These applications can be modified and run entirely within a company without publishing the source code.
- **Distribution:** Distributing a modified version of these applications requires making the source code of the modified MPL files available.
- **Larger Works:** The Engine can be combined with proprietary code. The MPL only requires publishing modifications made to the Engine's files, not newly added proprietary files.

### The Integration Points (MIT)
**Modules:** `hooks`, `ports`, `adapters/*`

The modules intended for integration are licensed under MIT. Custom adapters and hooks can be written, kept completely private, and linked into the MPL applications without publishing any integration code.

Wiring them in requires no modifications to the core source. The build discovers adapters on its own: a module placed in `adapters/` is picked up by `settings.gradle.kts`, and a prebuilt jar placed in the repository's `libs/` directory joins the Bridge and XO runtime classpath, where `ServiceLoader` finds it. Adding a private adapter therefore requires no changes to any tracked file. See [adapters/README.md](adapters/README.md).

### Shared Build Files (MIT)

Three build files sit at the repository root and belong to no single module: `settings.gradle.kts`, `build.gradle.kts`, and `gradle/libs.versions.toml`. They configure the MPL and MIT modules alike, so they are MIT rather than inheriting either side.

Every other build script inherits its own module's license — `engine/build.gradle.kts` is MPL because `engine` is, `ports/build.gradle.kts` is MIT because `ports` is.

*Note: The same holds for other scripts (e.g., release or tooling scripts), which inherit the license of their parent module as indicated by their SPDX headers.*

## Third-Party Components

The Bridge installer contains OpenJFX and a Java runtime, both licensed under GPLv2 with the Classpath Exception. See [THIRD-PARTY.md](THIRD-PARTY.md) for corresponding source code availability.

## Copyright and Legal Disclaimer

Copyright © 2025 Uncommon Goods LLC.

> **Disclaimer:** This document is a plain-English summary, not legal advice. The `LICENSE` files within each module remain authoritative. If you are relying on this commercially, please consult legal counsel.
