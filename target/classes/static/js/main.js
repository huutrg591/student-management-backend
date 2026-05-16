const API_BASE = "/api";
const REPAIR_STATUS_SEQUENCE = [
    "Chờ tiếp nhận",
    "Đã tiếp nhận",
    "Đang xử lý",
    "Đã hoàn thành"
];

let allStudents = [];
let allRepairs = [];
let isEditMode = false;
let currentStudentPortalData = null;

document.addEventListener("DOMContentLoaded", () => {
    initAdminPage();
    initStudentPage();
});

function initAdminPage() {
    const studentTableBody = document.getElementById("student-table-body");
    if (!studentTableBody) {
        return;
    }

    if (!enforceAuth("admin")) {
        return;
    }

    bindProtectedPageGuard("admin");

    fetchStudents();
    loadAdminRepairs();

    const mainForm = document.getElementById("main-form");
    if (mainForm) {
        mainForm.addEventListener("submit", submitStudentForm);
    }

    const invoiceForm = document.getElementById("invoice-form");
    if (invoiceForm) {
        invoiceForm.addEventListener("submit", submitInvoiceForm);
        setDefaultInvoiceMonth();
    }

    const searchInput = document.getElementById("search-input");
    if (searchInput) {
        searchInput.addEventListener("input", searchStudent);
    }
}

function initStudentPage() {
    const portalRoot = document.getElementById("student-portal-root");
    if (!portalRoot) {
        return;
    }

    const session = enforceAuth("student");
    if (!session) {
        return;
    }

    bindProtectedPageGuard("student");
    bindStudentInteractions();
    loadStudentPortal(session.portalMsv || session.username);
}

function getCurrentSession() {
    try {
        return JSON.parse(localStorage.getItem("userSession"));
    } catch (error) {
        return null;
    }
}

function fetchStudents() {
    const filterElement = document.getElementById("filter-toa");
    const toaSelected = filterElement ? filterElement.value : "";
    const url = toaSelected ? `${API_BASE}/students?toa=${toaSelected}` : `${API_BASE}/students`;

    fetch(url)
        .then((response) => response.json())
        .then((data) => {
            allStudents = data;
            setText("total-students", String(data.length));
            renderStudentTable(filterStudentsByKeyword(data));
        })
        .catch((error) => {
            console.error(error);
            document.getElementById("student-table-body").innerHTML =
                '<tr><td colspan="7" style="text-align:center; color:#ef4444;">Chưa kết nối được backend.</td></tr>';
        });
}

