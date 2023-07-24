private fun Map<Int, List<Int>>.ver(minor: Int, patch: Int):String = "${minor}.${this[minor]?.get(patch)}"

object PlayioPlugin {

    object Version {

        const val gradlePlugin = "0.2.0"
    }

    const val oss = "cloud.playio.gradle.oss"
    const val root = "cloud.playio.gradle.root"
    const val antora = "cloud.playio.gradle.antora"
    const val pandoc = "cloud.playio.gradle.pandoc"
    const val docgen = "cloud.playio.gradle.docgen"
    const val codegen = "cloud.playio.gradle.codegen"
}

object JacksonLibs {

    object Version {

        const val jackson = "2.12.0"
    }

    const val annotations = "com.fasterxml.jackson.core:jackson-annotations:${Version.jackson}"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:${Version.jackson}"
    const val datetime = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Version.jackson}"
}

object TestLibs {

    object Version {

        const val junit5 = "5.9.1"
    }

    const val junit5Api = "org.junit.jupiter:junit-jupiter-api:${Version.junit5}"
    const val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:${Version.junit5}"
    const val junit5Vintage = "org.junit.vintage:junit-vintage-engine:${Version.junit5}"
    const val junit5Params = "org.junit.jupiter:junit-jupiter-params:${Version.junit5}"

}

object VertxLibs {

    object Version {

        private val pool = mapOf(2 to (0..7).toList(), 3 to (0..6).toList(), 4 to (0..0).toList())
        @JvmField val vertxCore = "4.${pool.ver(3, 5)}"
        @JvmField val vertxSQL = "4.${pool.ver(3, 5)}"
        const val vertxJunit = "4.2.5"
    }

    @JvmField val core = "io.vertx:vertx-core:${Version.vertxCore}"
    @JvmField val junit5 = "io.vertx:vertx-junit5:${Version.vertxJunit}"
}

object ValidationLibs {
    object Version {
        const val api = "3.0.2"
        const val hibernate = "8.0.0.Final"
    }

    const val api = "jakarta.validation:jakarta.validation-api:${Version.api}"
    const val hibernate = "org.hibernate.validator:hibernate-validator:${Version.hibernate}"
}
