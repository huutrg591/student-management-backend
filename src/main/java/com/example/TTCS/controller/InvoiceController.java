package com.example.TTCS.controller;

import com.example.TTCS.model.HoaDon;
import com.example.TTCS.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<List<HoaDon>> getAll() {
        return ResponseEntity.ok(invoiceService.getAll());
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<HoaDon>> getByRoom(
            @PathVariable String roomId) {

        return ResponseEntity.ok(
                invoiceService.getByRoomId(roomId)
        );
    }

    @PostMapping
    public ResponseEntity<?> createInvoice(
            @RequestBody Map<String, Object> payload) {

        try {

            return ResponseEntity.ok(
                    invoiceService.createInvoice(payload)
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    @PutMapping("/{invoiceId}/pay")
    public ResponseEntity<?> markAsPaid(
            @PathVariable Integer invoiceId) {

        try {

            return ResponseEntity.ok(
                    invoiceService.markAsPaid(invoiceId)
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    @PutMapping("/{invoiceId}")
    public ResponseEntity<?> updateInvoice(
            @PathVariable Integer invoiceId,
            @RequestBody Map<String, Object> payload) {

        try {

            return ResponseEntity.ok(
                    invoiceService.updateInvoice(
                            invoiceId,
                            payload
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<?> deleteInvoice(
            @PathVariable Integer invoiceId) {

        try {

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            invoiceService.deleteInvoice(invoiceId)
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }
}