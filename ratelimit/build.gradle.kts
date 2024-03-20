oss {
    baseName.set("ratelimit-parent")
}

subprojects {
    version = project.parent!!.version

    oss {
        val bName = project.parent!!.oss.baseName.map { it.replace("-parent", "") }
//        baseName.set(bName.map { "${it}${if (project.name == "api") "" else "-" + project.name}" }.get())
        baseName.set(bName.map { "${it}-${project.name}" })
    }
}
