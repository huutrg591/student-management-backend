package com.example.TTCS.controller;

import com.example.TTCS.model.RepairRequest;
import com.example.TTCS.service.RepairRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repairs")
@CrossOrigin(origins = "*")
public class RepairRequestController {

    @Autowired
    private RepairRequestService repairRequestService;

    @GetMapping
    public ResponseEntity<List<RepairRequest>> getByStudent(@RequestParam String msv) {
        return ResponseEntity.ok(repairRequestService.getByMsv(msv));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<RepairRequest>> getAll(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(repairRequestService.getAll(status));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> payload) {
        try {
            RepairRequest request = repairRequestService.create(
                    payload.getOrDefault("msv", ""),
                    payload.getOrDefault("roomId", ""),
                    payload.getOrDefault("category", ""),
                    payload.getOrDefault("description", "")
            );
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            RepairRequest request = repairRequestService.updateStatus(id, payload.get("status"));
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
