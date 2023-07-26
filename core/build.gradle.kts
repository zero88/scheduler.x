plugins {
    `java-test-fixtures`
}


oss {
    baseName.set("schedulerx")
    title.set("Scheduler.x")
}

dependencies {
    api(VertxLibs.core)
    compileOnly(JacksonLibs.annotations)
    compileOnly(JacksonLibs.databind)
    compileOnly(UtilLibs.jetbrainsAnnotations)

    testImplementation(TestLibs.junit5Params)
    testImplementation(JacksonLibs.databind)
    testImplementation(LogLibs.logback)
    testCompileOnly(UtilLibs.jetbrainsAnnotations)

    testFixturesApi(TestLibs.junit5Api)
    testFixturesApi(TestLibs.junit5Engine)
    testFixturesApi(TestLibs.junit5Vintage)
    testFixturesApi(VertxLibs.junit5)
    testFixturesCompileOnly(UtilLibs.jetbrainsAnnotations)
}
