package com.example.TTCS.service;

import com.example.TTCS.model.RepairRequest;
import com.example.TTCS.model.Student;
import com.example.TTCS.repository.StudentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RepairRequestService {

    private static final Path STORAGE_PATH = Path.of("data", "repair-requests.json");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final List<String> ALLOWED_STATUSES = List.of(
            "Chờ tiếp nhận",
            "Đã tiếp nhận",
            "Đang xử lý",
            "Đã hoàn thành"
    );

    private static final Map<String, String> STATUS_ALIASES = createStatusAliases();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    private final AtomicLong sequence = new AtomicLong(1);
    private final List<RepairRequest> requests = new ArrayList<>();

    @PostConstruct
    public synchronized void init() {
        try {
            Files.createDirectories(STORAGE_PATH.getParent());

            if (Files.exists(STORAGE_PATH)) {
                List<RepairRequest> stored = objectMapper.readValue(
                        STORAGE_PATH.toFile(),
                        new TypeReference<List<RepairRequest>>() {}
                );

                requests.clear();
                requests.addAll(stored);

                long maxId = requests.stream()
                        .map(RepairRequest::getId)
                        .filter(Objects::nonNull)
                        .mapToLong(Long::longValue)
                        .max()
                        .orElse(0L);

                sequence.set(maxId + 1);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo dữ liệu yêu cầu sửa chữa", e);
        }
    }

    public synchronized List<RepairRequest> getByMsv(String msv) {
        return requests.stream()
                .filter(r -> r.getMsv() != null && r.getMsv().equalsIgnoreCase(msv))
                .sorted(Comparator.comparing(RepairRequest::getId).reversed())
                .toList();
    }

    public synchronized List<RepairRequest> getAll(String status) {
        return requests.stream()
                .filter(r -> matchesStatus(r, status))
                .sorted(Comparator.comparing(RepairRequest::getId).reversed())
                .toList();
    }

    public synchronized RepairRequest create(
            String msv,
            String roomId,
            String category,
            String description
    ) {

        if (msv == null || msv.isBlank()) {
            throw new IllegalArgumentException("MSV không được để trống");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Mô tả không được để trống");
        }

        Student student = studentRepository.findById(msv)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy sinh viên"));

        if (student.getIdPhong() == null || student.getIdPhong().isBlank()) {
            throw new IllegalArgumentException(
                    "Sinh viên chưa được xếp phòng, không thể gửi yêu cầu sửa chữa"
            );
        }

        String finalRoomId = student.getIdPhong();

        RepairRequest request = new RepairRequest(
                sequence.getAndIncrement(),
                msv.trim(),
                finalRoomId,
                category == null || category.isBlank() ? "Khác" : category.trim(),
                description.trim(),
                "Chờ tiếp nhận",
                LocalDateTime.now().format(DATE_FORMATTER)
        );

        requests.add(request);
        persist();
        return request;
    }

    public synchronized RepairRequest updateStatus(Long id, String status) {
        String normalizedStatus = normalizeStatus(status);

        RepairRequest request = requests.stream()
                .filter(r -> r.getId() != null && r.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy yêu cầu sửa chữa"));

        request.setStatus(normalizedStatus);
        persist();
        return request;
    }

    private boolean matchesStatus(RepairRequest item, String status) {
        if (status == null || status.isBlank()) return true;

        return normalizeStatus(status)
                .equals(normalizeStatus(item.getStatus()));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }

        String normalized = status.trim().toLowerCase(Locale.ROOT);

        String alias = STATUS_ALIASES.get(normalized);
        if (alias != null) return alias;

        return ALLOWED_STATUSES.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Trạng thái không hợp lệ"));
    }

    private void persist() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(STORAGE_PATH.toFile(), requests);
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu yêu cầu sửa chữa", e);
        }
    }

    private static Map<String, String> createStatusAliases() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("cho tiep nhan", "Chờ tiếp nhận");
        aliases.put("da tiep nhan", "Đã tiếp nhận");
        aliases.put("dang xu ly", "Đang xử lý");
        aliases.put("da hoan thanh", "Đã hoàn thành");
        return aliases;
    }
}