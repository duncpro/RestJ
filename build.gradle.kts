allprojects {
    group = "com.duncpro.restj"
    version = "1.0-SNAPSHOT-17"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://duncpro-personal-618824625980.d.codeartifact.us-east-1.amazonaws.com/maven/duncpro-personal/")
            credentials {
                username = "aws"
                password = System.getenv("CODEARTIFACT_AUTH_TOKEN")
            }
        }
    }
}

