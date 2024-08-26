package com.travelbuddy.notification.DTO;


import com.travelbuddy.notification.constants.Constants;
import lombok.Data;
import org.springframework.boot.context.metrics.buffering.StartupTimeline;

import java.time.LocalDate;
import java.util.List;

@Data
public class PostDTO {
    private String id; // serves as PostId,ChatId and TimelineId as mongoDB has auto generated Id we don't need @GenerateType
    private String title;
    private String source;
    private String destination; // also the Title of the post
    private String startDate;
    private String  endDate;
    private Count count;
    private List<TimelineEntry> events;
    private Double amount;
    private List<String> users;
    private Constants.Status status;
    private String adminName;
    private int days;
    private int nights;
}
