package com.example.TTCS.controller;

import com.example.TTCS.dto.StudentInfoDTO;
import com.example.TTCS.model.Student;
import com.example.TTCS.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    public ResponseEntity<List<StudentInfoDTO>> getAllStudents(
            @RequestParam(required = false) String toa,
            @RequestParam(required = false) String phong) {
        // Lưu ý: Đảm bảo tên phương thức trong Repository khớp 100%
        return ResponseEntity.ok(studentRepository.findStudentsWithRoom(toa, phong));
    }

    // Các hàm khác giữ nguyên...
    @GetMapping("/{msv}")
    public Student getStudentById(@PathVariable String msv) {
        return studentRepository.findById(msv).orElse(null);
    }
}