buildscript {
    repositories {
        mavenCentral()
        maven("https://maven.google.com")
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}
val buildToolsVersion by extra("34.0.0")

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
