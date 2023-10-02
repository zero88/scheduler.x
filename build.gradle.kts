import cloud.playio.gradle.NexusConfig
import cloud.playio.gradle.NexusVersion

plugins {
    eclipse
    idea
    id(PlayioPlugin.oss) version PlayioPlugin.Version.gradlePlugin
    id(PlayioPlugin.root) version PlayioPlugin.Version.gradlePlugin
    id(PlayioPlugin.antora) version PlayioPlugin.Version.gradlePlugin apply false
    id(PlayioPlugin.pandoc) version PlayioPlugin.Version.gradlePlugin apply false
    id(PlayioPlugin.codegen) version PlayioPlugin.Version.gradlePlugin apply false
    id(PlayioPlugin.docgen) version PlayioPlugin.Version.gradlePlugin apply false
}

project.ext.set("baseName", (gradle as ExtensionAware).extensions["BASE_NAME"] as String)
project.ext.set(NexusConfig.NEXUS_VERSION_KEY, NexusVersion.BEFORE_2021_02_24)

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

allprojects {
    group = project.ext.get("projectGroup") as String

    repositories {
        mavenLocal()
        maven { url = uri("https://maven-central-asia.storage-download.googleapis.com/maven2/") }
        maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
        mavenCentral()
    }
    val skipPublish = (gradle as ExtensionAware).extensions["SKIP_PUBLISH"] as Array<*>
    sonarqube {
        isSkipProject = project.path in skipPublish
    }

    tasks {
        withType<AbstractPublishToMaven> {
            enabled = project != rootProject && project.path !in skipPublish
        }
    }
}

subprojects {
    apply(plugin = PlayioPlugin.oss)

    dependencies {
        testImplementation(TestLibs.junit5Api)
        testImplementation(TestLibs.junit5Engine)
        testImplementation(TestLibs.junit5Vintage)
    }

    oss {
        zero88.set(true)
        github.set(true)
        publishing {
            homepage.set("https://github.com/zero88/scheduler.x")
        }
        testLogger {
            slowThreshold = 7000
        }
    }
}
