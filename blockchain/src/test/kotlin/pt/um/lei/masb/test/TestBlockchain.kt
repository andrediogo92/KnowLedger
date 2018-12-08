package pt.um.lei.masb.test

import org.junit.jupiter.api.Test

class TestBlockchain {
    @Test
    fun testBlockchainInit() {
/*        try {
            val resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver()
            val providers = resolver.persistenceProviders


            //There is a persistance provider.
            assertTrue(providers.stream().anyMatch(Objects::nonNull))
            providers.forEach(System.out::println)

            //providers.forEach(pr -> System.out.println(pr.getClass().getClassLoader().getResource("META-INF/persistence.xml")));
            //providers.stream().findAny().ifPresent(t -> assertNotNull(t.createEntityManagerFactory(null, null)));

            val bl = BlockChain()
            val s = bl.registerSideChainOf(TemperatureData::class.java).getSideChainOf(TemperatureData::class.java)
            val b = s.newBlock()
            val c = b.coinbase

            assertNotNull(c)
        } catch (e : Exception) {
            fail<Nothing>(e)
        }
*/
    }
}
