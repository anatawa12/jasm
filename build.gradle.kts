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
    `maven-publish`
    signing
}

group = "com.anatawa12.jasm"
if (project.hasProperty("push_release")) {
    version = "1.0.8"
} else {
    version = "1.0.9-SNAPSHOT"
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
}

java {
    withJavadocJar()
    withSourcesJar()
}

fun isReleaseBuild() = !version.toString().contains("SNAPSHOT")

fun getReleaseRepositoryUrl(): String {
    return project.findProperty("RELEASE_REPOSITORY_URL")?.toString()
        ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
}

fun getSnapshotRepositoryUrl(): String {
    return project.findProperty("SNAPSHOT_REPOSITORY_URL")?.toString()
        ?: "https://oss.sonatype.org/content/repositories/snapshots/"
}

fun Project.configurePublish() {
    publishing.publications.create<MavenPublication>("maven") {
        from(components["java"])
        pom {
            name.set("jasm")
            description.set("a simple, new assembly language.")
            url.set("https://github.com/anatawa12/jasm")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/anatawa12/jasm/blob/master/LICENSE.txt")
                }
            }
            developers {
                developer {
                    id.set("anatawa12")
                    name.set("anatawa12")
                    email.set("anatawa12@icloud.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/anatawa12/jasm.git")
                developerConnection.set("scm:git:ssh://github.com:anatawa12/jasm.git")
                url.set("https://github.com/anatawa12/jasm")
            }
        }
    }

    signing {
        publishing.publications.forEach { publication ->
            sign(publication)
        }
    }

    publishing {
        repositories {
            maven {
                name = "mavenCentral"
                url = uri(if (isReleaseBuild()) getReleaseRepositoryUrl() else getSnapshotRepositoryUrl())

                credentials {
                    username = project.findProperty("com.anatawa12.sonatype.username")?.toString() ?: ""
                    password = project.findProperty("com.anatawa12.sonatype.passeord")?.toString() ?: ""
                }
            }
        }
    }
}

configurePublish()
project(":jasm-gradle-plugin").run {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    afterEvaluate {
        configurePublish()
    }
}
