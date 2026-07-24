plugins {
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
