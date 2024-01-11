// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
}
buildscript {
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
        classpath("com.guardsquare:proguard-gradle:7.1.0")
        classpath("com.android.tools.build:gradle:7.0.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.44")
    }
}