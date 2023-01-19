val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val moshi_version: String by project

plugins {
  application
  kotlin("jvm") version "1.7.22"
  id("io.ktor.plugin") version "2.1.3"
  kotlin("plugin.serialization") version "1.7.22"
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
  implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-cors:$ktor_version")
  implementation("io.ktor:ktor-server-call-logging:$ktor_version")


  // sessions since we dont have a frontend
  implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")
  implementation("ch.qos.logback:logback-classic:$logback_version")

  implementation("io.github.microutils:kotlin-logging:2.1.23")

  // cache
  implementation("com.sksamuel.aedile:aedile-core:1.1.2")
  implementation("com.github.ben-manes.caffeine:caffeine:3.1.2")
  implementation("io.ktor:ktor-server-sessions-jvm:2.1.3")
  implementation("io.ktor:ktor-server-cors-jvm:2.1.3")
  implementation("io.ktor:ktor-server-call-logging-jvm:2.1.3")
  implementation("io.ktor:ktor-client-logging-jvm:2.1.3")

  testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}