package com.example.TTCS.controller;

import com.example.TTCS.model.Phong;
import com.example.TTCS.repository.PhongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class PhongController {

    @Autowired
    private PhongRepository phongRepository;

    @GetMapping
    public ResponseEntity<List<Phong>> getRoomsByBuilding(@RequestParam(required = false) String toa) {
        if (toa != null && !toa.isEmpty()) {
            List<Phong> dsPhong = phongRepository.findByIdToa(toa);
            return ResponseEntity.ok(dsPhong);
        } else {
            List<Phong> dsTatCaPhong = phongRepository.findAll();
            return ResponseEntity.ok(dsTatCaPhong);
        }
    }
}