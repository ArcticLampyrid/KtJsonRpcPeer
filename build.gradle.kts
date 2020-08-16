plugins {
    kotlin("jvm") version "1.3.72"
    `java-library`
    `maven-publish`
}
repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}
dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    api("com.google.code.gson:gson:2.8.6")
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
            version = "0.1"
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