function renderStudentTable(data) {
    const tbody = document.getElementById("student-table-body");
    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";

    if (!data.length) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Không có sinh viên nào trong danh sách.</td></tr>';
        return;
    }

    data.forEach((student) => {
        const msv = student.msv || "";
        const ten = student.hoTen || "";
        const toa = student.idToa || "Chưa xếp";
        const phong = student.idPhong || "Chưa xếp";
        const gt = student.gioiTinh || "";
        const sdt = student.sdt || "";

        const toaBadge = toa.includes("B1") || toa.includes("B2")
            ? `<span style="color: #3b82f6; font-weight: 700;">${toa}</span>`
            : `<span style="color: #ec4899; font-weight: 700;">${toa}</span>`;

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td><strong>${msv}</strong></td>
            <td>${ten}</td>
            <td>${toaBadge}</td>
            <td><span style="background: #f1f5f9; padding: 4px 8px; border-radius: 4px; font-weight: 700; border: 1px solid #cbd5e1;">${phong}</span></td>
            <td>${gt}</td>
            <td>${sdt}</td>
            <td>
                <button class="action-btn edit-btn" onclick="openEditForm('${msv}')">
                    <i class="fa-solid fa-pen"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function searchStudent() {
    renderStudentTable(filterStudentsByKeyword(allStudents));
}

function showStudentList() {
    clearStudentListFilters();
    showSection("list-section");
    fetchStudents();
}

function clearStudentListFilters() {
    setInputValue("search-input", "");
    setInputValue("filter-toa", "");
}

function filterStudentsByKeyword(students) {
    const input = document.getElementById("search-input");
    const keyword = input ? input.value.trim().toLowerCase() : "";
    if (!keyword) {
        return students;
    }

    return students.filter((student) =>
        (student.hoTen || "").toLowerCase().includes(keyword) ||
        (student.msv || "").toLowerCase().includes(keyword) ||
        (student.idToa || "").toLowerCase().includes(keyword) ||
        (student.idPhong || "").toLowerCase().includes(keyword)
    );
}

function showSection(sectionId) {
    document.querySelectorAll(".content-section").forEach((sec) => {
        sec.style.display = "none";
    });

    const target = document.getElementById(sectionId);
    if (target) {
        target.style.display = "block";
    }

    const navItems = document.querySelectorAll(".sidebar-nav li");
    navItems.forEach((li) => li.classList.remove("active"));

    const navIndexMap = {
        "building-section": 0,
        "room-section": 0,
        "list-section": 1,
        "form-section": 1,
        "repair-admin-section": 2,
        "invoice-admin-section": 3
    };
    const activeIndex = navIndexMap[sectionId];
    if (typeof activeIndex === "number" && navItems[activeIndex]) {
        navItems[activeIndex].classList.add("active");
    }

    if (sectionId === "repair-admin-section") {
        loadAdminRepairs();
    }
    if (sectionId === "invoice-admin-section") {
        loadAdminInvoices();
        setDefaultInvoiceMonth();
    }
}

function showAddForm() {
    isEditMode = false;
    setText("form-title", "Thêm sinh viên mới");
    document.getElementById("main-form").reset();
    document.getElementById("msv").readOnly = false;
    showSection("form-section");
}

function openEditForm(msv) {
    isEditMode = true;
    setText("form-title", "Cập nhật thông tin");
    showSection("form-section");

    const student = allStudents.find((item) => item.msv === msv);
    if (!student) {
        return;
    }

    document.getElementById("msv").value = student.msv || "";
    document.getElementById("msv").readOnly = true;
    document.getElementById("hoTen").value = student.hoTen || "";
    document.getElementById("ngaySinh").value = student.ngaySinh || "";
    document.getElementById("gioiTinh").value = student.gioiTinh || "Nam";
    document.getElementById("sdt").value = student.sdt || "";
    document.getElementById("queQuan").value = student.queQuan || "";
    const roomInput = document.getElementById("idPhong");
    if (roomInput) {
        roomInput.value = student.idPhong || "";
    }
}

function goBack() {
    const form = document.getElementById("main-form");
    if (form) {
        form.reset();
    }
    showStudentList();
}

function submitStudentForm(event) {
    event.preventDefault();

    const payload = {
        msv: document.getElementById("msv").value.trim(),
        hoTen: document.getElementById("hoTen").value.trim(),
        ngaySinh: document.getElementById("ngaySinh").value,
        gioiTinh: document.getElementById("gioiTinh").value,
        sdt: document.getElementById("sdt").value.trim(),
        queQuan: document.getElementById("queQuan").value.trim(),
        idPhong: document.getElementById("idPhong").value.trim()
    };

    const method = isEditMode ? "PUT" : "POST";
    const url = isEditMode ? `${API_BASE}/students/${encodeURIComponent(payload.msv)}` : `${API_BASE}/students`;

    fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(async (response) => {
            if (response.ok) {
                isEditMode = false;
                clearStudentListFilters();
                showSection("list-section");
                fetchStudents();
                return;
            }
            const message = await response.text();
            alert(message || "Lưu thất bại.");
        })
        .catch((error) => {
            console.error(error);
            alert("Không thể kết nối tới backend.");
        });
}

function logout() {
    const confirmed = window.confirm("Bạn có chắc chắn muốn đăng xuất?");
    if (!confirmed) {
        return;
    }

    localStorage.removeItem("userSession");
    sessionStorage.setItem("authMessage", "Bạn cần đăng nhập để tiếp tục.");
    window.location.replace("login.html");
}

function manageRooms(buildingId) {
    showSection("room-section");
    setText("current-building-name", buildingId);
    setText("stat-building-name", buildingId);

    const tbody = document.getElementById("room-table-body");
    if (!tbody) {
        return;
    }

    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Đang tải...</td></tr>';

    fetch(`${API_BASE}/rooms?toa=${buildingId}`)
        .then((response) => response.json())
        .then((rooms) => {
            tbody.innerHTML = "";
            let totalStudentsInBuilding = 0;

            if (!rooms.length) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Chưa có dữ liệu phòng.</td></tr>';
            }

            rooms.forEach((room) => {
                const id = room.idPhong || "";
                const loai = room.loaiPhong || "";
                const sucChua = room.sucChua || 0;
                const dangO = room.dangO || 0;
                const trangThai = room.trangThai || "Còn chỗ";

                totalStudentsInBuilding += Number(dangO);

                const statusClass = trangThai.toLowerCase().includes("đầy") || trangThai.toLowerCase().includes("day") ? "status-danger" : "status-success";
                tbody.innerHTML += `
                    <tr>
                        <td><strong>${id}</strong></td>
                        <td>${loai}</td>
                        <td>${sucChua}</td>
                        <td>${dangO}</td>
                        <td><span class="status-pill ${statusClass}">${trangThai}</span></td>
                        <td>
                            <div style="display:flex; gap:8px; flex-wrap:wrap;">
                                <button class="btn btn-secondary btn-small" onclick="filterByRoom('${id}')">Xem sinh viên</button>
                                <button class="btn btn-primary btn-small" onclick="openInvoiceFormForRoom('${id}')">Lập hóa đơn</button>
                            </div>
                        </td>
                    </tr>
                `;
            });

            setText("total-students-building", String(totalStudentsInBuilding));
        })
        .catch((error) => {
            console.error(error);
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#ef4444;">Không tải được danh sách phòng.</td></tr>';
        });
}

function filterByRoom(roomId) {
    const filterToa = document.getElementById("filter-toa");
    if (filterToa) {
        filterToa.value = "";
    }

    showSection("list-section");
    fetch(`${API_BASE}/students?phong=${roomId}`)
        .then((response) => response.json())
        .then((data) => {
            allStudents = data;
            setText("total-students", String(data.length));
            renderStudentTable(filterStudentsByKeyword(data));
        })
        .catch((error) => console.error(error));
}

function loadAdminRepairs() {
    const tbody = document.getElementById("repair-admin-table-body");
    if (!tbody) {
        return;
    }

    const statusFilter = document.getElementById("repair-status-filter");
    const status = statusFilter ? statusFilter.value : "";
    const url = status
        ? `${API_BASE}/repairs/admin?status=${encodeURIComponent(status)}`
        : `${API_BASE}/repairs/admin`;

    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Đang tải...</td></tr>';

    fetch(url)
        .then((response) => response.json())
        .then((items) => {
            allRepairs = Array.isArray(items) ? items : [];
            updateRepairSummary(allRepairs);
            renderAdminRepairTable(allRepairs);
        })
        .catch((error) => {
            console.error(error);
            tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#ef4444;">Không tải được danh sách yêu cầu sửa chữa.</td></tr>';
        });
}

function updateRepairSummary(items) {
    const counts = {
        pending: 0,
        progress: 0,
        completed: 0
    };

    items.forEach((item) => {
        const status = normalizeRepairStatus(item.status);
        if (status === "Chờ tiếp nhận") {
            counts.pending += 1;
        } else if (status === "Đang xử lý" || status === "Đã tiếp nhận") {
            counts.progress += 1;
        } else if (status === "Đã hoàn thành") {
            counts.completed += 1;
        }
    });

    setText("repair-pending-count", String(counts.pending));
    setText("repair-progress-count", String(counts.progress));
    setText("repair-completed-count", String(counts.completed));
}

function renderAdminRepairTable(items) {
    const tbody = document.getElementById("repair-admin-table-body");
    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";

    if (!items.length) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Chưa có yêu cầu sửa chữa nào.</td></tr>';
        return;
    }

    items.forEach((item) => {
        const status = normalizeRepairStatus(item.status);
        const statusClass = getRepairStatusClass(status);

        tbody.innerHTML += `
            <tr>
                <td><strong>#${item.id ?? ""}</strong></td>
                <td>${item.msv || ""}</td>
                <td>${item.roomId || "--"}</td>
                <td>${item.category || "Khác"}</td>
                <td style="max-width: 320px;">${item.description || ""}</td>
                <td>${item.createdAt || ""}</td>
                <td><span class="status-pill ${statusClass}">${formatRepairStatus(status)}</span></td>
                <td>${renderRepairActions(item.id, status)}</td>
            </tr>
        `;
    });
}

