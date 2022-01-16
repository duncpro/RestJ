plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api("com.google.inject:guice:5.0.1")
    api(project(":restj-core"))
    api("io.undertow:undertow-core:2.1.0.Final")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
