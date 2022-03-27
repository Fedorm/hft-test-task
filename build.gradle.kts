import LibraryVersions.allureJavaCommons
import LibraryVersions.assertj
import LibraryVersions.jackson
import LibraryVersions.jupiterApi
import LibraryVersions.restAssured
import LibraryVersions.testContainers

plugins {
    kotlin("jvm") version Platform.kotlin
    id("io.qameta.allure") version PluginVersions.allure
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

allure {
    autoconfigure = true
    version = PluginVersions.allure
}

dependencies {
    testImplementation("io.qameta.allure:allure-junit5:$allureJavaCommons")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterApi}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterApi}")
    testImplementation("org.testcontainers:testcontainers:${testContainers}")
    testImplementation("org.testcontainers:junit-jupiter:${testContainers}")
    testImplementation("io.rest-assured:rest-assured:${restAssured}")
    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:${jackson}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
