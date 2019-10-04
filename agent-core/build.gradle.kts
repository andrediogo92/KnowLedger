plugins {
    kotlin("jvm")
    id("org.knowledger.plugin.docs")
}

version = "0.1"

repositories {
    maven("https://jade.tilab.com/maven")
}

docs {
    module = "agent-core"
}

dependencies {
    Libs.jade.forEach(::implementation)
}
