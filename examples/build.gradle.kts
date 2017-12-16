import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    compile(project(":core"))

    compile("com.fasterxml.jackson.core", "jackson-databind", "2.9.3")
    compile("org.slf4j", "slf4j-simple", "1.7.25")

    testCompile("org.testng", "testng", "6.13.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
