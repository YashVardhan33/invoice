package xa.sh.invoice.invoice.Controller;


import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import xa.sh.invoice.invoice.Certificate.SelfSignedCertGenerator;

@RestController
@RequestMapping("/api/keystore")
// @CrossOrigin(origins = "https://invoigen.netlify.app/")
public class KeyStoreController {

    @GetMapping("/generate")
    public ResponseEntity<Resource> generateKeystore(
            @RequestParam String alias,
            @RequestParam String password,
            @RequestParam String cn) throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X509Certificate cert = SelfSignedCertGenerator.generate("CN=" + cn, keyPair);

        System.setProperty("org.bouncycastle.pkcs12.legacy", "true");
        KeyStore ks = KeyStore.getInstance("PKCS12","BC");
        ks.load(null, null);
        ks.setKeyEntry(alias, keyPair.getPrivate(), password.toCharArray(), new Certificate[]{cert});

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ks.store(baos, password.toCharArray());

        // return ResponseEntity.ok()
        //         .header("Content-Disposition", "attachment; filename=" + alias + ".p12")
        //         .header("Content-Type", "application/x-pkcs12")
        //         .body(baos.toByteArray());

        return prepareFileResponse(baos.toByteArray(), "myKeyStore.p12");
    }



    private ResponseEntity<Resource> prepareFileResponse(byte[] data, String filename) {
    String finalName = filename;
    
    ByteArrayResource resource = new ByteArrayResource(data);
    return ResponseEntity.ok()
                         .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalName + "\"")
                         .contentType(MediaType.APPLICATION_OCTET_STREAM)
                         .body(resource);
}
}

