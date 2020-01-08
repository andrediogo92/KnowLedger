plugins {
    kotlin("jvm")
    id(Plugins.docs)
}

version = "0.1"

docsPlugin {
    module = "agent-core"
}

dependencies {
    Libs.jade.forEach(::implementation)
    implementation(project(":base64-extensions"))
}
