package com.travelbuddy.notification.controller;

import com.travelbuddy.notification.service.NotificationScheduleTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    @Autowired
    private NotificationScheduleTask notificationScheduleTask;

    @GetMapping("/fetchPostsAndUpdate")
    public void test()
    {
        notificationScheduleTask.fetchPostData();
    }
}
