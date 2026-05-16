package com.example.TTCS.controller;

import com.example.TTCS.dto.StudentInfoDTO;
import com.example.TTCS.repository.StudentRepository;
import com.example.TTCS.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    public ResponseEntity<List<StudentInfoDTO>> getAllStudents(
            @RequestParam(required = false) String toa,
            @RequestParam(required = false) String phong) {
        return ResponseEntity.ok(studentRepository.findStudentsWithRoom(toa, phong));
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody Map<String, Object> payload) {
        try {
            studentService.saveStudent(payload);
            return ResponseEntity.ok("Lưu thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @PutMapping("/{msv}")
    public ResponseEntity<?> updateStudent(@PathVariable String msv, @RequestBody Map<String, Object> payload) {
        try {
            studentService.updateStudent(msv, payload);
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}
