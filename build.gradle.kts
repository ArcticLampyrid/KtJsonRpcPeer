plugins {
    kotlin("multiplatform") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    signing
    `maven-publish`
}
group = "com.github.ArcticLampyrid.KtJsonRpcPeer"
if (version.toString() == "unspecified") {
    version = "0.7.0"
}
kotlin {
    explicitApi()
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    js {
        nodejs()
        browser()
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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
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
    }
}
repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}
val emptyJavadocJar = tasks.register<Jar>("emptyJavadocJar") {
    archiveClassifier.set("javadoc")
}
configure<PublishingExtension> {
    publications.withType<MavenPublication>().configureEach {
        artifact(emptyJavadocJar)
        pom {
            name.set("KtJsonRpcPeer")
            description.set(
                "KtJsonRpcPeer is a Kotlin library that implements JSON-RPC 2.0 in Peer mode.\n" +
                        "It's full-duplex, supporting two-way procedure call.\n" +
                        "It can work well with WebSocket."
            )
            url.set("https://github.com/ArcticLampyrid/KtJsonRpcPeer")
            licenses {
                license {
                    name.set("BSD 3-Clause \"New\" or \"Revised\" License")
                    url.set("https://github.com/ArcticLampyrid/KtJsonRpcPeer/blob/main/LICENSE.md")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("ArcticLampyrid")
                    name.set("ArcticLampyrid")
                    email.set("ArcticLampyrid@outlook.com")
                    timezone.set("Asia/Shanghai")
                }
            }
            scm {
                url.set("https://github.com/ArcticLampyrid/KtJsonRpcPeer")
                connection.set("scm:git:git://github.com/ArcticLampyrid/KtJsonRpcPeer.git")
                developerConnection.set("scm:git:ssh://github.com:ArcticLampyrid/KtJsonRpcPeer.git")
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ArcticLampyrid/KtJsonRpcPeer")
            credentials {
                username = System.getenv("gpr.usr")
                password = System.getenv("gpr.key")
            }
        }
        maven {
            name = "Sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("sonatypeUsername").toString()
                password = findProperty("sonatypePassword").toString()
            }
        }
    }
}
signing {
    isRequired = false
    sign(publishing.publications)
}