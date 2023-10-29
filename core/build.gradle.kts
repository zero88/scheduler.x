import cloud.playio.gradle.generator.codegen.SourceSetName

plugins {
    `java-test-fixtures`
    id(PlayioPlugin.codegen)
}


oss {
    baseName.set("schedulerx")
    title.set("Scheduler.x")
}

codegen {
    vertx {
        version.set(VertxLibs.Version.defaultVersion)
        sources.addAll(arrayOf(SourceSetName.MAIN))
    }
}

dependencies {
    api(VertxLibs.core)
    compileOnly(JacksonLibs.annotations)
    compileOnly(JacksonLibs.databind)
    compileOnly(UtilLibs.jetbrainsAnnotations)
    codeGenerator(VertxLibs.rx3)
    codeGenerator(MutinyLibs.core)

    testImplementation(TestLibs.junit5Params)
    testImplementation(TestLibs.junitPioneer)
    testImplementation(JacksonLibs.databind)
    testImplementation(JacksonLibs.jsr310)
    testImplementation(LogLibs.logback)
    testCompileOnly(UtilLibs.jetbrainsAnnotations)

    testFixturesApi(TestLibs.junit5Api)
    testFixturesApi(TestLibs.junit5Engine)
    testFixturesApi(TestLibs.junit5Vintage)
    testFixturesApi(VertxLibs.junit5)
    testFixturesCompileOnly(UtilLibs.jetbrainsAnnotations)
}
