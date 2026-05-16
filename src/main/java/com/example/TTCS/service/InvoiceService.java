package com.example.TTCS.service;

import com.example.TTCS.model.HoaDon;
import com.example.TTCS.repository.HoaDonRepository;
import com.example.TTCS.repository.PhongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceService {

    private static final int ELECTRIC_PRICE = 3500;
    private static final int WATER_PRICE = 12000;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private PhongRepository phongRepository;


    public List<HoaDon> getAll() {
        return hoaDonRepository.findAllByOrderByThangNamDescIdPhongAsc();
    }

    public List<HoaDon> getByRoomId(String roomId) {
        return hoaDonRepository.findByIdPhongOrderByThangNamDesc(roomId);
    }

    @Transactional
    public HoaDon createInvoice(Map<String, Object> payload) {
        String roomId = getString(payload, "idPhong");
        String thangNam = getString(payload, "thangNam");
        if (thangNam.isBlank()) {
            thangNam = previousMonth();
        }

        if (roomId.isBlank()) {
            throw new RuntimeException("Mã phòng không được để trống.");
        }
        if (!phongRepository.existsById(roomId)) {
            throw new RuntimeException("Không tìm thấy phòng " + roomId);
        }
        if (hoaDonRepository.existsByIdPhongAndThangNam(roomId, thangNam)) {
            throw new RuntimeException("Phòng " + roomId + " đã có hóa đơn tháng " + thangNam + ".");
        }

        int oldElectric = getInt(payload, "chiSoDienCu");
        int newElectric = getInt(payload, "chiSoDienMoi");
        int oldWater = getInt(payload, "chiSoNuocCu");
        int newWater = getInt(payload, "chiSoNuocMoi");
        validateMeterIndexes(oldElectric, newElectric, oldWater, newWater);

        return hoaDonRepository.save(buildInvoice(roomId, thangNam, oldElectric, newElectric, oldWater, newWater));
    }

    @Transactional
public HoaDon markAsPaid(Integer invoiceId) {

    HoaDon invoice = hoaDonRepository.findById(invoiceId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Không tìm thấy hóa đơn " + invoiceId
                    )
            );

    if ("Đã đóng"
            .equalsIgnoreCase(invoice.getTrangThai())) {

        throw new RuntimeException(
                "Hóa đơn đã được thanh toán trước đó."
        );
    }

    invoice.setTrangThai("Đã đóng");

    return hoaDonRepository.save(invoice);
}

@Transactional
public HoaDon updateInvoice(
        Integer invoiceId,
        Map<String, Object> payload) {

    HoaDon invoice = hoaDonRepository.findById(invoiceId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Không tìm thấy hóa đơn " + invoiceId
                    )
            );

    if ("Đã đóng"
            .equalsIgnoreCase(invoice.getTrangThai())) {

        throw new RuntimeException(
                "Không thể chỉnh sửa hóa đơn đã thanh toán."
        );
    }

    int oldElectric = getInt(payload, "chiSoDienCu");
    int newElectric = getInt(payload, "chiSoDienMoi");

    int oldWater = getInt(payload, "chiSoNuocCu");
    int newWater = getInt(payload, "chiSoNuocMoi");

    validateMeterIndexes(
            oldElectric,
            newElectric,
            oldWater,
            newWater
    );

    invoice.setChiSoDienCu(oldElectric);
    invoice.setChiSoDienMoi(newElectric);

    invoice.setChiSoNuocCu(oldWater);
    invoice.setChiSoNuocMoi(newWater);

    invoice.setTongTien(
            calculateTotal(
                    oldElectric,
                    newElectric,
                    oldWater,
                    newWater
            )
    );

    return hoaDonRepository.save(invoice);
}
@Transactional
public String deleteInvoice(Integer invoiceId) {

    HoaDon invoice = hoaDonRepository.findById(invoiceId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "Không tìm thấy hóa đơn " + invoiceId
                    )
            );

    if ("Đã đóng"
            .equalsIgnoreCase(invoice.getTrangThai())) {

        throw new RuntimeException(
                "Không thể xóa hóa đơn đã thanh toán."
        );
    }

    hoaDonRepository.delete(invoice);

    return "Xóa hóa đơn thành công!";
}

    private HoaDon buildInvoice(String roomId, String thangNam, int oldElectric, int newElectric, int oldWater, int newWater) {
        HoaDon invoice = new HoaDon();
        invoice.setIdPhong(roomId);
        invoice.setThangNam(thangNam);
        invoice.setChiSoDienCu(oldElectric);
        invoice.setChiSoDienMoi(newElectric);
        invoice.setChiSoNuocCu(oldWater);
        invoice.setChiSoNuocMoi(newWater);
        invoice.setTongTien(calculateTotal(oldElectric, newElectric, oldWater, newWater));
        invoice.setTrangThai("Chưa thanh toán");
        return invoice;
    }

    private double calculateTotal(int oldElectric, int newElectric, int oldWater, int newWater) {
        return ((newElectric - oldElectric) * ELECTRIC_PRICE) + ((newWater - oldWater) * WATER_PRICE);
    }

    private void validateMeterIndexes(int oldElectric, int newElectric, int oldWater, int newWater) {
        if (newElectric < oldElectric) {
            throw new RuntimeException("Chỉ số điện mới phải lớn hơn hoặc bằng chỉ số điện cũ.");
        }
        if (newWater < oldWater) {
            throw new RuntimeException("Chỉ số nước mới phải lớn hơn hoặc bằng chỉ số nước cũ.");
        }
    }

    private String previousMonth() {
        return LocalDate.now().minusMonths(1).format(MONTH_FORMATTER);
    }

    private String getString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : value.toString().trim();
    }

    private int getInt(Map<String, Object> payload, String key) {
        String value = getString(payload, key);
        if (value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }
}
