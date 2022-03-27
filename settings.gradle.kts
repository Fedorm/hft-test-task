rootProject.name = "hft"
pluginManagement {
    repositories {
        gradlePluginPortal()
        flatDir { dirs("libs") }

    }
}
include("ru.hft.random.gradle.allure")
