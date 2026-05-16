package com.example.TTCS.service;

import com.example.TTCS.model.Phong;
import com.example.TTCS.model.Student;
import com.example.TTCS.repository.PhongRepository;
import com.example.TTCS.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PhongRepository phongRepository;

    @Transactional
    public void saveStudent(Map<String, Object> payload) {

    java.util.function.Function<String, String> getSafe = (key) ->
            payload.get(key) != null ? payload.get(key).toString() : "";

    Student student = new Student();

    student.setMsv(getSafe.apply("msv"));
    student.setHoTen(getSafe.apply("hoTen"));
    student.setNgaySinh(getSafe.apply("ngaySinh"));
    validateNgaySinh(student.getNgaySinh());
    student.setGioiTinh(getSafe.apply("gioiTinh"));
    student.setSdt(getSafe.apply("sdt"));
    student.setQueQuan(getSafe.apply("queQuan"));

    String roomId = getSafe.apply("idPhong");

    if (student.getMsv().isEmpty()) {
        throw new RuntimeException("Mã sinh viên không được để trống.");
    }

    if (roomId.isEmpty()) {
        throw new RuntimeException("Mã phòng không được để trống.");
    }

    Phong room = phongRepository.findById(roomId)
        .orElseThrow(() ->
                new RuntimeException("Không tìm thấy phòng.")
        );

if (!student.getGioiTinh()
        .equalsIgnoreCase(room.getGioiTinh())) {

    throw new RuntimeException(
            "Không thể xếp sinh viên vào phòng khác giới tính."
    );
}
if (!student.getSdt().matches("^0\\d{9}$")) {

    throw new RuntimeException(
            "Số điện thoại không hợp lệ."
    );
}

if (studentRepository.existsById(student.getMsv())) {

    throw new RuntimeException(
            "Mã sinh viên đã tồn tại."
    );
}

student.setIdPhong(roomId);

studentRepository.save(student);

updateRoomOccupancy(roomId, 1);
    }
    @Transactional
    public void updateStudent(String msv, Map<String, Object> payload) {

        Student student = studentRepository.findById(msv)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên " + msv));

        java.util.function.Function<String, String> getSafe = (key) ->
                payload.get(key) != null ? payload.get(key).toString() : "";

        student.setHoTen(getSafe.apply("hoTen"));
        student.setNgaySinh(getSafe.apply("ngaySinh"));
        validateNgaySinh(student.getNgaySinh());
        student.setGioiTinh(getSafe.apply("gioiTinh"));
        student.setSdt(getSafe.apply("sdt"));
        if (!student.getSdt().matches("^0\\d{9}$")) {

    throw new RuntimeException(
            "Số điện thoại không hợp lệ."
    );
}
        student.setQueQuan(getSafe.apply("queQuan"));

        String newRoomId = getSafe.apply("idPhong");

        if (!newRoomId.isEmpty()) {

            Phong newRoom = phongRepository.findById(newRoomId)
        .orElseThrow(() ->
                new RuntimeException(
                        "Không tìm thấy phòng " + newRoomId
                )
        );

if (!student.getGioiTinh()
        .equalsIgnoreCase(newRoom.getGioiTinh())) {

    throw new RuntimeException(
            "Không thể chuyển sinh viên sang phòng khác giới tính."
    );
}


            String oldRoomId = student.getIdPhong();

            if (!newRoomId.equals(oldRoomId)) {

            if (oldRoomId != null && !oldRoomId.isBlank()) {
                updateRoomOccupancy(oldRoomId, -1);
            }

            student.setIdPhong(newRoomId);

            studentRepository.save(student);

            updateRoomOccupancy(newRoomId, 1);

            return;
        }
        }

        studentRepository.save(student);
    }

        private void updateRoomOccupancy(String roomId, int delta) {

        phongRepository.findById(roomId).ifPresent(room -> {

            int current = room.getDangO() != null ? room.getDangO() : 0;

            int next = current + delta;

            if (next < 0) {
                next = 0;
            }

            if (next > room.getSucChua()) {
                throw new RuntimeException("Phòng đã đầy.");
            }

            room.setDangO(next);

            room.setTrangThai(
                    next >= room.getSucChua()
                            ? "Đã đầy"
                            : "Còn chỗ"
            );

            phongRepository.save(room);
        });
    }
    private void validateNgaySinh(String ngaySinhStr) {

    try {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate ngaySinh =
                LocalDate.parse(ngaySinhStr, formatter);

        if (ngaySinh.isAfter(LocalDate.now())) {

            throw new RuntimeException(
                    "Ngày sinh không được lớn hơn ngày hiện tại."
            );
        }

    } catch (Exception e) {

        throw new RuntimeException(
                "Ngày sinh không hợp lệ."
        );
    }
}
}
