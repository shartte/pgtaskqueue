
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile("org.postgresql", "postgresql", "42.1.4.jre7")
    compile("org.slf4j", "slf4j-api", "1.7.25")

    compileOnly("com.fasterxml.jackson.core", "jackson-databind", "2.9.3")

    testCompile("org.testng", "testng", "6.13.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
