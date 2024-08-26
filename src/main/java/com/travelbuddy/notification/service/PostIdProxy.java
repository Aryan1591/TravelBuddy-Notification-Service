package com.travelbuddy.notification.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name="PostService",url="https://travelbuddy-posts-service-production.up.railway.app")
public interface PostIdProxy {

    @PutMapping("/post/updateStatusToInactive/{id}")
    public ResponseEntity<?> updateStatusToInactive(@PathVariable String id);

    @PutMapping("/post/updateStatusToLocked/{id}")
    public ResponseEntity<?> updateStatusToLocked(@PathVariable String id);
}
