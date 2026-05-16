package com.example.TTCS.service;

import com.example.TTCS.dto.RoommateDTO;
import com.example.TTCS.dto.StudentPortalInfoDTO;
import com.example.TTCS.dto.StudentPortalResponse;
import com.example.TTCS.model.HoaDon;
import com.example.TTCS.repository.HoaDonRepository;
import com.example.TTCS.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class StudentPortalService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    public StudentPortalResponse getPortalData(String msv) {
        StudentPortalInfoDTO student = studentRepository.findStudentPortalInfo(msv)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên với mã " + msv));

        String roomId = student.getIdPhong();
        List<RoommateDTO> roommates = roomId == null || roomId.isBlank()
                ? Collections.emptyList()
                : studentRepository.findRoommatesByRoomId(roomId);

        List<HoaDon> invoices = roomId == null || roomId.isBlank()
                ? Collections.emptyList()
                : hoaDonRepository.findByIdPhongOrderByThangNamDesc(roomId);

        return new StudentPortalResponse(student, roommates, invoices);
    }
}
