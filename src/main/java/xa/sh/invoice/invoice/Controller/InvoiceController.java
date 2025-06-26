package xa.sh.invoice.invoice.Controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import xa.sh.invoice.invoice.DTO.FileDto;
import xa.sh.invoice.invoice.DTO.ItemDto;
import xa.sh.invoice.invoice.email.EmailSender;
import xa.sh.invoice.invoice.pdfGen.PdfGenerator;

@RestController
@RequestMapping("/api")
// @CrossOrigin(origins = "https://invoigen.netlify.app/")
public class InvoiceController {

    private final EmailSender emailSender;
    private final PdfGenerator pdfGenerator;

    @Autowired
    public InvoiceController(PdfGenerator pdfGenerator, EmailSender emailSender) {
        this.pdfGenerator = pdfGenerator;
        this.emailSender = emailSender;
    }

    @PostMapping(value = "/invoice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> generateInvoiceOnly(
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "keystore", required = false) MultipartFile keystore,
            @RequestParam(value = "keystorePassword", required = false) String keystorePassword,
            @RequestParam(value = "alias", required = false) String alias,

            @RequestParam("company") String company,
            @RequestParam("address") String address,
            @RequestParam("phone") String phone,
            @RequestParam("email") String email,
            @RequestParam(value="sendEmail",required = false, defaultValue = "false") boolean sendEmail,
            @RequestParam(value="gstin",required = false) String gstin,
            @RequestParam("invoiceNo") int invoice,
            @RequestParam("customerName") String customerName,
            @RequestParam("customerEmail") String customerEmail,
            @RequestParam(value="customerGstin",required = false) String customerGstin,

            @RequestParam("items") String itemsJson
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        List<ItemDto> items = mapper.readValue(itemsJson, new TypeReference<List<ItemDto>>() {});
        FileDto dto = new FileDto();
        dto.setLogo(logo != null && !logo.isEmpty() ? logo : null);
        dto.setKeystore(keystore != null && !keystore.isEmpty() ? keystore : null);
        dto.setKeystorePassword(keystorePassword != null && !keystorePassword.isBlank() ? keystorePassword : null);
        dto.setAlias(alias != null && !alias.isBlank() ? alias : null);

        dto.setCompany(company);
        dto.setAddress(address);
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setGstin(gstin!=null&&!gstin.equals("") ? gstin:"-");
        dto.setInvoiceNumber(invoice);

        dto.setCustomerName(customerName);
        dto.setCustomerEmail(customerEmail);
        dto.setCustomerGstin(customerGstin!=null&&!customerGstin.equals("")?customerGstin:"-");
        dto.setItems(items);

        byte[] pdfBytes = pdfGenerator.generateInvoiceModified(dto);
        if (sendEmail) {
            emailSender.send(
                customerEmail,
                email,
                "Invoice from " + company,
                "Please find the attached invoice from " + company,
                "Invoice_" + invoice + ".pdf",
                pdfBytes
        );
        }
        

        return prepareFileResponse(pdfBytes, "invoice.pdf");
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

