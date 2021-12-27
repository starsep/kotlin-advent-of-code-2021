plugins {
    kotlin("jvm") version "1.6.0"
}

repositories {
    mavenCentral()
}

tasks {
    sourceSets {
        main {
            java.srcDirs("src")
        }
    }

    wrapper {
        gradleVersion = "7.3"
    }

    dependencies {
        implementation("org.jetbrains.kotlinx:multik-api:0.1.1")
        implementation("org.jetbrains.kotlinx:multik-default:0.1.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC2")
        implementation("org.sosy-lab:java-smt:3.11.0")
        implementation("org.sosy-lab:javasmt-solver-z3:4.8.9-sosy1")
    }
}
