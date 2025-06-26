package xa.sh.invoice.invoice.pdfGen;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

import xa.sh.invoice.invoice.DTO.FileDto;
import xa.sh.invoice.invoice.DTO.ItemDto;

@Service
public class PdfGenerator {

    public byte[] generateInvoice(FileDto dto) throws Exception {
        // String keystorePath = "keystore.p12";
        // String password = "your-password";
        // String alias = "your-alias";

        // KeyStore ks = KeyStore.getInstance("PKCS12");
        // ks.load(new FileInputStream(keystorePath), password.toCharArray());
        // PrivateKey privateKey = (PrivateKey) ks.getKey(alias,
        // password.toCharArray());

        // Certificate[] chain = ks.getCertificateChain(alias);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // === 1. Company Header with Logo ===
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 1.5f, 3.5f });

        PdfPCell logoCell;
        if (dto.getLogo() != null && !dto.getLogo().isEmpty()) {
            Image logo = Image.getInstance(dto.getLogo().getBytes());
            logo.scaleToFit(80, 80);
            logoCell = new PdfPCell(logo);
        } else {
            logoCell = new PdfPCell(new Phrase("No Logo", normalFont));
        }
        logoCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(logoCell);

        PdfPCell companyInfo = new PdfPCell();
        companyInfo.setBorder(Rectangle.NO_BORDER);
        Paragraph company = new Paragraph(dto.getCompany(), titleFont);
        company.add("\n" + dto.getAddress());
        company.add("\n" + dto.getPhone() + " | " + dto.getEmail());
        company.add("\nGSTIN: " + dto.getGstin());
        companyInfo.addElement(company);
        headerTable.addCell(companyInfo);

        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        // === Invoice number ===
        Paragraph invoicePara = new Paragraph("Invoice No: " + dto.getInvoiceNumber(), titleFont);
        invoicePara.setAlignment(Element.ALIGN_RIGHT);
        document.add(invoicePara);
        document.add(Chunk.NEWLINE);

        // === 2. Customer Info ===
        Paragraph customerInfo = new Paragraph("Bill To:", titleFont);
        customerInfo.add("\n" + dto.getCustomerName());
        customerInfo.add("\nEmail: " + dto.getCustomerEmail());
        customerInfo.add("\nGSTIN: " + dto.getCustomerGstin());
        document.add(customerInfo);
        document.add(Chunk.NEWLINE);

        // === 3. Item Table ===
        PdfPTable itemTable = new PdfPTable(5);
        itemTable.setWidthPercentage(100);
        itemTable.setWidths(new float[] { 3, 1, 1, 1, 1 });
        itemTable.addCell(new Phrase("Item", titleFont));
        itemTable.addCell(new Phrase("Qty", titleFont));
        itemTable.addCell(new Phrase("Rate", titleFont));
        itemTable.addCell(new Phrase("Discount (%)", titleFont));
        itemTable.addCell(new Phrase("Amount", titleFont));

        BigDecimal grandTotal = BigDecimal.ZERO;
        for (ItemDto item : dto.getItems()) {
            itemTable.addCell(new Phrase(item.getItemName(), normalFont));
            itemTable.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
            itemTable.addCell(new Phrase(String.valueOf(item.getPrice()), normalFont));
            itemTable.addCell(new Phrase(String.valueOf(item.getDiscount()), normalFont));

            BigDecimal amount = BigDecimal.valueOf(item.getPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal discount = amount
                    .multiply(BigDecimal.valueOf(item.getDiscount()).divide(BigDecimal.valueOf(100)));
            BigDecimal finalAmount = amount.subtract(discount);
            grandTotal = grandTotal.add(finalAmount);

            itemTable.addCell(new Phrase(finalAmount.toPlainString(), normalFont));
        }

        // Grand Total Row
        PdfPCell totalCell = new PdfPCell(new Phrase("Grand Total", titleFont));
        totalCell.setColspan(4);
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemTable.addCell(totalCell);
        itemTable.addCell(new Phrase(grandTotal.toPlainString(), titleFont));

        document.add(itemTable);
        document.close();

        if (dto.getKeystore() == null || dto.getKeystorePassword() == null || dto.getAlias() == null) {
            return baos.toByteArray();
        }

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(dto.getKeystore().getInputStream(), dto.getKeystorePassword().toCharArray());
        PrivateKey privateKey = (PrivateKey) ks.getKey(dto.getAlias(), dto.getKeystorePassword().toCharArray());
        if (privateKey == null) {
            throw new IllegalArgumentException("Private key not found for alias: " + dto.getAlias());
        }

        Certificate[] chain = ks.getCertificateChain(dto.getAlias());

        if (chain == null || chain.length == 0) {
            throw new IllegalArgumentException("Certificate chain is empty or null for alias: " + dto.getAlias());
        }

        // === 4. Sign and return PDF ===
        return signPdfWithBC(baos.toByteArray(), privateKey, chain);
    }

    public static byte[] signPdfWithBC(byte[] pdfBytes, PrivateKey privateKey, Certificate[] chain) throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }

        PdfReader reader = new PdfReader(pdfBytes);
        ByteArrayOutputStream signedBaos = new ByteArrayOutputStream();
        PdfStamper stamper = PdfStamper.createSignature(reader, signedBaos, '\0', null, true);

        PdfSignatureAppearance sap = stamper.getSignatureAppearance();
        sap.setReason("Invoice Authentication");
        sap.setLocation("India");
        sap.setSignDate(Calendar.getInstance());
        sap.setVisibleSignature(new Rectangle(359, 36, 559, 86), 1, "sig");

        sap.setCrypto(privateKey, chain, null, PdfSignatureAppearance.WINCER_SIGNED);

        stamper.close();

        return signedBaos.toByteArray();
    }


    public byte[] generateInvoiceModified(FileDto dto) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font CompanyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font mediumFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font mediumFont1 = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // === 1. Company Header with Logo ===
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 1.5f, 3.5f });

        PdfPCell logoCell;
        if (dto.getLogo() != null && !dto.getLogo().isEmpty()) {
            Image logo = Image.getInstance(dto.getLogo().getBytes());
            logo.scaleToFit(80, 80);
            logoCell = new PdfPCell(logo);
        } else {
            logoCell = new PdfPCell(new Phrase("", normalFont));
        }
        logoCell.setBorder(Rectangle.BOTTOM);
        headerTable.addCell(logoCell);

        PdfPCell companyInfo = new PdfPCell();
        companyInfo.setBorder(Rectangle.BOTTOM);
        Paragraph company = new Paragraph(dto.getCompany(), CompanyFont);
        company.add(new Phrase("\n" + dto.getAddress(), normalFont));
        company.add(new Phrase("\n" + dto.getPhone() + " | " + dto.getEmail(), normalFont));

        company.add(new Phrase("\nGSTIN: " + dto.getGstin(), mediumFont));
        company.setSpacingAfter(4f);
        companyInfo.addElement(company);
        headerTable.addCell(companyInfo);

        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        // === Invoice number ===

        Paragraph invoicePara = new Paragraph("Invoice No: ", mediumFont);
        invoicePara.add(new Phrase(String.valueOf(dto.getInvoiceNumber()), mediumFont1));
        invoicePara.setAlignment(Element.ALIGN_RIGHT);
        invoicePara.setSpacingAfter(5f);

        Paragraph invoicePara2 = new Paragraph("INVOICE", titleFont);
        invoicePara2.setAlignment(Element.ALIGN_RIGHT);
        //invoicePara2.setSpacingBefore(10f);
        invoicePara2.setSpacingAfter(5f);

        PdfPCell leftInvoiceCell = new PdfPCell(invoicePara2);
        leftInvoiceCell.setBorder(Rectangle.LEFT);
        // leftInvoiceCell.setBorder(Rectangle.TOP);
        leftInvoiceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        leftInvoiceCell.setPaddingRight(20f);
        leftInvoiceCell.setPaddingLeft(5f);

        PdfPCell rightInvoiceCell = new PdfPCell(invoicePara);
        rightInvoiceCell.setBorder(Rectangle.RIGHT);
        // rightInvoiceCell.setBorder(Rectangle.TOP);
        rightInvoiceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightInvoiceCell.setPaddingRight(10f);

        PdfPTable invoiceTable = new PdfPTable(2);
        invoiceTable.setWidthPercentage(100);
        invoiceTable.setWidths(new float[] { 3F, 2F });
        invoiceTable.addCell(leftInvoiceCell);
        invoiceTable.addCell(rightInvoiceCell);
        document.add(invoiceTable);

        // === 2. Customer Info ===
        PdfPTable customerTable = new PdfPTable(1);
        customerTable.setWidthPercentage(100);

        // Customer Details Header Cell
        PdfPCell customerDet = new PdfPCell();
        Paragraph custDet = new Paragraph("Customer Details", mediumFont);
        custDet.setAlignment(Element.ALIGN_CENTER);
        custDet.setSpacingAfter(4f); // Add bottom spacing
        customerDet.addElement(custDet);
        //customerDet.setPadding(8f); // Add inner padding
        customerTable.addCell(customerDet);

        // Helper method to create padded customer info cells
        PdfPCell customName = createCustomerInfoCell("M/s     ", dto.getCustomerName(), mediumFont,mediumFont1);
        

        PdfPCell customEmail = createCustomerInfoCell("Email   ",dto.getCustomerEmail(), mediumFont ,mediumFont1);
        

        PdfPCell customGstin = createCustomerInfoCell("GSTIN  ",dto.getCustomerGstin(), mediumFont,mediumFont1);

        customerTable.addCell(customName);
        customerTable.addCell(customEmail);
        customerTable.addCell(customGstin);

        document.add(customerTable);

        // === 3. Item Table ===
        PdfPTable itemTable = new PdfPTable(5);
        itemTable.setWidthPercentage(100);
        itemTable.setWidths(new float[] { 3, 1, 1, 1, 1 });

        // Header Row with padding
        Stream.of("Item", "Qty", "Rate", "Discount (%)", "Amount")
                .map(header -> {
                    PdfPCell cell = new PdfPCell(new Phrase(header, mediumFont));
                    cell.setPaddingLeft(10f);
                    cell.setPaddingTop(4f);
                    cell.setPaddingBottom(4f);
                    return cell;
                }).forEach(itemTable::addCell);

        // Item rows
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (ItemDto item : dto.getItems()) {
            itemTable.addCell(new PdfPCell(new Phrase(item.getItemName(), normalFont)));
                    //.setPaddingLeft(5f);
            itemTable.addCell(new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont)));
                    //.setPaddingLeft(5f);
            itemTable.addCell(new PdfPCell(new Phrase(String.valueOf(item.getPrice()), normalFont)));;
                    //.setPaddingLeft(5f);
            itemTable.addCell(new PdfPCell(new Phrase(String.valueOf(item.getDiscount()), normalFont)));
                    //.setPaddingLeft(5f);

            BigDecimal amount = BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal discount = amount
                    .multiply(BigDecimal.valueOf(item.getDiscount()).divide(BigDecimal.valueOf(100)));
            BigDecimal finalAmount = amount.subtract(discount);
            grandTotal = grandTotal.add(finalAmount);

            itemTable.addCell(new PdfPCell(new Phrase("₹" + finalAmount.toPlainString(), normalFont)));
                    //.setPaddingLeft(5f);
        }

        // Grand Total Row
        PdfPCell totalCell = new PdfPCell(new Phrase("Grand Total", mediumFont));
        totalCell.setColspan(4);
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCell.setPaddingRight(8f);
        itemTable.addCell(totalCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase("₹" + grandTotal.toPlainString(), mediumFont));
        totalValueCell.setPaddingLeft(5f);
        itemTable.addCell(totalValueCell);

        document.add(itemTable);
        document.close();

        if (dto.getKeystore() == null || dto.getKeystorePassword() == null || dto.getAlias() == null) {
            return baos.toByteArray();
        }

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(dto.getKeystore().getInputStream(), dto.getKeystorePassword().toCharArray());
        PrivateKey privateKey = (PrivateKey) ks.getKey(dto.getAlias(), dto.getKeystorePassword().toCharArray());
        if (privateKey == null) {
            throw new IllegalArgumentException("Private key not found for alias: " + dto.getAlias());
        }

        Certificate[] chain = ks.getCertificateChain(dto.getAlias());

        if (chain == null || chain.length == 0) {
            throw new IllegalArgumentException("Certificate chain is empty or null for alias: " + dto.getAlias());
        }

        // === 4. Sign and return PDF ===
        return signPdfWithBC(baos.toByteArray(), privateKey, chain);
    }

    PdfPCell createCustomerInfoCell(String aa, String bb , Font main, Font second) {
        PdfPCell cell = new PdfPCell();
        Paragraph para = new Paragraph();
        para.setSpacingBefore(4f);
        para.add(new Phrase(aa,main));
        para.add(new Phrase(bb,second));
        cell.addElement(para);
        cell.setPaddingLeft(8f);
        cell.setPaddingTop(4f);
        cell.setPaddingBottom(4f);
        cell.setBorder(Rectangle.BOX);
        return cell;
    }

}
