package com.example.TTCS.controller;

import com.example.TTCS.model.Account;
import com.example.TTCS.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Account loginRequest) {
        Account account = accountRepository.findById(loginRequest.getUsername()).orElse(null);

        if (account != null && account.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.ok(Map.of(
                    "username", account.getUsername(),
                    "role", account.getRole()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tài khoản hoặc mật khẩu!");
        }
    }
}