plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    `java-library`
    `maven-publish`
}
kotlin {
    explicitApi()
}
repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}
dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0-RC2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    api("com.squareup.okhttp3:okhttp:4.8.1")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}
configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = "ktjsonrpcpeer"
            artifactId = "ktjsonrpcpeer"
            version = "0.3.2"
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/1354092549/ktjsonrpcpeer")
            credentials {
                username = System.getenv("gpr.usr")
                password = System.getenv("gpr.key")
            }
        }
    }
}