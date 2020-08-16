import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    kotlin("jvm") version "1.3.72"
    maven
    `java-library`
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
tasks {
    getByName<Upload>("uploadArchivesToLocal") {
        repositories {
            withConvention(MavenRepositoryHandlerConvention::class) {
                mavenDeployer {
                    withGroovyBuilder {
                        "repository"("url" to repositories.mavenLocal().url)
                    }
                    pom.project {
                        withGroovyBuilder {
                            "groupId"("ktjsonrpcpeer")
                            "artifactId"("ktjsonrpcpeer")
                            "version"("1.0")
                        }
                    }
                }
            }
        }
    }
}