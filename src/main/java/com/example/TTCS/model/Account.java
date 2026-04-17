package com.example.TTCS.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tai_khoan")
@Data
public class Account {
    @Id
    private String username;
    private String password;
    private String role;
}