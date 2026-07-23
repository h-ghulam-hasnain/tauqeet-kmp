import re

with open('shared/build.gradle.kts', 'r') as f:
    content = f.read()

# Add plugins
content = re.sub(
    r'id\("com\.android\.library"\) version "8\.2\.2"',
    r'id("com.android.library") version "8.2.2"\n    `maven-publish`\n    signing',
    content
)

# Add group and version
content = re.sub(
    r'kotlin \{',
    r'group = "com.tauqeet"\nversion = "0.1.0"\n\nkotlin {',
    content
)

# Add js package json
js_config = """        compilations["main"].packageJson {
            customField("name", "tauqeet")
            customField("version", "0.1.0")
            customField("description", "A high-precision Islamic prayer times and Qibla calculation library.")
            customField("repository", mapOf("type" to "git", "url" to "https://github.com/tauqeet/tauqeet-kmp.git"))
            customField("license", "MIT")
        }"""
content = re.sub(
    r'generateTypeScriptDefinitions\(\)\s*// produces \.d\.ts files',
    r'generateTypeScriptDefinitions()\n' + js_config,
    content
)

# Add publishing block at the end
publishing_block = """
publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("Tauqeet KMP")
                description.set("A high-precision Islamic prayer times and Qibla calculation library for Kotlin Multiplatform.")
                url.set("https://github.com/tauqeet/tauqeet-kmp")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("hasnain")
                        name.set("Hasnain")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/tauqeet/tauqeet-kmp.git")
                    developerConnection.set("scm:git:ssh://github.com/tauqeet/tauqeet-kmp.git")
                    url.set("https://github.com/tauqeet/tauqeet-kmp")
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = System.getenv("GPG_SIGNING_KEY")
    val signingPassword = System.getenv("GPG_SIGNING_PASSWORD")
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
"""

content += publishing_block

with open('shared/build.gradle.kts', 'w') as f:
    f.write(content)
