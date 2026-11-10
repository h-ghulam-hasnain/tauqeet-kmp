plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.28.0"
    id("org.jetbrains.dokka") version "2.0.0"
    kotlin("npm-publish") version "3.6.0"
    id("signing")
}

group = "io.github.h-ghulam-hasnain"
version = "0.2.1"

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        browser {
            testTask {
                useMocha {
                    timeout = "30s"
                }
            }
        }
        nodejs {
            testTask {
                useMocha {
                    timeout = "30s"
                }
            }
        }
        binaries.library()
        generateTypeScriptDefinitions()
        compilations["main"].packageJson {
            customField("name", "tauqeet")
            customField("version", "0.2.1")
            customField("description", "A high-precision Islamic prayer times and Qibla calculation library.")
            customField("repository", mapOf("type" to "git", "url" to "https://github.com/h-ghulam-hasnain/tauqeet-kmp.git"))
            customField("license", "MIT")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_SIGNING_KEY"),
        System.getenv("GPG_SIGNING_PASSWORD")
    )
}

mavenPublishing {
    configure(com.vanniktech.maven.publish.KotlinMultiplatform(
        javadocJar = com.vanniktech.maven.publish.JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
        sourcesJar = true
    ))

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = "io.github.h-ghulam-hasnain",
        artifactId = "tauqeet-kmp",
        version = "0.2.1"
    )

    pom {
        name.set("Tauqeet KMP")
        description.set("High-precision Islamic prayer-times & Qibla calculation library for Kotlin Multiplatform.")
        inceptionYear.set("2026")
        url.set("https://github.com/h-ghulam-hasnain/tauqeet-kmp")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("h-ghulam-hasnain")
                name.set("Hasnain Ghulam")
                email.set("ghulamhasnain.pro@gmail.com")
                url.set("https://github.com/h-ghulam-hasnain")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/h-ghulam-hasnain/tauqeet-kmp.git")
            developerConnection.set("scm:git:ssh://github.com/h-ghulam-hasnain/tauqeet-kmp.git")
            url.set("https://github.com/h-ghulam-hasnain/tauqeet-kmp")
        }
    }
}

npmPublish {
    registries {
        npmjs {
            authToken = System.getenv("NPM_TOKEN")
        }
    }
    packages {
        named("js") {
            version = "0.2.1"
            packageName = "tauqeet-kmp"
            readme = file("../README.md")
            packageJson {
                license = "MIT"
                homepage = "https://github.com/h-ghulam-hasnain/tauqeet-kmp#readme"
                description = "High-precision Islamic prayer-times & Qibla calculation library for Kotlin Multiplatform."
                repository {
                    type = "git"
                    url = "https://github.com/h-ghulam-hasnain/tauqeet-kmp.git"
                }
            }
        }
    }
}
