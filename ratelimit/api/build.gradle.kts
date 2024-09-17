oss {
    title.set("The rate-limit API")
}

dependencies {
    api(VertxLibs.core)
    compileOnly(JacksonLibs.annotations)
    compileOnly(JacksonLibs.databind)
    compileOnly(UtilLibs.jetbrainsAnnotations)
}
