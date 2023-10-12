dependencies {
    api(project(":core"))
    api(VertxLibs.jsonSchema)

    compileOnly(UtilLibs.jetbrainsAnnotations)

    testImplementation(testFixtures(project(":core")))
}
