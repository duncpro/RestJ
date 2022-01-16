plugins {
    `java-library`
    `maven-publish`
}

version = "1.0-SNAPSHOT"

dependencies {
    api("com.amazonaws:aws-lambda-java-events:3.11.0")
    api(project(":restj-core"))
    api("com.amazonaws:aws-lambda-java-core:1.2.1")
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
