plugins {
    id("java")
    id("application")
}

group = "io.github.abvadabra"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("io.github.layout.demo.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("io.github.spair:imgui-java-app:1.86.4")
    implementation(project.parent!!)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
