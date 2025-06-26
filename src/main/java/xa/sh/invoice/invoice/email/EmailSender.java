package xa.sh.invoice.invoice.email;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
public interface EmailSender {
    void send (String to,String senderEmail, String subject, String htmlBody, String fileName, byte[] pdfBytes);
}
