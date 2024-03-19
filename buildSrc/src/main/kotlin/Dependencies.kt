private fun Map<Int, List<Int>>.ver(minor: Int, patch: Int): String = "${minor}.${this[minor]?.get(patch)}"

object PlayioPlugin {

    object Version {

        const val gradlePlugin = "0.2.1"
    }

    const val oss = "cloud.playio.gradle.oss"
    const val root = "cloud.playio.gradle.root"
    const val antora = "cloud.playio.gradle.antora"
    const val pandoc = "cloud.playio.gradle.pandoc"
    const val docgen = "cloud.playio.gradle.docgen"
    const val codegen = "cloud.playio.gradle.codegen"
}

object UtilLibs {

    object Version {

        const val jetbrainsAnnotations = "20.1.0"
    }

    const val jetbrainsAnnotations = "org.jetbrains:annotations:${Version.jetbrainsAnnotations}"
}

object LogLibs {

    object Version {

        const val logback = "1.5.3"
    }

    const val logback = "ch.qos.logback:logback-classic:${Version.logback}"
}

object JacksonLibs {

    object Version {

        const val jackson = "2.12.0"
    }

    const val annotations = "com.fasterxml.jackson.core:jackson-annotations:${Version.jackson}"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:${Version.jackson}"
    const val jsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Version.jackson}"
}

object TestLibs {

    object Version {

        const val junit5 = "5.10.2"
        const val pioneer = "2.1.0"
    }

    const val junit5Api = "org.junit.jupiter:junit-jupiter-api:${Version.junit5}"
    const val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:${Version.junit5}"
    const val junit5Vintage = "org.junit.vintage:junit-vintage-engine:${Version.junit5}"
    const val junit5Params = "org.junit.jupiter:junit-jupiter-params:${Version.junit5}"
    const val junitPioneer = "org.junit-pioneer:junit-pioneer:${Version.pioneer}"

}

object VertxLibs {

    object Version {

        private val pool = mapOf(2 to (0..7).toList(), 3 to (0..6).toList(), 4 to (0..5).toList())
        @JvmField val defaultVersion = "4.${pool.ver(4, 4)}"
    }

    @JvmField val core = "io.vertx:vertx-core:${Version.defaultVersion}"
    @JvmField val junit5 = "io.vertx:vertx-junit5:${Version.defaultVersion}"
    @JvmField val rx3 = "io.vertx:vertx-rx-java3:${Version.defaultVersion}"
    @JvmField val jsonSchema = "io.vertx:vertx-json-schema:${Version.defaultVersion}"
}

object MutinyLibs {
    object Version {

        const val mutiny = "2.27.0"
    }

    const val core = "io.smallrye.reactive:smallrye-mutiny-vertx-core:${Version.mutiny}"
}
