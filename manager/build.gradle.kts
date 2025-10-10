dependencies {
    api(project(":schedulerx"))
    api(VertxLibs.jsonSchema)
    api(JacksonLibs.databind)
    api(JacksonLibs.jsr310)
    compileOnly(UtilLibs.jetbrainsAnnotations)

    testImplementation(testFixtures(project(":schedulerx")))
}
