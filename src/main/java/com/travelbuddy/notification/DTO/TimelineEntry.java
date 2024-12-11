package com.travelbuddy.notification.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelineEntry {
    private String title;
    private LocalDate date;
    private List<String> events; // instant in LocalDate if required
}