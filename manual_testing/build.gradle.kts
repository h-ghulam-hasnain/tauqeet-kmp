plugins {
    kotlin("jvm") version "1.9.22"
    application
}

dependencies {
    implementation(project(":shared"))
}

application {
    mainClass.set("com.tauqeet.manual.prayer.PrayerDefaultValuesKt")
}
