package com.example.TTCS.repository;

import com.example.TTCS.model.Student;
import com.example.TTCS.dto.StudentInfoDTO; // Import DTO ở đây!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    @Query(value = "SELECT s.msv, s.ho_ten as hoTen, s.ngay_sinh as ngaySinh, s.gioi_tinh as gioiTinh, s.sdt, s.que_quan as queQuan, h.id_phong as idPhong, p.id_toa as idToa " +
            "FROM sinh_vien s " +
            "JOIN hop_dong h ON s.msv = h.msv " +
            "JOIN phong p ON h.id_phong = p.id_phong " +
            "WHERE (:toaId IS NULL OR p.id_toa = :toaId) " +
            "AND (:phongId IS NULL OR h.id_phong = :phongId)", nativeQuery = true)
    List<StudentInfoDTO> findStudentsWithRoom(@Param("toaId") String toaId, @Param("phongId") String phongId);
}