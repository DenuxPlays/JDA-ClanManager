plugins {
    java
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.denux"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
    maven(url = "https://m2.dv8tion.net/releases")
    maven(url = "https://jitpack.io")
}

dependencies {
    //Database stuff
    api("com.h2database:h2:2.1.214")
    api("com.zaxxer:HikariCP:5.0.1")
    // Quartz scheduler
    api("org.quartz-scheduler:quartz:2.3.2")

    implementation("net.dv8tion:JDA:5.0.0-alpha.15") {
        exclude( module = "opus-java")
    }

    testImplementation("org.postgresql:postgresql:42.4.0")
    testImplementation("ch.qos.logback:logback-classic:1.2.11")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.withType<Test> { useJUnitPlatform() }
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}