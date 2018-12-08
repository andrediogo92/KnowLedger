rootProject.name = "blockchainMain"
include(
    "agent",
    "blockchain"
)

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id ==
                "kotlinx-serialization"
            ) {
                useModule(Libs.serializationModule)
            }
        }
    }
}