rootProject.name = "Knowledger"
include(
    "ledger", "agent", "annotations",
    "generation", "ledger-core", "example"
)


pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id ==
                "kotlinx-serialization"
            ) {
                useModule(
                    "org.jetbrains.kotlin:kotlin-serialization:${
                    requested.version
                    }"
                )
            }
        }
    }
}
