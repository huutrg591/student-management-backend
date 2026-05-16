package com.example.TTCS.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequest {
    private Long id;
    private String msv;
    private String roomId;
    private String category;
    private String description;
    private String status;
    private String createdAt;
}
