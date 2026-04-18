package com.example.TTCS.repository;

import com.example.TTCS.model.Phong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhongRepository extends JpaRepository<Phong, String> {
    List<Phong> findByIdToa(String idToa);
}