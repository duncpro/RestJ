plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api("com.google.inject:guice:5.0.1")
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
        repositories {
            maven {
                url = uri("https://duncpro-personal-618824625980.d.codeartifact.us-east-1.amazonaws.com/maven/duncpro-personal/")
                credentials {
                    username = "aws"
                    password = System.getenv("CODEARTIFACT_AUTH_TOKEN")
                }
            }
        }
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
