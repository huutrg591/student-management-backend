package com.example.TTCS.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "hoa_don")
@Data
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hoa_don")
    private Integer idHoaDon;

    @Column(name = "id_phong")
    private String idPhong;

    @Column(name = "thang_nam")
    private String thangNam;

    @Column(name = "chi_so_dien_cu")
    private Integer chiSoDienCu;

    @Column(name = "chi_so_dien_moi")
    private Integer chiSoDienMoi;

    @Column(name = "chi_so_nuoc_cu")
    private Integer chiSoNuocCu;

    @Column(name = "chi_so_nuoc_moi")
    private Integer chiSoNuocMoi;

    @Column(name = "tong_tien")
    private Double tongTien;

    @Column(name = "trang_thai")
    private String trangThai;
}