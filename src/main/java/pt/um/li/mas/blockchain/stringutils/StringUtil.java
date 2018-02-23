package pt.um.li.mas.blockchain.stringutils;

import pt.um.li.mas.blockchain.Transaction;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class StringUtil {
  public static Logger LOGGER = Logger.getLogger("StringUtil");

    //Applies Sha256 to a string and returns the result.
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //Applies sha256 to our input,
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
          for (byte aHash : hash) {
            String hex = Integer.toHexString(0xff & aHash);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
          }
          return hexString.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
          LOGGER.log(Level.SEVERE, e.getMessage());
          throw new RuntimeException("Apply SHA256 problem", e);
        }
    }

  //Applies ECDSA Signature and returns the result ( as bytes ).
  public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
    Signature dsa;
    byte[] output;
    try {
      dsa = Signature.getInstance("ECDSA", "BC");
      dsa.initSign(privateKey);
      byte[] strByte = input.getBytes();
      dsa.update(strByte);
      output = dsa.sign();
    } catch (GeneralSecurityException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
      throw new RuntimeException("ECDSA Signature problem", e);
    }
    return output;
  }

  //Verifies a String signature
  public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
    try {
      Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
      ecdsaVerify.initVerify(publicKey);
      ecdsaVerify.update(data.getBytes());
      return ecdsaVerify.verify(signature);
    } catch (GeneralSecurityException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
      throw new RuntimeException("ECDSA Verification problem", e);
    }
  }

  public static String getStringFromKey(Key key) {
    return Base64.getEncoder().encodeToString(key.getEncoded());
  }

  public static String getMerkleRoot(Transaction[] data, int size) {
    int count = size;
    String previousTreeLayer[] = new String[size];
    for(int i=0; i< size; i++) {
      previousTreeLayer[i] = data[i].getTransactionId();
    }
    String treeLayer[] = previousTreeLayer;
    while(count > 1) {
      treeLayer = new String[treeLayer.length-1];
      for(int i=1; i < previousTreeLayer.length; i++) {
        treeLayer[i-1]=applySha256(previousTreeLayer[i-1] + previousTreeLayer[i]);
      }
      count = treeLayer.length;
      previousTreeLayer = treeLayer;
    }
    String merkleRoot = (treeLayer.length == 1) ? treeLayer[0] : "";
    return merkleRoot;
  }

  public static String getDifficultyString(int difficulty) {
    return new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
  }
}