function renderRepairActions(id, status) {
    const actions = [];

    if (status === "Chờ tiếp nhận") {
        actions.push(createRepairActionButton(id, "Đã tiếp nhận", "Tiếp nhận", "btn-primary"));
    }
    if (status === "Đã tiếp nhận") {
        actions.push(createRepairActionButton(id, "Đang xử lý", "Xử lý", "btn-secondary"));
    }
    if (status === "Đang xử lý") {
        actions.push(createRepairActionButton(id, "Đã hoàn thành", "Hoàn thành", "btn-primary"));
    }
    if (status !== "Chờ tiếp nhận") {
        actions.push(createRepairActionButton(id, "Chờ tiếp nhận", "Reset", "btn-secondary"));
    }

    return `<div style="display:flex; gap:8px; flex-wrap:wrap;">${actions.join("")}</div>`;
}

function createRepairActionButton(id, nextStatus, label, className) {
    return `<button class="btn ${className} btn-small" type="button" onclick="updateRepairStatus(${id}, '${nextStatus}')">${label}</button>`;
}

function updateRepairStatus(id, status) {
    fetch(`${API_BASE}/repairs/${id}/status`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status })
    })
        .then(async (response) => {
            const body = await response.json();
            if (!response.ok) {
                throw new Error(body.message || "Không cập nhật được trạng thái.");
            }
            loadAdminRepairs();
        })
        .catch((error) => {
            console.error(error);
            alert(error.message || "Không thể cập nhật trạng thái.");
        });
}

