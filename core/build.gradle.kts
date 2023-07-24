plugins {
    `java-test-fixtures`
}


oss {
    baseName.set("schedulerx")
    title.set("Scheduler.x")
}

val lombok = "org.projectlombok:lombok:1.18.16"
val vertxVersion = "4.0.0"
val junit5Version = "4.0.0"
val junit5Api = "org.junit.jupiter:junit-jupiter-api:${junit5Version}"
val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:${junit5Version}"
val vertxCore = "io.vertx:vertx-core:${vertxVersion}"
val vertx5Junit = "io.vertx:vertx-junit5:${vertxVersion}"
val jackson = "com.fasterxml.jackson.core:jackson-databind:2.12.0"

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
