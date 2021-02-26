plugins {
    id("io.github.zero88.qwe.gradle.oss") version "1.0.0"
    id("io.codearte.nexus-staging") version "0.22.0"
    id("eclipse")
    id("idea")
}

group = "io.github.zero88"
val lombok = "org.projectlombok:lombok:1.18.16"

repositories {
    mavenLocal()
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    mavenCentral()
    jcenter()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api("io.vertx:vertx-core:4.0.0")
    api("org.slf4j:slf4j-api:1.7.30")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    compileOnly(lombok)
    annotationProcessor(lombok)

    testImplementation(testFixtures("io.github.zero88.qwe:qwe-base:0.6.1-SNAPSHOT"))
    testCompileOnly(lombok)
    testAnnotationProcessor(lombok)
}


qwe {
    zero88.set(true)
    publishingInfo {
        enabled.set(true)
        homepage.set("https://github.com/zero88/vertx-scheduler")
        license {
            name.set("The Apache License, Version 2.0")
            url.set("https://github.com/zero88/vertx-scheduler/blob/master/LICENSE")
        }
        scm {
            connection.set("scm:git:git://git@github.com:zero88/vertx-scheduler.git")
            developerConnection.set("scm:git:ssh://git@github.com:zero88/vertx-scheduler.git")
            url.set("https://github.com/zero88/vertx-scheduler")
        }
    }
}

nexusStaging {
    packageGroup = "io.github.zero88"
    username = project.property("nexus.username") as String?
    password = project.property("nexus.password") as String?
}


