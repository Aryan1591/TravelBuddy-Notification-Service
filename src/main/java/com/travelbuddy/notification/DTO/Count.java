package com.travelbuddy.notification.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Count {
    private Integer maleCount;
    private Integer femaleCount;
    private Integer otherCount;
    private Integer totalCount() {
        return maleCount + femaleCount + otherCount;
    }
}