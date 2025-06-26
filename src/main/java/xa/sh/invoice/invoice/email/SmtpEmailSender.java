package xa.sh.invoice.invoice.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class SmtpEmailSender implements EmailSender{

    private final JavaMailSender mailSender;

    @Autowired
    public SmtpEmailSender(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }



    @Override
    public void send(String to,String senderEmail, String subject, String htmlBody, String fileName, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setTo(to);
            helper.setCc(senderEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody,true);
            ByteArrayResource pdf = new ByteArrayResource(pdfBytes);
            helper.addAttachment(fileName, pdf);

            mailSender.send(message);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to send mail",e);

        }
    }
}
