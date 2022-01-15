plugins {
    java
}

group = "com.duncpro"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.duncpro:jroute:1.0-SNAPSHOT-4")
    implementation("com.google.inject:guice:5.0.1")
    compileOnly("com.google.auto.value:auto-value-annotations:1.9")
    annotationProcessor("com.google.auto.value:auto-value:1.9")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
