package ru.discode.mailbackgroundlibrary.util;

import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import ru.discode.mailbackgroundlibrary.BackgroundMail;

public class MailSender extends javax.mail.Authenticator {

    private String user;
    private String password;
    private Session session;
    private Multipart multipart;

    public MailSender(String user, String password, boolean useDefaultSession, MailBox mailBox, String from) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailBox.smtp);
        props.put("mail.smtp.auth", "true");

        if (from != null) {
            props.put("mail.smtp.from", from);
        }

        props.put("mail.smtp.port", mailBox.port.toString());
//        props.put("mail.smtp.socketFactory.port", "25");
        if (mailBox.ssl) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
//        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = useDefaultSession ? Session.getDefaultInstance(props, this) : Session.getInstance(props, this);
        multipart = new MimeMultipart();
    }

    public MailSender(String user, String password, boolean useDefaultSession, MailBox mailBox) {
        this(user, password, useDefaultSession, mailBox, null);
    }

    public static MailBox buildMailBox(String smtp, Integer port) {
        return MailSender.buildMailBox(smtp, port, false);
    }

    static {
        Security.addProvider(new JSSEProvider());
    }

    public static MailBox buildMailBox(String smtp, Integer port, Boolean ssl) {
        MailBox mailBox = new MailBox();
        mailBox.smtp = smtp;
        mailBox.port = port;
        mailBox.ssl = ssl;
        return mailBox;
    }

    public static class MailBox {
        public String smtp;
        public Integer port;
        public Boolean ssl;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String senderEmail, String senderName,
                                      String mailTo, String mailCc, String mailBcc, String type) throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(TextUtils.isEmpty(senderName) ?
                new InternetAddress(senderEmail) :
                new InternetAddress(senderEmail, senderName));
        message.setSubject(subject);

        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), type));
        message.setDataHandler(handler);

        if (multipart.getCount() > 0 || BackgroundMail.TYPE_HTML.equalsIgnoreCase(type)) {
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            if (BackgroundMail.TYPE_HTML.equalsIgnoreCase(type)) {
                // Content type has to be set after the message is put together
                // Then saveChanges() must be called for it to take effect
                messageBodyPart.setHeader("Content-Type", type);
                message.saveChanges();
            }
        } else {
            // text/plain
            message.setText(body);
        }

        if (!TextUtils.isEmpty(mailTo)) {
            if (mailTo.indexOf(',') > 0) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
            } else {
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
            }
        }

        if (!TextUtils.isEmpty(mailCc)) {
            if (mailCc.indexOf(',') > 0) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(mailCc));
            } else {
                message.setRecipient(Message.RecipientType.CC, new InternetAddress(mailCc));
            }
        }

        if (!TextUtils.isEmpty(mailBcc)) {
            if (mailBcc.indexOf(',') > 0) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(mailBcc));
            } else {
                message.setRecipient(Message.RecipientType.BCC, new InternetAddress(mailBcc));
            }
        }

        Transport.send(message);
    }

    public void addAttachment(String filename) throws Exception {
        BodyPart fileBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        fileBodyPart.setDataHandler(new DataHandler(source));
        fileBodyPart.setFileName(source.getName());

        multipart.addBodyPart(fileBodyPart);
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            return type != null ? type : "application/octet-stream";
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}
