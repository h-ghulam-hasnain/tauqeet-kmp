# Publishing Guide for Tauqeet KMP

This library is configured to publish to **Maven Central via Sonatype Central Portal** (for Android, JVM, and iOS targets) and **npm** (for the JS/Node.js target).

---

## 1. Prerequisites — GitHub Repository Secrets

Before publishing, you must configure the following **GitHub Repository Secrets** under `Settings → Secrets and variables → Actions`:

| Secret Name | Description |
| :--- | :--- |
| `MAVEN_CENTRAL_USERNAME` | Your Sonatype Central Portal username (usually your email). |
| `MAVEN_CENTRAL_PASSWORD` | Your Sonatype Central Portal **user token** (not your login password — generate one from the Portal UI). |
| `GPG_SIGNING_KEY_ID` | The last 8 characters of your GPG key fingerprint. |
| `GPG_SIGNING_KEY` | Your ASCII-armored GPG **private** key (the full block output of `gpg --armor --export-secret-keys YOUR_KEY_ID`). |
| `GPG_SIGNING_PASSWORD` | The passphrase protecting your GPG key. |

> **Note:** `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD` are your **Central Portal API token credentials**, generated at [central.sonatype.com → Account → Generate User Token](https://central.sonatype.com/account). They are NOT your Sonatype OSSRH (legacy) credentials.

---

## 2. Publishing to Maven Central (Automated via CI/CD)

The release workflow (`.github/workflows/release.yml`) is triggered automatically whenever you push a **version tag** matching `v*`.

### Step-by-step release process:

```bash
# 1. Make sure all code is committed and tests pass locally
./gradlew build

# 2. Update the version in shared/build.gradle.kts
#    coordinates(groupId = "io.github.h-ghulam-hasnain", artifactId = "tauqeet-kmp", version = "0.2.0")

# 3. Commit the version bump
git add shared/build.gradle.kts
git commit -m "chore: bump version to 0.2.0"

# 4. Create and push a version tag — this triggers the publish workflow
git tag v0.2.0
git push origin main --tags
```

The CI pipeline will then:
1. Compile all targets (Android, JVM, iOS, JS).
2. Generate Sources JARs and Javadoc JARs.
3. Sign all artifacts using your in-memory GPG key.
4. Upload and **automatically release** to Maven Central (no manual staging step needed).

---

## 3. Publishing Manually (Local Machine)

If you need to trigger a publish from your local machine, set the required credentials as environment variables and run:

```bash
export ORG_GRADLE_PROJECT_mavenCentralUsername="your_central_portal_username"
export ORG_GRADLE_PROJECT_mavenCentralPassword="your_central_portal_token"
export ORG_GRADLE_PROJECT_signingInMemoryKeyId="ABCDEF01"
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(cat your-private-key.asc)"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="your_gpg_passphrase"

./gradlew :shared:publishAndReleaseToMavenCentral --no-configuration-cache
```

---

## 4. Publishing to npm

The Kotlin/JS target generates a Node.js-compatible package.

```bash
# 1. Build the production JS library
./gradlew :shared:jsBrowserProductionLibraryDistribution

# 2. Navigate to the generated npm package
cd shared/build/dist/js/productionLibrary

# 3. Publish (ensure you're logged in via `npm login`)
npm publish --access public
```

---

## 5. What Gets Published

The following artifacts are generated and signed for each platform target:

| Target | Artifact Type | Sources | Javadoc |
| :--- | :--- | :---: | :---: |
| Android (release) | `.aar` | ✅ | ✅ |
| JVM | `.jar` | ✅ | ✅ (empty stub) |
| iOS arm64 | `.klib` | ✅ | ✅ (empty stub) |
| iOS x64 | `.klib` | ✅ | ✅ (empty stub) |
| iOS Simulator arm64 | `.klib` | ✅ | ✅ (empty stub) |
| JS (IR) | `.klib` + npm | ✅ | ✅ (empty stub) |
| POM | `.pom` | — | — |
| Gradle Module | `.module` | — | — |

All artifacts above are GPG-signed (`.asc` files are generated alongside each).
