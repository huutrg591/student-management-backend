package com.example.TTCS.repository;

import com.example.TTCS.model.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    List<HoaDon> findByIdPhong(String idPhong);
    List<HoaDon> findByIdPhongOrderByThangNamDesc(String idPhong);
    List<HoaDon> findAllByOrderByThangNamDescIdPhongAsc();
    Optional<HoaDon> findByIdPhongAndThangNam(String idPhong, String thangNam);
    boolean existsByIdPhongAndThangNam(String idPhong, String thangNam);
}
