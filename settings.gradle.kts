rootProject.name = "Knowledger"
include(
    "agent", "agent:ontologies", "agent:publish", "annotations", "benchmarks",
    "collections-extensions", "encoding-extensions", "example", "ledger", "ledger:core",
    "ledger:core:crypto", "ledger:core:data", "ledger:core:db", "ledger:core:data-serial",
    "ledger:orient", "ledger:storage", "generation", "results", "testing"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}
