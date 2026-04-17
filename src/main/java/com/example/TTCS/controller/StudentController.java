package com.example.TTCS.controller;

import com.example.TTCS.model.Student;
import com.example.TTCS.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/{msv}")
    public Student getStudentById(@PathVariable String msv) {
        return studentRepository.findById(msv).orElse(null);
    }

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentRepository.save(student);
    }

    @PutMapping("/{msv}")
    public Student updateStudent(@PathVariable String msv, @RequestBody Student studentDetails) {
        Student student = studentRepository.findById(msv).orElse(null);
        if (student != null) {
            student.setHoTen(studentDetails.getHoTen());
            student.setNgaySinh(studentDetails.getNgaySinh());
            student.setGioiTinh(studentDetails.getGioiTinh());
            student.setSdt(studentDetails.getSdt());
            student.setQueQuan(studentDetails.getQueQuan());
            return studentRepository.save(student);
        }
        return null;
    }

    // Xóa sinh viên
    @DeleteMapping("/{msv}")
    public void deleteStudent(@PathVariable String msv) {
        studentRepository.deleteById(msv);
    }
}