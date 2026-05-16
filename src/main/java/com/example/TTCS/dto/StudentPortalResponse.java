package com.example.TTCS.dto;

import com.example.TTCS.model.HoaDon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentPortalResponse {
    private StudentPortalInfoDTO student;
    private List<RoommateDTO> roommates;
    private List<HoaDon> invoices;
}
