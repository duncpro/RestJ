plugins {
    `java-library`
}

dependencies {
    api("com.duncpro:jroute:1.0-SNAPSHOT-4")
    api("com.google.inject:guice:5.0.1")
    compileOnly("com.google.auto.value:auto-value-annotations:1.9")
    annotationProcessor("com.google.auto.value:auto-value:1.9")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
