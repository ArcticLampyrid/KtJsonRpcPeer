@file:Suppress("UNUSED_VARIABLE")
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.18.3")
    }
}
plugins {
    kotlin("multiplatform") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.20"
    id("com.github.arcticlampyrid.gradle-git-version") version "1.0.4"
    signing
    `maven-publish`
}
apply(plugin = "kotlinx-atomicfu")
group = "com.github.ArcticLampyrid.KtJsonRpcPeer"
kotlin {
    explicitApi()
    jvm()
    js(BOTH) {
        nodejs()
        browser {
            testTask {
                enabled = false
            }
        }
    }
    macosX64()
    linuxX64()
    mingwX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
                implementation("io.ktor:ktor-client-core:2.1.1")
                implementation("io.ktor:ktor-client-websockets:2.1.1")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }
        val jvmMain by getting {
            dependencies {
                compileOnly("com.squareup.okhttp3:okhttp:4.9.3")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.slf4j:slf4j-simple:1.7.36")
                implementation("io.ktor:ktor-client-cio:2.1.1")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }
        val nativeCommonTest by creating {
            dependsOn(commonTest)
        }
        val linuxX64Main by getting {
            dependsOn(nativeCommonMain)
        }
        val linuxX64Test by getting {
            dependsOn(nativeCommonTest)
        }
        val mingwX64Main by getting {
            dependsOn(nativeCommonMain)
        }
        val mingwX64Test by getting {
            dependsOn(nativeCommonTest)
        }
        val macosX64Main by getting {
            dependsOn(nativeCommonMain)
        }
        val macosX64Test by getting {
            dependsOn(nativeCommonTest)
        }
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}
repositories {
    mavenCentral()
}
val emptyJavadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
}
tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Vendor" to "ArcticLampyrid",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version.toString()
        )
    }
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
    findProperty("signingKey")?.let {
        useInMemoryPgpKeys(it.toString(), "")
    }
    sign(publishing.publications)
}