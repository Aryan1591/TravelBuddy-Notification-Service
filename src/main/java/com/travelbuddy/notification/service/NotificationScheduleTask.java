package com.travelbuddy.notification.service;

import com.travelbuddy.notification.DTO.PostDTO;
import com.travelbuddy.notification.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationScheduleTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationScheduleTask.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserEmailProxy proxy;

    @Autowired
    private PostIdProxy postProxy;

    /**
     * Scheduled method to fetch post data and process notifications.
     * Runs daily at midnight (00:00) in the Asia/Kolkata time zone.
     */
    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Kolkata")
    public void schedule() {
        LOGGER.info("Starting scheduled task to fetch post data.");
        fetchPostData();
    }

    /**
     * Fetches all travel post data from MongoDB and processes notifications.
     */
    public void fetchPostData() {
        LOGGER.info("Fetching post data from MongoDB.");
        List<PostDTO> postDTOList = mongoTemplate.findAll(PostDTO.class, "travelbuddy.posts");

        postDTOList.parallelStream().forEach(post -> {
            if (isPostActive(post) && dateBeforeFor24hrs(post)) {
                LOGGER.info("Post ID: {} is within 24 hours. Triggering sendTripReminder.", post.getId());
                sendTripReminder(post);
            } else if (isTripCompleted(post)) {
                LOGGER.info("Post ID: {} is completed. Changing status to INACTIVE.", post.getId());
                changeStatusOfPost(post);
            }
        });
    }

    /**
     * Checks if the post's status is ACTIVE.
     *
     * @param post the post to be checked
     * @return true if the post's status is ACTIVE, false otherwise
     */
    private boolean isPostActive(PostDTO post) {
        boolean isActive = Constants.Status.ACTIVE.equals(post.getStatus());
        LOGGER.info("Post ID: {} is active: {}", post.getId(), isActive);
        return isActive;
    }

    /**
     * Changes the status of the post to INACTIVE if the trip is completed.
     *
     * @param post the post whose status needs to be updated
     */
    private void changeStatusOfPost(PostDTO post) {
        postProxy.updateStatusToInactive(post.getId());
        LOGGER.info("Status updated to INACTIVE for post ID: {}", post.getId());
    }

    /**
     * Checks if the trip is completed by comparing the end date with the current date.
     *
     * @param post the post to be checked
     * @return true if the trip is completed, false otherwise
     */
    private boolean isTripCompleted(PostDTO post) {
        LocalDate endDate = LocalDate.parse(post.getEndDate(), DATE_FORMATTER);
        LocalDate currentDate = LocalDate.now();
        boolean completed = endDate.isBefore(currentDate);
        LOGGER.info("Post ID: {} is completed: {}", post.getId(), completed);
        return completed;
    }

    /**
     * Checks if the trip is within the next 24 hours from the current time.
     *
     * @param post the post to be checked
     * @return true if the trip starts within the next 24 hours, false otherwise
     */
    private boolean dateBeforeFor24hrs(PostDTO post) {
        LocalDate startDate = LocalDate.parse(post.getStartDate(), DATE_FORMATTER);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime currentDateTime = LocalDateTime.now();
        Duration duration = Duration.between(currentDateTime, startDateTime);

        boolean within24Hours = !duration.isNegative() && duration.toHours() <= 24;
        if (within24Hours) {
            postProxy.updateStatusToLocked(post.getId());
        }
        LOGGER.info("Post ID: {} is within 24 hours: {}", post.getId(), within24Hours);
        return within24Hours;
    }

    /**
     * Sends a trip reminder email to all users associated with the post.
     *
     * @param postDTO the post data transfer object containing details for the reminder
     */
    public void sendTripReminder(PostDTO postDTO) {
        List<String> users = postDTO.getUsers();

        users.parallelStream().forEach(user -> {
            String email = proxy.getEmailFromUsername(user);
            LOGGER.info("Sending email to: {} for post ID: {}", email, postDTO.getId());
            emailService.sendMail(email, user, postDTO);
        });
    }
}
