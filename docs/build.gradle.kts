import cloud.playio.gradle.antora.tasks.AntoraCopyTask
import cloud.playio.gradle.generator.docgen.AsciidocGenTask

plugins {
    id(PlayioPlugin.antora)
    id(PlayioPlugin.docgen)
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(VertxLibs.rx3)
    compileOnly(MutinyLibs.core)
}

documentation {
    val extensions = (gradle as ExtensionAware).extensions
    val mainProject = extensions["BASE_NAME"] as String
    antora {
        asciiAttributes.set(
            mapOf(
                "project-group" to project.group,
                "project-name" to mainProject,
                "project-version" to project.version
            )
        )
        javadocTitle.set("${project.ext["title"]} ${project.version} API")
        javadocProjects.set((extensions["PROJECT_POOL"] as Map<*, Array<String>>)[mainProject]!!.map(project::project))
    }
}

tasks {
    named<AntoraCopyTask>("antoraPartials") {
        from(withType<AsciidocGenTask>())
        include("*.adoc")
    }
}
