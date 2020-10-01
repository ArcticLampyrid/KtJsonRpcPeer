plugins {
    kotlin("multiplatform") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    `maven-publish`
}
group = "ktjsonrpcpeer"
version = "0.5.0"
kotlin {
    explicitApi()
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0-RC2")
                implementation("co.touchlab:stately-concurrency:1.1.1")
                implementation("co.touchlab:stately-isolate:1.1.1-a1")
                implementation("co.touchlab:stately-iso-collections:1.1.1-a1")
                compileOnly("io.ktor:ktor-client-core:1.4.1")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                compileOnly("com.squareup.okhttp3:okhttp:4.8.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}
repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}
configure<PublishingExtension> {
    publications.forEach { publication ->
        when (publication) {
            is MavenPublication -> {
                with(publication.pom) {
                    withXml {
                        val root = asNode()
                        root.appendNode("name", "ktjsonrpcpeer")
                        root.appendNode(
                            "description",
                            "ktjsonrpcpeer is a Kotlin library that implements JSON-RPC 2.0 in Peer mode.\n" +
                                    "It's full-duplex, supporting two-way procedure call.\n" +
                                    "It can be work well with WebSocket."
                        )
                        root.appendNode("url", "https://github.com/1354092549/ktjsonrpcpeer")
                    }
                    licenses {
                        license {
                            name.set("BSD 3-Clause \"New\" or \"Revised\" License\n")
                            url.set("https://github.com/1354092549/ktjsonrpcpeer/blob/master/LICENSE.md")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("qiqiworld")
                            name.set("qiqiworld")
                            email.set("1354092549@qq.com")
                        }
                    }
                    scm {
                        url.set("https://github.com/1354092549/ktjsonrpcpeer")
                        connection.set("scm:git:git://github.com/1354092549/ktjsonrpcpeer")
                        developerConnection.set("scm:git:git://github.com/1354092549/ktjsonrpcpeer")
                    }
                }
            }
            else -> {

            }
        }
    }
    repositories {
        maven {
            name = "Bintray"
            url = uri("https://api.bintray.com/maven/qiqiworld/ktjsonrpcpeer/ktjsonrpcpeer/;publish=1;override=1")
            credentials {
                username = System.getenv("BINTRAY_USER")
                password = System.getenv("BINTRAY_API_KEY")
            }
        }
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