function loadAdminInvoices() {
    const tbody = document.getElementById("invoice-admin-table-body");
    if (!tbody) {
        return;
    }

    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Đang tải...</td></tr>';

    fetch(`${API_BASE}/invoices`)
        .then((response) => response.json())
        .then((items) => {
            renderAdminInvoiceTable(Array.isArray(items) ? items : []);
        })
        .catch((error) => {
            console.error(error);
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:#ef4444;">Không tải được danh sách hóa đơn.</td></tr>';
        });
}

function renderAdminInvoiceTable(items) {
    const tbody = document.getElementById("invoice-admin-table-body");
    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";
    if (!items.length) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Chưa có hóa đơn nào.</td></tr>';
        return;
    }

    items.forEach((invoice) => {
        const soDien = (invoice.chiSoDienMoi || 0) - (invoice.chiSoDienCu || 0);
        const soNuoc = (invoice.chiSoNuocMoi || 0) - (invoice.chiSoNuocCu || 0);
        const trangThai = invoice.trangThai || "Chưa thanh toán";
        const daDong = trangThai.toLowerCase().includes("đã") || trangThai.toLowerCase().includes("da");

        tbody.innerHTML += `
            <tr>
                <td><strong>#${invoice.idHoaDon || ""}</strong></td>
                <td>${invoice.idPhong || ""}</td>
                <td>${invoice.thangNam || ""}</td>
                <td>${soDien} kWh</td>
                <td>${soNuoc} m³</td>
                <td style="font-weight:700;">${formatCurrency(invoice.tongTien || 0)}</td>
                <td><span class="status-pill ${daDong ? "status-success" : "status-danger"}">${trangThai}</span></td>
            </tr>
        `;
    });
}

