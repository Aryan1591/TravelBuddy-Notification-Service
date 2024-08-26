package com.travelbuddy.notification.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.travelbuddy.notification.DTO.PostDTO;
import com.travelbuddy.notification.DTO.TimelineEntry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * Service class responsible for sending email notifications related to travel posts.
 */
@Component
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender sender;

    /**
     * Asynchronously sends an email notification about a new travel post with a PDF attachment.
     *
     * @param toAddress the recipient's email address
     * @param user      the username of the recipient
     * @param postDTO   the data transfer object containing post details
     */
    @Async
    public void sendMail(String toAddress, String user, PostDTO postDTO) {
        LOGGER.info("SendMail method triggered for user: {} to address: {}", user, toAddress);

        MimeMessage message = sender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
            messageHelper.setTo(toAddress);
            messageHelper.setSubject("New Travel Post Created: " + postDTO.getTitle());

            String emailBody = generateEmailBody(user, postDTO);
            messageHelper.setText(emailBody, true);

            // Generate PDF and attach
            byte[] pdfBytes = generatePdf(user, postDTO);
            messageHelper.addAttachment("TravelItinerary.pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));

            sender.send(message);
            LOGGER.info("Email sent successfully to {}", toAddress);

        } catch (MessagingException e) {
            LOGGER.error("Exception inside sendMail: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Exception while generating PDF: {}", e.getMessage());
        }
    }

    /**
     * Generates the HTML body of the email based on the provided user and post details.
     *
     * @param user    the username of the recipient
     * @param postDTO the data transfer object containing post details
     * @return the HTML string to be used as the email body
     */
    private String generateEmailBody(String user, PostDTO postDTO) {
        StringBuilder emailBody = new StringBuilder();

        // HTML header and styling
        emailBody.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<title>Travel Itinerary Notification</title>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; line-height: 1.6; }")
                .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
                .append("h1, h2, h3 { color: #333; }")
                .append("p { margin: 5px 0; }")
                .append("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }")
                .append("th, td { padding: 8px; text-align: left; border: 1px solid #ddd; }")
                .append("th { background-color: #f4f4f4; }")
                .append("tr:nth-child(even) { background-color: #f9f9f9; }")
                .append(".timeline-entry { margin-bottom: 15px; }")
                .append(".safety { margin-top: 20px; padding: 10px; border: 1px solid #ddd; background-color: #f9f9f9; }")
                .append(".trademark { margin-top: 20px; font-size: 12px; color: #888; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>");

        // Greeting and post details
        emailBody.append("<h1>Hello, ").append(user).append("!</h1>")
                .append("<p>We are excited to inform you about your new travel itinerary. Here are the details:</p>");

        // Post details table
        emailBody.append("<h2>Travel Itinerary:</h2>")
                .append("<table>")
                .append("<tr><th>Field</th><th>Details</th></tr>")
                .append("<tr><td><strong>Title</strong></td><td>").append(postDTO.getTitle()).append("</td></tr>")
                .append("<tr><td><strong>Source</strong></td><td>").append(postDTO.getSource()).append("</td></tr>")
                .append("<tr><td><strong>Destination</strong></td><td>").append(postDTO.getDestination()).append("</td></tr>")
                .append("<tr><td><strong>Start Date</strong></td><td>").append(postDTO.getStartDate()).append("</td></tr>")
                .append("<tr><td><strong>End Date</strong></td><td>").append(postDTO.getEndDate()).append("</td></tr>")
                .append("<tr><td><strong>Days</strong></td><td>").append(postDTO.getDays()).append("</td></tr>")
                .append("<tr><td><strong>Nights</strong></td><td>").append(postDTO.getNights()).append("</td></tr>")
                .append("<tr><td><strong>Amount</strong></td><td>").append(postDTO.getAmount()).append("</td></tr>")
                .append("<tr><td><strong>Admin Name</strong></td><td>").append(postDTO.getAdminName()).append("</td></tr>")
                .append("</table>");

        // Timeline entries table
        if (postDTO.getEvents() != null && !postDTO.getEvents().isEmpty()) {
            emailBody.append("<h2>Events Timeline:</h2>")
                    .append("<table>")
                    .append("<tr><th>Title</th><th>Date</th><th>Details</th></tr>");
            for (TimelineEntry entry : postDTO.getEvents()) {
                emailBody.append("<tr>")
                        .append("<td>").append(entry.getTitle()).append("</td>")
                        .append("<td>").append(entry.getDate()).append("</td>")
                        .append("<td>").append(String.join(", ", entry.getEvents())).append("</td>")
                        .append("</tr>");
            }
            emailBody.append("</table>");
        }

        // Safety rules and precautions
        emailBody.append("<div class='safety'>")
                .append("<h2>Safety Rules and Precautions:</h2>")
                .append("<ul>")
                .append("<li>Always keep your valuables secure and avoid displaying them publicly.</li>")
                .append("<li>Be aware of your surroundings and avoid risky areas.</li>")
                .append("<li>Keep emergency contact numbers handy and know the local emergency services.</li>")
                .append("<li>Stay hydrated and take regular breaks during your travel.</li>")
                .append("<li>Follow local health guidelines and stay informed about any travel advisories.</li>")
                .append("</ul>")
                .append("</div>");

        // Closing tags
        emailBody.append("<p>Thank you for using Travel Buddy! We hope you have a great trip.</p>")
                .append("</div>");

        // Trademark notice
        emailBody.append("<div class='trademark'>")
                .append("<p>© 2024 Travel Buddy. All rights reserved. 'Travel Buddy' is a trademark of Travel Buddy Inc.</p>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return emailBody.toString();
    }

    /**
     * Generates a PDF from the post details and timeline entries.
     *
     * @param user    the username of the recipient
     * @param postDTO the data transfer object containing post details
     * @return a byte array representing the PDF
     * @throws Exception if an error occurs during PDF generation
     */
    /**
     * Generates a PDF from the post details, timeline entries, safety rules, and trademark notice.
     *
     * @param user    the username of the recipient
     * @param postDTO the data transfer object containing post details
     * @return a byte array representing the PDF
     * @throws Exception if an error occurs during PDF generation
     */
    private byte[] generatePdf(String user, PostDTO postDTO) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Create PDF document
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add content
        document.add(new Paragraph("Hello, " + user + "!")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("We are excited to inform you about your new travel itinerary."));

        // Add post details
        document.add(new Paragraph("Travel Itinerary:")
                .setFontSize(16)
                .setBold());
        Table table = new Table(2);
        table.addCell(new Cell().add("Field").setBold());
        table.addCell(new Cell().add("Details").setBold());

        table.addCell("Title");
        table.addCell(postDTO.getTitle());
        table.addCell("Source");
        table.addCell(postDTO.getSource());
        table.addCell("Destination");
        table.addCell(postDTO.getDestination());
        table.addCell("Start Date");
        table.addCell(postDTO.getStartDate().toString());
        table.addCell("End Date");
        table.addCell(postDTO.getEndDate().toString());
        table.addCell("Days");
        table.addCell(String.valueOf(postDTO.getDays()));
        table.addCell("Nights");
        table.addCell(String.valueOf(postDTO.getNights()));
        table.addCell("Amount");
        table.addCell(postDTO.getAmount().toString());
        table.addCell("Admin Name");
        table.addCell(postDTO.getAdminName());
        document.add(table);

        // Add timeline entries
        if (postDTO.getEvents() != null && !postDTO.getEvents().isEmpty()) {
            document.add(new Paragraph("Events Timeline:")
                    .setFontSize(16)
                    .setBold());
            Table timelineTable = new Table(3);
            timelineTable.addCell(new Cell().add("Title").setBold());
            timelineTable.addCell(new Cell().add("Date").setBold());
            timelineTable.addCell(new Cell().add("Details").setBold());

            for (TimelineEntry entry : postDTO.getEvents()) {
                timelineTable.addCell(entry.getTitle());
                timelineTable.addCell(entry.getDate().toString());
                timelineTable.addCell(String.join(", ", entry.getEvents()));
            }
            document.add(timelineTable);
        }

        // Add safety rules and precautions
        document.add(new Paragraph("Safety Rules and Precautions:")
                .setFontSize(16)
                .setBold());
        document.add(new Paragraph("• Always keep your valuables secure and avoid displaying them publicly."));
        document.add(new Paragraph("• Be aware of your surroundings and avoid risky areas."));
        document.add(new Paragraph("• Keep emergency contact numbers handy and know the local emergency services."));
        document.add(new Paragraph("• Stay hydrated and take regular breaks during your travel."));
        document.add(new Paragraph("• Follow local health guidelines and stay informed about any travel advisories."));

        // Add trademark notice
        document.add(new Paragraph("© 2024 Travel Buddy. All rights reserved. 'Travel Buddy' is a trademark of Travel Buddy Inc.")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMarginTop(20));

        // Close PDF document
        document.close();

        return outputStream.toByteArray();
    }

}
