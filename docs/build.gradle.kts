import cloud.playio.gradle.antora.tasks.AntoraCopyTask
import cloud.playio.gradle.generator.docgen.AsciidocGenTask

plugins {
    id(PlayioPlugin.antora)
    id(PlayioPlugin.docgen)
}

dependencies {
    compileOnly(project(":schedulerx"))
    compileOnly(VertxLibs.rx3)
    compileOnly(MutinyLibs.core)
    compileOnly(UtilLibs.jetbrainsAnnotations)
}

documentation {
    val extensions = (gradle as ExtensionAware).extensions
    val mainProject = extensions["BASE_NAME"] as String
    antora {
        asciiAttributes.set(
            mapOf(
                "project-group" to project.group,
                "project-name" to mainProject,
                "project-version" to project.version,
                "vertx-version" to VertxLibs.Version.defaultVersion,
            )
        )
        javadocTitle.set("${project.ext["title"]} ${project.version} API")
        javadocProjects.set((extensions["PROJECT_POOL"] as Map<*, Array<String>>)[mainProject]!!.map(project::project))
    }

//    pandoc {
//        from.set(FormatFrom.markdown)
//        to.set(FormatTo.asciidoc)
//        inputFile.set(rootDir.resolve("CHANGELOG.md"))
//        outFile.set("pg-changelog.adoc")
//        config {
//            arguments.set(arrayOf("--trace"))
//        }
//    }
}

tasks {
//    named<AntoraCopyTask>("antoraPages") { from(withType<PandocTask>()) }
    named<AntoraCopyTask>("antoraPartials") {
        from(withType<AsciidocGenTask>())
        include("*.adoc")
    }
}
