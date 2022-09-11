plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
}

group = "io.github.abvadabra"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

artifacts {
    archives(tasks.jar.get())
    archives(sourceJar.get())
}

signing {
    val signingKey = File(properties["GPG_KEY_FILE"] as String).readText(Charsets.UTF_8)
    val signingPassphase = properties["GPG_SIGNING_PASSPHRASE"] as String

    useInMemoryPgpKeys(signingKey, signingPassphase)
    sign(publishing.publications)
}

object ArtifactMeta {
    const val desc = "A simple/fast stacking box layout library."
    const val license = "Unlicense"
    const val licenseUrl = "https://opensource.org/licenses/unlicense"
    const val githubRepo = "github.com/abvadabra/layout-java"
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

    const val devId = "abvadabra"
    const val devName = "Daniil Dubrovsky"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
            artifact(sourceJar.get())
            artifact(javadocJar.get())
            pom {
                name.set(project.name)
                description.set(ArtifactMeta.desc)
                url.set("https://" + ArtifactMeta.githubRepo)
                licenses {
                    license {
                        name.set(ArtifactMeta.license)
                        url.set(ArtifactMeta.licenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set(ArtifactMeta.devId)
                        name.set(ArtifactMeta.devName)
                    }
                }
                scm {
                    url.set(
                        "https://${ArtifactMeta.githubRepo}.git"
                    )
                    connection.set(
                        "scm:git:git://${ArtifactMeta.githubRepo}.git"
                    )
                    developerConnection.set(
                        "scm:git:git://${ArtifactMeta.githubRepo}.git"
                    )
                }
                issueManagement {
                    url.set("https://${ArtifactMeta.githubRepo}/issues")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri(ArtifactMeta.release))
            snapshotRepositoryUrl.set(uri(ArtifactMeta.snapshot))
            username.set(properties["OSSRH_USERNAME"] as String)
            password.set(properties["OSSRH_PASSWORD"] as String)
        }
    }
}
