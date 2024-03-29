buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.denux"
val archivesBaseName = "jda-clanmanager"
version = "1.0.0-beta.3"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing {
    publications {
        register("Release", MavenPublication::class) {
            from(components["java"])

            artifactId = archivesBaseName
            groupId = group as String
            version = version as String
        }
    }
}

repositories {
    mavenCentral()
    maven(url = "https://m2.dv8tion.net/releases")
    maven(url = "https://jitpack.io")
}

dependencies {
    //Database stuff
    api("com.h2database:h2:2.1.214")
    api("org.hibernate.orm:hibernate-hikaricp:6.1.4.Final")
    api("org.hibernate.orm:hibernate-core:6.1.4.Final")
    api("com.zaxxer:HikariCP:5.0.1")
    // Quartz scheduler
    api("org.quartz-scheduler:quartz:2.3.2")

    api("net.dv8tion:JDA:5.0.0-alpha.21") {
        exclude( module = "opus-java")
    }
    //Javax annotations
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    testImplementation("org.postgresql:postgresql:42.5.0")
    testImplementation("ch.qos.logback:logback-classic:1.4.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.withType<Test> { useJUnitPlatform() }
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}