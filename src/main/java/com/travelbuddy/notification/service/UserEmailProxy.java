package com.travelbuddy.notification.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="UserService",url = "https://travelbuddy-user-service-production.up.railway.app")
public interface UserEmailProxy {
    @GetMapping("/users/email/{username}")
    public String getEmailFromUsername(@PathVariable String username);
}
