rootProject.name = "blockchainMain"
include(
    "blockchain",
    "agent"
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
