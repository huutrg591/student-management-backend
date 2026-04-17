package com.example.TTCS.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sinh_vien")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @Column(name = "msv")
    private String msv;

    @Column(name = "ho_ten")
    private String hoTen;

    @Column(name = "ngay_sinh")
    private String ngaySinh;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @Column(name = "sdt")
    private String sdt;

    @Column(name = "que_quan")
    private String queQuan;
}