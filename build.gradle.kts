buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.novoda:bintray-release:0.9.2")
    }
}

plugins {
    id("org.jetbrains.intellij") version "0.4.16" apply false
    kotlin("jvm") version "1.3.70"
    id("com.jfrog.bintray") version "1.8.4"
}

apply {
    plugin("com.novoda.bintray-release")
}

group = "com.anatawa12.jasm"
if (project.hasProperty("push_release")) {
    version = "1.0.4"
} else {
    version = "1.0.5-SNAPSHOT"
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.ow2.asm:asm-debug-all:5.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        useJUnitPlatform()
    }

    register("javadocJar", Jar::class.java) {
        dependsOn(javadoc.get())
        from(javadoc)
        archiveClassifier.set("javadoc")
    }

    register("sourcesJar", Jar::class.java) {
        dependsOn(javadoc.get())
        from(sourceSets.main.get().allSource)
        archiveClassifier.set("sources")
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
