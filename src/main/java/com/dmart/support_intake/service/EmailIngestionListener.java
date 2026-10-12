package com.dmart.support_intake.service;

import com.dmart.support_intake.dto.EventRequest;
import jakarta.mail.*;
import jakarta.mail.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Properties;

@Service
public class EmailIngestionListener {

    private static final Logger log = LoggerFactory.getLogger(EmailIngestionListener.class);

    @Value("${email.imap.host}")
    private String host;

    @Value("${email.imap.port}")
    private String port;

    @Value("${email.imap.username}")
    private String username;

    @Value("${email.imap.password}")
    private String password;

    private final EventService eventService;
    private final EmailPayloadTransformer payloadTransformer;

    public EmailIngestionListener(EventService eventService, EmailPayloadTransformer payloadTransformer) {
        this.eventService = eventService;
        this.payloadTransformer = payloadTransformer;
    }

    @Scheduled(fixedDelayString = "${email.imap.poll-rate-ms:30000}")
    public void pollInbox() {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", host);
        properties.put("mail.imaps.port", port);
        properties.put("mail.imaps.ssl.enable", "true");

        Store store = null;
        Folder inbox = null;

        try {
            Session session = Session.getInstance(properties);
            store = session.getStore("imaps");
            store.connect(host, username, password);

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // 1. Filter for UNREAD messages
            SearchTerm unreadTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

            // 2. Filter for emails received within the last 24 hours to ignore older inbox clutter
            long currentMillis = System.currentTimeMillis();
            Date oneDayAgo = new Date(currentMillis - (24 * 60 * 60 * 1000));
            SearchTerm recentTerm = new ReceivedDateTerm(ComparisonTerm.GE, oneDayAgo);

            // Combine search conditions
            SearchTerm searchTerm = new AndTerm(unreadTerm, recentTerm);
            Message[] messages = inbox.search(searchTerm);

            for (Message message : messages) {
                try {
                    // Step 1: Input Transformation (Returns null if email lacks DMart identifiers)
                    EventRequest request = payloadTransformer.transform(message);

                    // Step 2: Skip non-support / promotional emails
                    if (request == null) {
                        log.debug("Skipping non-DMart email: {}", message.getSubject());
                        message.setFlag(Flags.Flag.SEEN, true);
                        continue;
                    }

                    // Step 3: Execute Service Layer logic
                    eventService.processEvent(request);
                    log.info("Successfully ingested and processed email: {}", message.getSubject());

                    // Mark as READ after successful processing
                    message.setFlag(Flags.Flag.SEEN, true);

                } catch (Exception e) {
                    log.error("Failed to process email message: {}", message.getSubject(), e);
                    // Mark as READ on failure to prevent continuous retry loop on malformed email
                    message.setFlag(Flags.Flag.SEEN, true);
                }
            }

        } catch (Exception e) {
            log.error("Error connecting to IMAP server or reading inbox", e);
        } finally {
            // Ensure IMAP connections are properly closed
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (MessagingException me) {
                log.error("Error closing IMAP resources", me);
            }
        }
    }
}