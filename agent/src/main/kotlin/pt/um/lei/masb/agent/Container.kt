package pt.um.lei.masb.agent

import jade.core.Profile
import jade.core.ProfileImpl
import jade.core.Runtime
import jade.wrapper.ContainerController
import mu.KLogging
import pt.um.lei.masb.blockchain.BlockChain
import pt.um.lei.masb.blockchain.emptyHash

class Container {
    private lateinit var rt: Runtime
    private lateinit var container: ContainerController

    fun initContainerInPlatform(
        host: String,
        port: String,
        containerName: String
    ): ContainerController {
        // Get the JADE runtime interface (singleton)
        this.rt = Runtime.instance()

        // Create a Profile, where the launch arguments are stored
        val profile = ProfileImpl()
        profile.setParameter(Profile.CONTAINER_NAME, containerName)
        profile.setParameter(Profile.MAIN_HOST, host)
        profile.setParameter(Profile.MAIN_PORT, port)
        // create a non-main agent container
        return rt.createAgentContainer(profile)
    }

    fun initMainContainerInPlatform(
        host: String,
        port: String,
        containerName: String
    ) {

        // Get the JADE runtime interface (singleton)
        this.rt = Runtime.instance()

        // Create a Profile, where the launch arguments are stored
        val prof = ProfileImpl()
        prof.setParameter(Profile.CONTAINER_NAME, containerName)
        prof.setParameter(Profile.MAIN_HOST, host)
        prof.setParameter(Profile.MAIN_PORT, port)
        prof.setParameter(Profile.MAIN, "true")
        prof.setParameter(Profile.GUI, "true")

        // create a main agent container
        this.container = rt.createMainContainer(prof)
        rt.setCloseVM(true)

    }

    fun startAEInPlatform(
        name: String,
        classpath: String,
        bc: BlockChain
    ) {

        try {
            val ac = container.createNewAgent(name, classpath, arrayOf<Any>(bc))
            ac.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {

            val a = Container()
            val bc =
                BlockChain.getBlockChainByHash(hash = emptyHash())
                    ?: BlockChain(id = "smarthub")

            a.initMainContainerInPlatform(
                "localhost",
                "9888",
                "Container"
            )

            a.startAEInPlatform(
                "MinerAgent",
                "pt.um.lei.masb.agent.SingleChainAgent",
                bc
            )

        }
    }


}