plugins {
    kotlin("multiplatform") version "2.0.0"
    id("com.android.library") version "8.2.2"
    id("com.vanniktech.maven.publish")
}

group = "com.tauqeet"
version = "0.1.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        browser()
        nodejs()
        binaries.executable()
        generateTypeScriptDefinitions()
        compilations["main"].packageJson {
            customField("name", "tauqeet")
            customField("version", "0.1.0")
            customField("description", "A high-precision Islamic prayer times and Qibla calculation library.")
            customField("repository", mapOf("type" to "git", "url" to "https://github.com/tauqeet/tauqeet-kmp.git"))
            customField("license", "MIT")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Add common dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.tauqeet.library"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = "com.tauqeet",
        artifactId = "tauqeet-kmp",
        version = "0.1.0"
    )

    pom {
        name.set("tauqeet-kmp")
        description.set("High-precision Islamic Prayer Times & WGS-84 Geodesic Qibla direction library for Kotlin Multiplatform.")
        inceptionYear.set("2026")
        url.set("https://github.com/tauqeet/tauqeet-kmp")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("hasnain")
                name.set("Ghulam Hasnain")
            }
        }
        scm {
            url.set("https://github.com/tauqeet/tauqeet-kmp")
            connection.set("scm:git:github.com/tauqeet/tauqeet-kmp.git")
            developerConnection.set("scm:git:ssh://github.com/tauqeet/tauqeet-kmp.git")
        }
    }
}
