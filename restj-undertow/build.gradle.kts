plugins {
    `java-library`
}

version = "1.0-SNAPSHOT"

dependencies {
    api(project(":restj-core"))
    api("io.undertow:undertow-core:2.1.0.Final")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