function submitInvoiceForm(event) {
    event.preventDefault();

    const payload = {
        idPhong: getInputValue("invoice-room-id"),
        thangNam: formatMonthInput(getInputValue("invoice-month")),
        chiSoDienCu: getInputValue("invoice-electric-old"),
        chiSoDienMoi: getInputValue("invoice-electric-new"),
        chiSoNuocCu: getInputValue("invoice-water-old"),
        chiSoNuocMoi: getInputValue("invoice-water-new")
    };

    fetch(`${API_BASE}/invoices`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(async (response) => {
            const body = await response.json();
            if (!response.ok) {
                throw new Error(body.message || "Không lập được hóa đơn.");
            }
            resetInvoiceForm();
            loadAdminInvoices();
            alert("Đã lập hóa đơn thành công.");
        })
        .catch((error) => {
            console.error(error);
            alert(error.message || "Không thể lập hóa đơn.");
        });
}


function openInvoiceFormForRoom(roomId) {
    showSection("invoice-admin-section");
    setInputValue("invoice-room-id", roomId);
    setDefaultInvoiceMonth();
    document.getElementById("invoice-electric-old")?.focus();
}

function resetInvoiceForm() {
    const form = document.getElementById("invoice-form");
    if (form) {
        form.reset();
    }
    setDefaultInvoiceMonth();
}

function setDefaultInvoiceMonth() {
    const monthInput = document.getElementById("invoice-month");
    if (!monthInput || monthInput.value) {
        return;
    }

    const date = new Date();
    date.setMonth(date.getMonth() - 1);
    monthInput.value = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

function formatMonthInput(value) {
    if (!value || !value.includes("-")) {
        return value;
    }

    const [year, month] = value.split("-");
    return `${month}/${year}`;
}

function bindStudentInteractions() {
    document.querySelectorAll("[data-student-section]").forEach((item) => {
        item.addEventListener("click", () => {
            switchStudentTab(item.dataset.studentSection, item);
        });
    });

    const repairForm = document.getElementById("repair-form");
    if (repairForm) {
        repairForm.addEventListener("submit", submitRepairRequest);
    }

    const editProfileToggle = document.getElementById("edit-profile-toggle");
    if (editProfileToggle) {
        editProfileToggle.addEventListener("click", showStudentProfileForm);
    }

    const cancelProfileEdit = document.getElementById("cancel-profile-edit");
    if (cancelProfileEdit) {
        cancelProfileEdit.addEventListener("click", hideStudentProfileForm);
    }

    const profileForm = document.getElementById("student-profile-form");
    if (profileForm) {
        profileForm.addEventListener("submit", submitStudentProfileForm);
    }
}

function switchStudentTab(sectionId, clickedElement) {
    document.querySelectorAll(".content-section").forEach((sec) => {
        sec.style.display = "none";
    });

    const target = document.getElementById(sectionId);
    if (target) {
        target.style.display = "block";
    }

    document.querySelectorAll("[data-student-section]").forEach((item) => {
        item.classList.remove("active");
    });
    clickedElement.classList.add("active");
}

function loadStudentPortal(msv) {
    const emptyState = document.getElementById("student-loading-state");
    if (emptyState) {
        emptyState.style.display = "flex";
    }

    fetch(`${API_BASE}/student-portal/${encodeURIComponent(msv)}`)
        .then(async (response) => {
            const payload = await response.json();
            if (!response.ok) {
                throw new Error(payload.message || "Không thể tải dữ liệu sinh viên.");
            }
            return payload;
        })
        .then((data) => {
            currentStudentPortalData = data;
            renderStudentPortal(data);
        })
        .catch((error) => {
            console.error(error);
            renderStudentPortalError(error.message);
        });
}

function renderStudentPortal(data) {
    const student = data.student || {};
    const roomLabel = [student.tenPhong || student.idPhong, student.idToa].filter(Boolean).join(" - ");
    const roomRate = formatCurrency(student.giaThue || 0);
    const occupancy = `${student.dangO || 0}/${student.sucChua || 0}`;

    setText("student-topbar-name", student.hoTen || "Sinh viên");
    setText("student-topbar-msv", student.msv || "");
    setText("student-topbar-msv-copy", student.msv || "--");
    setText("student-avatar-role", "Sinh viên");
    setText("student-profile-name", student.hoTen || "Sinh viên");
    setText("student-profile-subtitle", `${student.msv || ""} - ${student.gioiTinh || "Chưa cập nhật"}`);

    setText("room-card-name", roomLabel || "Chưa được xếp phòng");
    setText("room-card-type", student.loaiPhong || "Chưa cập nhật");
    setText("room-card-price", roomRate);
    setText("room-card-occupancy", occupancy);
    const roomStatus = student.idPhong
    ? "Đang sử dụng phòng"
    : "Chưa được xếp phòng";

    setText("room-card-status", roomStatus);
    setText("student-phone", student.sdt || "Chưa cập nhật");
    setText("student-hometown", student.queQuan || "Chưa cập nhật");
    setText("student-birthday", student.ngaySinh || "Chưa cập nhật");
    fillStudentProfileForm(student);
    hideStudentProfileForm();

    renderRoommates(student, Array.isArray(data.roommates) ? data.roommates : []);
    renderInvoices(data.invoices || []);
    loadRepairHistory();

    const loading = document.getElementById("student-loading-state");
    if (loading) {
        loading.style.display = "none";
    }
}

function fillStudentProfileForm(student) {
    setInputValue("profile-msv", student.msv || "");
    setInputValue("profile-ho-ten", student.hoTen || "");
    setInputValue("profile-ngay-sinh", student.ngaySinh || "");
    setInputValue("profile-gioi-tinh", student.gioiTinh || "Chưa cập nhật");
    setInputValue("profile-sdt", student.sdt || "");
    setInputValue("profile-que-quan", student.queQuan || "");
}

function showStudentProfileForm() {
    const form = document.getElementById("student-profile-form");
    const student = currentStudentPortalData?.student;
    if (!form || !student) {
        return;
    }

    fillStudentProfileForm(student);
    form.style.display = "grid";
}

function hideStudentProfileForm() {
    const form = document.getElementById("student-profile-form");
    if (form) {
        form.style.display = "none";
    }
}

function submitStudentProfileForm(event) {
    event.preventDefault();

    const student = currentStudentPortalData?.student;
    if (!student?.msv) {
        alert("Không tìm thấy thông tin sinh viên.");
        return;
    }

    const payload = {
        hoTen: getInputValue("profile-ho-ten"),
        ngaySinh: getInputValue("profile-ngay-sinh"),
        gioiTinh: getInputValue("profile-gioi-tinh"),
        sdt: getInputValue("profile-sdt"),
        queQuan: getInputValue("profile-que-quan"),
        idPhong: student.idPhong || ""
    };

    fetch(`${API_BASE}/students/${encodeURIComponent(student.msv)}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(async (response) => {
            const message = await response.text();
            if (!response.ok) {
                throw new Error(message || "Không cập nhật được thông tin cá nhân.");
            }
            hideStudentProfileForm();
            loadStudentPortal(student.msv);
            alert("Đã cập nhật thông tin cá nhân.");
        })
        .catch((error) => {
            console.error(error);
            alert(error.message || "Không thể cập nhật thông tin cá nhân.");
        });
}

function renderRoommates(student, roomMates) {
    const roomMateTable = document.getElementById("roommates-table-body");
    if (!roomMateTable) {
        return;
    }

    roomMateTable.innerHTML = "";

    if (!roomMates.length) {
        roomMateTable.innerHTML = '<tr><td colspan="4" style="text-align:center;">Chưa có dữ liệu bạn cùng phòng.</td></tr>';
        return;
    }

    roomMates.forEach((mate, index) => {
        const isCurrentUser = mate.msv === student.msv;
        roomMateTable.innerHTML += `
            <tr ${isCurrentUser ? 'style="background:#f8fafc;"' : ""}>
                <td><strong>${mate.msv || ""}</strong></td>
                <td>${mate.hoTen || ""}${isCurrentUser ? " (Bạn)" : ""}</td>
                <td>Giường ${index + 1}</td>
                <td>${mate.sdt || "Chưa cập nhật"}</td>
            </tr>
        `;
    });
}

function renderInvoices(invoices) {
    const tbody = document.getElementById("invoice-table-body");
    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";

    if (!invoices.length) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Chưa có hóa đơn cho phòng hiện tại.</td></tr>';
        return;
    }

    invoices.forEach((invoice) => {
        const soDien = (invoice.chiSoDienMoi || 0) - (invoice.chiSoDienCu || 0);
        const soNuoc = (invoice.chiSoNuocMoi || 0) - (invoice.chiSoNuocCu || 0);
        const trangThai = invoice.trangThai || "Chưa thanh toán";
        const daDong = trangThai.toLowerCase().includes("đã") || trangThai.toLowerCase().includes("da");

        tbody.innerHTML += `
            <tr>
                <td>${invoice.thangNam || ""}</td>
                <td>${soDien}</td>
                <td>${soNuoc}</td>
                <td style="font-weight:700; color:${daDong ? "var(--text-main)" : "var(--danger)"};">${formatCurrency(invoice.tongTien || 0)}</td>
                <td><span class="status-pill ${daDong ? "status-success" : "status-danger"}">${trangThai}</span></td>
                <td>${daDong ? "-" : `<button class="btn btn-primary btn-small" type="button" onclick="payInvoice(${invoice.idHoaDon})">Thanh toán</button>`}</td>
            </tr>
        `;
    });
}

function loadRepairHistory() {
    const list = document.getElementById("repair-history-list");
    const student = currentStudentPortalData?.student;
    if (!list || !student?.msv) {
        return;
    }

    fetch(`${API_BASE}/repairs?msv=${encodeURIComponent(student.msv)}`)
        .then((response) => response.json())
        .then((items) => renderRepairHistory(items))
        .catch((error) => {
            console.error(error);
            list.innerHTML = '<div class="empty-note">Không tải được lịch sử sửa chữa.</div>';
        });
}

function renderRepairHistory(items) {
    const list = document.getElementById("repair-history-list");
    if (!list) {
        return;
    }

    list.innerHTML = "";

    if (!items.length) {
        list.innerHTML = '<div class="empty-note">Chưa có yêu cầu sửa chữa nào được gửi.</div>';
        return;
    }

    items.forEach((item) => {
        const status = normalizeRepairStatus(item.status);
        list.innerHTML += `
            <div class="repair-history-item">
                <div>
                    <strong>${item.category || "Khác"}</strong>
                    <p>${item.description || ""}</p>
                </div>
                <div class="repair-meta">
                    <span>${item.createdAt || ""}</span>
                    <span class="status-pill ${getRepairStatusClass(status)}">${formatRepairStatus(status)}</span>
                </div>
            </div>
        `;
    });
}

function submitRepairRequest(event) {
    event.preventDefault();

    const form = event.currentTarget;
    const student = currentStudentPortalData?.student;
    if (!student) {
        alert("Không tìm thấy thông tin sinh viên.");
        return;
    }

    if (!student.idPhong || student.idPhong.trim() === "") {
        alert("Bạn hiện chưa được xếp phòng. Chức năng gửi yêu cầu sửa chữa chỉ dành cho sinh viên đã có phòng.");
        return;
    }

    const payload = {
        msv: student.msv,
        roomId: student.idPhong || "",
        category: form.querySelector("[name='repair-category']").value,
        description: form.querySelector("[name='repair-description']").value.trim()
    };

    fetch(`${API_BASE}/repairs`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(async (response) => {
            const body = await response.json();
            if (!response.ok) {
                throw new Error(body.message || "Không gửi được yêu cầu sửa chữa.");
            }
            form.reset();
            loadRepairHistory();
            alert("Đã gửi yêu cầu sửa chữa thành công.");
        })
        .catch((error) => {
            console.error(error);
            alert(error.message || "Không thể gửi yêu cầu sửa chữa.");
        });
}

function payInvoice(invoiceId) {
    fetch(`${API_BASE}/invoices/${invoiceId}/pay`, { method: "PUT" })
        .then(async (response) => {
            const body = await response.json();
            if (!response.ok) {
                throw new Error(body.message || "Thanh toán thất bại.");
            }
            if (currentStudentPortalData?.student?.msv) {
                loadStudentPortal(currentStudentPortalData.student.msv);
            }
            alert("Đã cập nhật trạng thái thanh toán.");
        })
        .catch((error) => {
            console.error(error);
            alert(error.message || "Không thể cập nhật thanh toán.");
        });
}

function renderStudentPortalError(message) {
    const loading = document.getElementById("student-loading-state");
    if (loading) {
        loading.innerHTML = `
            <div class="empty-state-card">
                <i class="fa-solid fa-circle-exclamation"></i>
                <h3>Không tải được dữ liệu</h3>
                <p>${message}</p>
            </div>
        `;
    }
}

function normalizeRepairStatus(status) {
    const normalized = (status || "").trim().toLowerCase();
    const aliases = {
        "cho tiep nhan": "Chờ tiếp nhận",
        "da tiep nhan": "Đã tiếp nhận",
        "dang xu ly": "Đang xử lý",
        "da hoan thanh": "Đã hoàn thành"
    };
    if (aliases[normalized]) {
        return aliases[normalized];
    }
    const match = REPAIR_STATUS_SEQUENCE.find((item) => item.toLowerCase() === normalized);
    return match || "Chờ tiếp nhận";
}

function enforceAuth(requiredRole) {
    const session = getCurrentSession();
    if (!session || session.role !== requiredRole) {
        localStorage.removeItem("userSession");
        sessionStorage.setItem("authMessage", "Bạn cần đăng nhập để tiếp tục.");
        window.location.replace("login.html");
        return null;
    }
    return session;
}

function bindProtectedPageGuard(requiredRole) {
    window.addEventListener("pageshow", () => {
        enforceAuth(requiredRole);
    });

    window.addEventListener("popstate", () => {
        enforceAuth(requiredRole);
    });
}

function formatRepairStatus(status) {
    const labels = {
        "Cho tiep nhan": "Chờ tiếp nhận",
        "Da tiep nhan": "Đã tiếp nhận",
        "Dang xu ly": "Đang xử lý",
        "Da hoan thanh": "Đã hoàn thành",
        "Chờ tiếp nhận": "Chờ tiếp nhận",
        "Đã tiếp nhận": "Đã tiếp nhận",
        "Đang xử lý": "Đang xử lý",
        "Đã hoàn thành": "Đã hoàn thành"
    };
    return labels[status] || status;
}

function getRepairStatusClass(status) {
    if (status === "Đã hoàn thành") {
        return "status-success";
    }
    if (status === "Đang xử lý" || status === "Đã tiếp nhận") {
        return "status-warning";
    }
    return "status-danger";
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
    }
}

function setInputValue(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.value = value;
    }
}

function getInputValue(id) {
    const element = document.getElementById(id);
    return element ? element.value.trim() : "";
}

function formatCurrency(value) {
    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
        maximumFractionDigits: 0
    }).format(value);
}
async function exportStudentsExcel() {

    try {
        const response = await fetch("http://localhost:8081/api/students");

        const students = await response.json();

        const data = students.map(student => ({
            "Mã sinh viên": student.msv,
            "Họ tên": student.hoTen,
            "Ngày sinh": student.ngaySinh,
            "Giới tính": student.gioiTinh,
            "Số điện thoại": student.sdt,
            "Quê quán": student.queQuan
        }));

        const worksheet = XLSX.utils.json_to_sheet(data);

        const workbook = XLSX.utils.book_new();

        XLSX.utils.book_append_sheet(
            workbook,
            worksheet,
            "SinhVien"
        );

        XLSX.writeFile(
            workbook,
            "DanhSachSinhVien.xlsx"
        );

    } catch (error) {

        console.error("Lỗi export Excel:", error);

        alert("Xuất Excel thất bại!");
    }
}
