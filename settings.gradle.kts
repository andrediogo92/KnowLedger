rootProject.name = "Knowledger"
include(
    "ledger", "agent", "annotations",
    "generation", "common"
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
