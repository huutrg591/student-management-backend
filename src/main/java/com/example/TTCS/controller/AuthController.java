package com.example.TTCS.controller;

import com.example.TTCS.model.Account;
import com.example.TTCS.model.Student;
import com.example.TTCS.repository.AccountRepository;
import com.example.TTCS.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Account loginRequest) {
        Account account = accountRepository.findById(loginRequest.getUsername()).orElse(null);

        if (account != null && account.getPassword().equals(loginRequest.getPassword())) {
            String normalizedRole = account.getRole() == null
                    ? ""
                    : account.getRole().toLowerCase(Locale.ROOT);
            String portalMsv = resolvePortalMsv(account.getUsername(), normalizedRole);

            return ResponseEntity.ok(Map.of(
                    "username", account.getUsername(),
                    "role", normalizedRole,
                    "portalMsv", portalMsv
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Sai tài khoản hoặc mật khẩu!");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            String msv = payload.getOrDefault("msv", "").trim();
            String password = payload.getOrDefault("password", "");
            String confirmPassword = payload.getOrDefault("confirmPassword", "");

            if (msv.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "MSV không được để trống."));
            }
            if (password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu không được để trống."));
            }
            if (!password.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu xác nhận không khớp."));
            }
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu phải có ít nhất 6 ký tự."));
            }
            if (accountRepository.existsById(msv)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tài khoản đã tồn tại, vui lòng đăng nhập."));
            }

            ensureStudentProfile(msv);

            Account account = new Account();
            account.setUsername(msv);
            account.setPassword(password);
            account.setRole("student");
            accountRepository.save(account);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Đăng ký tài khoản thành công.",
                    "username", account.getUsername(),
                    "role", "student"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Không thể tạo tài khoản lúc này."));
        }
    }

    private String resolvePortalMsv(String username, String role) {
        if (!"student".equals(role)) {
            return username;
        }

        return studentRepository.findById(username)
                .map(student -> student.getMsv())
                .or(() -> studentRepository.findFirstByOrderByMsvAsc().map(student -> student.getMsv()))
                .orElse(username);
    }

    private void ensureStudentProfile(String msv) {
        if (studentRepository.existsById(msv)) {
            return;
        }

        Student student = new Student();
        student.setMsv(msv);
        student.setHoTen("Sinh viên " + msv);
        student.setNgaySinh("");
        student.setGioiTinh("Chưa cập nhật");
        student.setSdt("");
        student.setQueQuan("Tài khoản test");
        studentRepository.save(student);
    }
}
