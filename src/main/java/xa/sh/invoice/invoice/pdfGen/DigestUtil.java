package xa.sh.invoice.invoice.pdfGen;

import java.io.InputStream;
import java.security.MessageDigest;

public class DigestUtil {
    public static byte[] digestSHA256(InputStream data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int n;
        while ((n = data.read(buffer)) > 0) {
            md.update(buffer, 0, n);
        }
        return md.digest();
    }
}
