package pt.um.lei.masb.blockchain.test;

import org.junit.jupiter.api.Test;
import pt.um.lei.masb.blockchain.BlockChain;

import javax.persistence.spi.PersistenceProviderResolverHolder;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TestBlockchain {
    @Test
    void testBlockchainInit() {
        try {
            var resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
            var providers = resolver.getPersistenceProviders();


            //There is a persistance provider.
            assertTrue(providers.stream().anyMatch(Objects::nonNull));
            providers.forEach(System.out::println);
            //providers.forEach(pr -> System.out.println(pr.getClass().getClassLoader().getResource("META-INF/persistence.xml")));
            //providers.stream().findAny().ifPresent(t -> assertNotNull(t.createEntityManagerFactory(null, null)));
            var bl = BlockChain.getInstance();
            var b = bl.newBlock();
            var c = b.getCoinbase();
            assertNotNull(c);
        } catch (Exception e) {
            fail(e);
        }
    }
}
