dependencies {
    api(project(":schedulerx"))
    api(VertxLibs.jsonSchema)
    compileOnly(UtilLibs.jetbrainsAnnotations)

    testImplementation(testFixtures(project(":schedulerx")))
}
