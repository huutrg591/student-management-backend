package com.example.TTCS.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "phong")
@Data
public class Phong {

    @Id
    @Column(name = "id_phong")
    private String idPhong;

    @Column(name = "id_toa")
    private String idToa;

    @Column(name = "ten_phong")
    private String tenPhong;

    @Column(name = "loai_phong")
    private String loaiPhong;

    @Column(name = "suc_chua")
    private Integer sucChua;

    @Column(name = "dang_o")
    private Integer dangO;

    @Column(name = "gia_thue")
    private Double giaThue;

    @Column(name = "trang_thai")
    private String trangThai;
}