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
