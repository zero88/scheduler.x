plugins {
    id("io.github.zero88.qwe.gradle.oss") version "1.0.0"
    id("io.codearte.nexus-staging") version "0.22.0"
    id("org.sonarqube") version "3.1.1"
    `java-test-fixtures`
}

group = "io.github.zero88"

val lombok = "org.projectlombok:lombok:1.18.16"
val vertxVersion = "4.0.0"
val junit5Version = "4.0.0"
val junit5Api = "org.junit.jupiter:junit-jupiter-api:${junit5Version}"
val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:${junit5Version}"
val vertxCore = "io.vertx:vertx-core:${vertxVersion}"
val vertx5Junit = "io.vertx:vertx-junit5:${vertxVersion}"
val jackson = "com.fasterxml.jackson.core:jackson-databind:2.12.0"

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
    api("io.vertx:vertx-core:${vertxVersion}")
    api("org.slf4j:slf4j-api:1.7.30")
    compileOnly(jackson)
    compileOnly(lombok)
    annotationProcessor(lombok)

    testImplementation(junit5Api)
    testImplementation(junit5Engine)
    testImplementation(vertx5Junit)
    testImplementation(vertx5Junit)
    testImplementation(jackson)
    testCompileOnly(lombok)
    testAnnotationProcessor(lombok)

    testFixturesApi("ch.qos.logback:logback-classic:1.2.3")
    testFixturesApi(junit5Api)
    testFixturesApi(junit5Engine)
    testFixturesApi(vertx5Junit)
    testFixturesCompileOnly(lombok)
    testFixturesAnnotationProcessor(lombok)
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

sonarqube {
    properties {
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/test/jacocoTestReport.xml")
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.isEnabled = true
    }
}
tasks.sonarqube {
    dependsOn(tasks.jacocoTestReport)
}
