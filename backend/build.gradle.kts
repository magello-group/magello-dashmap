import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.utils.findIsInstanceAnd

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val moshi_version: String by project
val exposed_version: String by project

plugins {
  java
  application
  kotlin("jvm") version "1.9.20-RC2"
  id("io.ktor.plugin") version "2.3.5"
  id("com.bmuschko.docker-spring-boot-application") version "9.3.7"
  kotlin("plugin.serialization") version "1.9.20-RC2"
}

group = "se.magello"
version = "0.0.1"

application {
  mainClass.set("io.ktor.server.netty.EngineMain")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
  mavenCentral()
}

kotlin {
  jvmToolchain(21)
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of("21"))
  }
}

dependencies {
  implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-resources-jvm:$ktor_version")
  implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
  implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
  implementation("io.ktor:ktor-client-okhttp:$ktor_version")
  implementation("io.ktor:ktor-client-logging:$ktor_version")
  implementation("io.ktor:ktor-client-resources:$ktor_version")
  implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-cors:$ktor_version")
  implementation("io.ktor:ktor-server-call-logging:$ktor_version")
  implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")

  // sessions since we dont have a frontend
  implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")
  implementation("ch.qos.logback:logback-classic:$logback_version")

  implementation("io.github.microutils:kotlin-logging:2.1.23")

  implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.8.0")

  // cache
  implementation("com.sksamuel.aedile:aedile-core:1.3.0-RC1")
  implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

  // database
  implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
  implementation("org.xerial:sqlite-jdbc:3.40.0.0")

  testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

val createDockerFile = task<Dockerfile>("createDockerFile") {
  from("eclipse-temurin:21-alpine")
  copyFile("./magello-dashmap-all.jar", "/application.jar")
  entryPoint("java")
  defaultCommand("-jar", "/application.jar")
  exposePort(8080)
}

task<Copy>("copyFatJar") {
  dependsOn("build")
  from("$projectDir/build/libs/magello-dashmap-all.jar")
  into("$projectDir/build/docker")
}

task<DockerBuildImage>("buildDockerImage") {
  dependsOn("createDockerFile", "copyFatJar")
  inputDir.set(createDockerFile.destDir)
  images.set(arrayListOf("magello-dashmap:latest"))
}
