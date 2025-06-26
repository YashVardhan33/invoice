package xa.sh.invoice.invoice.Certificate;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class SelfSignedCertGenerator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate generate(String dn, KeyPair keyPair) throws Exception {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (3650L * 24 * 60 * 60 * 1000)); // 10 years

        X500Name subject = new X500Name(dn);
        BigInteger serial = BigInteger.valueOf(new SecureRandom().nextInt() & 0x7fffffff);

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
            subject, serial, now, expiry, subject, keyPair.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
            .build(keyPair.getPrivate());

        return new JcaX509CertificateConverter()
            .setProvider("BC")
            .getCertificate(certBuilder.build(signer));
    }
}

