plugins {
    kotlin("jvm") version "2.0.0"
    application
}

dependencies {
    implementation(project(":shared"))
}

application {
    val target = project.findProperty("mainClass") as String?
        ?: "com.tauqeet.manual.prayer.PrayerDefaultValuesKt"
    mainClass.set(target)
}
