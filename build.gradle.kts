plugins {
    kotlin("multiplatform") version "2.0.0" apply false
    kotlin("jvm") version "2.0.0" apply false
    id("com.android.library") version "8.2.2" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
