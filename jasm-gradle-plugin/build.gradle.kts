buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.novoda:bintray-release:0.9.2")
    }
}

plugins {
    kotlin("jvm")

    id("com.gradle.plugin-publish") version "0.11.0"
}

apply {
    plugin("com.novoda.bintray-release")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(gradleApi())
    implementation(project(":"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

configure<com.novoda.gradle.release.PublishExtension> {
    userOrg = "anatawa12"
    setLicences("MIT")
    groupId = project.group.toString()
    artifactId = project.name
    publishVersion = project.version.toString()
    desc = "a simple, new assembly language."
    website = "https://github.com/anatawa12/jasm"
    uploadName = "$groupId.$artifactId"

    bintrayUser = project.findProperty("BINTRAY_USER")?.toString() ?: ""
    bintrayKey = project.findProperty("BINTRAY_KEY")?.toString() ?: ""
}

pluginBundle {
    website = "http://www.gradle.org/"
    vcsUrl = "https://github.com/gradle/gradle"

    description = "Greetings from here!"

    plugins {
        // first plugin
        register("jasmPlugin") {
            id = "com.anatawa12.jasm"
            displayName = "Jasm plugin"
            tags = listOf("language", "assembly")
            version = "${project.version}"
        }
    }

    mavenCoordinates {
        groupId = "${project.group}"
        artifactId = project.name
        version = "${project.version}"
    }
}
