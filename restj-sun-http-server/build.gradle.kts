plugins {
    `java-library`
}

group = "com.duncpro"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":restj-core"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
