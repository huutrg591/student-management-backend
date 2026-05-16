package com.example.TTCS.controller;

import com.example.TTCS.dto.StudentPortalResponse;
import com.example.TTCS.service.StudentPortalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/student-portal")
@CrossOrigin(origins = "*")
public class StudentPortalController {

    @Autowired
    private StudentPortalService studentPortalService;

    @GetMapping("/{msv}")
    public ResponseEntity<?> getStudentPortal(@PathVariable String msv) {
        try {
            StudentPortalResponse response = studentPortalService.getPortalData(msv);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
