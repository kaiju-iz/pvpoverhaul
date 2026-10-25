plugins {
    java
}

group = "dev.pvpoverhaul"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Beta builds change often: check https://papermc.io/downloads/paper for the newest 26.3 build number.
    compileOnly("io.papermc.paper:paper-api:26.3.build.143-beta")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version) }
}
