# Publishing Guide for Tauqeet KMP

This library is configured to be published to **Maven Central** (for Android and JVM) and **npm** (for JS/Node.js).

## 1. Publishing to Maven Central

We use the standard `maven-publish` plugin along with the `signing` plugin.

### Prerequisites

You must set the following environment variables:
- `OSSRH_USERNAME`: Your Sonatype OSSRH username.
- `OSSRH_PASSWORD`: Your Sonatype OSSRH password.
- `GPG_SIGNING_KEY`: Your GPG private key in ASCII-armored format.
- `GPG_SIGNING_PASSWORD`: The passphrase for your GPG key.

### Publishing Command

To publish the library to the Sonatype OSSRH staging repository, run:

```bash
./gradlew publishAllPublicationsToOSSRHRepository
```

Once published, you will need to log into the Sonatype Nexus Repository Manager, close the staging repository, and release it to Maven Central.

## 2. Publishing to npm

The Kotlin Multiplatform JS target automatically generates a `package.json` file inside the `build` directory.

### Prerequisites

Ensure you are logged into your npm account locally:
```bash
npm login
```

### Publishing Command

To build the production JS library and publish it to npm:

1. Build the JS production library:
   ```bash
   ./gradlew :shared:jsBrowserProductionLibraryDistribution
   ```
2. Navigate to the generated npm package directory:
   ```bash
   cd shared/build/productionLibrary
   ```
3. Publish to npm:
   ```bash
   npm publish --access public
   ```

*(Note: Depending on the exact Kotlin version, the generated npm package path might be `shared/build/dist/js/productionLibrary` or `shared/build/compileSync/js/main/productionLibrary`. Verify the directory containing the correct `package.json`.)*

## 3. GitHub Actions CI/CD

The repository includes a GitHub Actions workflow `.github/workflows/build.yml` which automatically builds and tests the library on every push.

You can extend this workflow to automatically publish a new release when a Git tag is pushed by configuring GitHub Repository Secrets matching the required environment variables above.
