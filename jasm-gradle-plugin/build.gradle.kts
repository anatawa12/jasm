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

java {
    withJavadocJar()
    withSourcesJar()
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

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf { publication.name != "jasmPluginPluginMarkerMaven" }
}
