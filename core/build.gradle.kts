plugins {
    `java-test-fixtures`
}


oss {
    baseName.set("schedulerx")
    title.set("Scheduler.x")
}
val vertxVersion = "4.0.0"
val vertxCore = "io.vertx:vertx-core:${vertxVersion}"
val vertx5Junit = "io.vertx:vertx-junit5:${vertxVersion}"

dependencies {
    api("io.vertx:vertx-core:${vertxVersion}")
    api("org.slf4j:slf4j-api:1.7.30")
    compileOnly(JacksonLibs.databind)
    compileOnly(UtilLibs.jetbrainsAnnotations)
    annotationProcessor(UtilLibs.jetbrainsAnnotations)

    testImplementation(vertx5Junit)
    testImplementation(vertx5Junit)
    testImplementation(JacksonLibs.databind)
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testCompileOnly(UtilLibs.jetbrainsAnnotations)
    testAnnotationProcessor(UtilLibs.jetbrainsAnnotations)

    testFixturesApi(TestLibs.junit5Api)
    testFixturesApi(TestLibs.junit5Engine)
    testFixturesApi(TestLibs.junit5Vintage)
    testFixturesApi(vertx5Junit)
    testFixturesCompileOnly(UtilLibs.jetbrainsAnnotations)
    testFixturesAnnotationProcessor(UtilLibs.jetbrainsAnnotations)
}
