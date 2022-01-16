plugins {
    `java-library`
    `maven-publish`
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

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
