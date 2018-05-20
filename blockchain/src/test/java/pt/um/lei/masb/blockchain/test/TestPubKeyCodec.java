package pt.um.lei.masb.blockchain.test;

import org.junit.jupiter.api.Test;
import pt.um.lei.masb.blockchain.Ident;
import pt.um.lei.masb.blockchain.utils.StringUtil;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Tests for encoding/decoding the public keys.
 */
public class TestPubKeyCodec {

    @Test
    void TestEncodeDecode() {
        Ident t = new Ident();
        String enc = StringUtil.getStringFromKey(t.getPublicKey());
        PublicKey p = StringUtil.stringToPublicKey(enc);
        //Re-encode matches original encode.
        assertEquals(enc, StringUtil.getStringFromKey(p));
        //Keys match.
        assertEquals(t.getPublicKey(), p);
    }
}
