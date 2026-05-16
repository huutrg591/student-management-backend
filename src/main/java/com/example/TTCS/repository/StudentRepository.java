package com.example.TTCS.repository;

import com.example.TTCS.dto.RoommateDTO;
import com.example.TTCS.dto.StudentInfoDTO;
import com.example.TTCS.dto.StudentPortalInfoDTO;
import com.example.TTCS.model.Student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    @Query(value = """
        SELECT
            s.msv,
            s.ho_ten as hoTen,
            s.ngay_sinh as ngaySinh,
            s.gioi_tinh as gioiTinh,
            s.sdt,
            s.que_quan as queQuan,
            s.id_phong as idPhong,
            p.id_toa as idToa

        FROM sinh_vien s

        LEFT JOIN phong p
            ON s.id_phong = p.id_phong

        WHERE (:toaId IS NULL OR p.id_toa = :toaId)
        AND (:phongId IS NULL OR s.id_phong = :phongId)

        ORDER BY s.msv
    """, nativeQuery = true)
    List<StudentInfoDTO> findStudentsWithRoom(
            @Param("toaId") String toaId,
            @Param("phongId") String phongId
    );

    @Query(value = """
        SELECT
            s.msv,
            s.ho_ten as hoTen,
            s.ngay_sinh as ngaySinh,
            s.gioi_tinh as gioiTinh,
            s.sdt,
            s.que_quan as queQuan,

            s.id_phong as idPhong,

            p.id_toa as idToa,
            p.ten_phong as tenPhong,
            p.loai_phong as loaiPhong,
            p.suc_chua as sucChua,
            p.dang_o as dangO,
            p.gia_thue as giaThue

        FROM sinh_vien s

        LEFT JOIN phong p
            ON s.id_phong = p.id_phong

        WHERE s.msv = :msv

        LIMIT 1
    """, nativeQuery = true)
    Optional<StudentPortalInfoDTO> findStudentPortalInfo(
            @Param("msv") String msv
    );

    @Query(value = """
        SELECT
            s.msv,
            s.ho_ten as hoTen,
            s.sdt

        FROM sinh_vien s

        WHERE s.id_phong = :roomId

        ORDER BY s.ho_ten
    """, nativeQuery = true)
    List<RoommateDTO> findRoommatesByRoomId(
            @Param("roomId") String roomId
    );

    Optional<Student> findFirstByOrderByMsvAsc();
}