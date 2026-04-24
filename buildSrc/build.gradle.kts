plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.7.3")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "quranengine.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("composeLibrary") {
            id = "quranengine.compose.library"
            implementationClass = "ComposeLibraryConventionPlugin"
        }
    }
}
