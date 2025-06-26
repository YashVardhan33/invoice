package xa.sh.invoice.invoice.pdfGen;

import java.security.PrivateKey;
import java.security.Signature;

public class SignUtil {
    public static byte[] signSHA256(byte[] hash, PrivateKey key) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);
        signature.update(hash);
        return signature.sign();
    }
}
