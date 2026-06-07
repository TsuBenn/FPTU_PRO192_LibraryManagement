package service;

import models.Member;
import utilities.IDGenerator;
import utilities.InputController;
import utilities.Validator;
import utilities.UIRender;

import java.util.ArrayList;
import java.util.List;

public class MemberManagement {
    private final List<Member> members;
    private final IDGenerator idGenerator;

    public MemberManagement() {
        this.members = new ArrayList<>();
        this.idGenerator = new IDGenerator("MEM", 3);
    }


    public List<Member> getAllMember() {
        return this.members;
    }

    private boolean isInvalidMember(Member member) {
        return member == null || !Validator.isValidString(member.getName())
                || !Validator.isValidPhone(member.getPhoneNumber())
                || !Validator.isValidEmail(member.getEmail());
    }

    public Member inputMember() {
        String name = InputController.getString("Enter member name: ");
        String email = InputController.getString("Enter member email: ");
        String phoneNumber = InputController.getString("Enter phone number: ");
        int maxBorrowLimit = InputController.getInt("Enter max borrow limit slots: ");

        if (maxBorrowLimit <= 0) {
            UIRender.renderError("Invalid limit data. Limit slots must be greater than 0.");
            return null;
        }
        return new Member(idGenerator.newID(), name, email, phoneNumber, maxBorrowLimit);
    }

    /**
     * C - CREATE: Đăng ký thành viên mới
     */
    public boolean addMember(Member member) {
        if (isInvalidMember(member)) {
            UIRender.renderError("Failed to register member. Provided data is invalid or empty.");
            return false;
        }
        members.add(member);
        return true;
    }

    /**
     * R - READ: Tìm kiếm thành viên theo ID chính xác
     */
    public Member findById(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) return null;
        for (Member m : members) {
            if (m.getId().equalsIgnoreCase(memberId.trim())) {
                return m;
            }
        }
        return null;
    }

    public List<Member> findMemberByName(String name) {
        List<Member> result = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) return result;
        for (Member m : members) {
            if (m.getName().toLowerCase().contains(name.toLowerCase().trim())) {
                result.add(m);
            }
        }
        return result;
    }

    public Member findMemberByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        for (Member m : members) {
            if (m.getEmail().equalsIgnoreCase(email.trim())) return m;
        }
        return null;
    }

    public boolean updateMember(String memberId, Member updatedMember) {
        Member existingMember = findById(memberId);
        if (existingMember == null || updatedMember == null) return false;
        if (isInvalidMember(updatedMember))
            return false;

        existingMember.setName(updatedMember.getName().trim());
        existingMember.setEmail(updatedMember.getEmail().trim());
        existingMember.setPhoneNumber(updatedMember.getPhoneNumber().trim());
        existingMember.setMaxBorrowLimit(updatedMember.getMaxBorrowLimit());
        return true;
    }

    /**
     * D - DELETE: Xóa cứng thành viên khỏi danh sách active
     * RÀNG BUỘC PHÒNG THỦ: Kể cả khi LibraryService đã check, hàm này vẫn giữ chốt chặn tài chính.
     */
    public boolean removeMember(String memberId) {
        Member member = findById(memberId);
        if (member == null) return false;

        // CHỐT CHẶN BẤT BIẾN: CònX nợ phạt tuyệt đối không cho phép xóa khỏi danh sách quản lý
        if (member.getFineMoney() > 0.0) {
            UIRender.renderError("Hard-Delete REJECTED! Member '" + member.getName() + "' still owes: " + member.getFineMoney() + " VND.");
            return false;
        }

        members.remove(member);
        return true;
    }

    /**
     * U - UPDATE (Tài chính): Xử lý đóng tiền phạt tại quầy
     */
    public boolean processFinePayment(String memberId, double amountPaid) {
        Member member = findById(memberId);
        if (member == null) return false;

        if (amountPaid <= 0 || amountPaid > member.getFineMoney()) {
            UIRender.renderError("Payment rejected! Invalid cash counter input bounds.");
            return false;
        }

        member.setFineMoney(member.getFineMoney() - amountPaid);
        return true;
    }

    /**
     * Độc lập xuất bảng danh sách dữ liệu Member
     */
    public void displayAllMembers() {
        UIRender.renderHeader("Library Active Members Directory");
        String[] headers = {"Member ID", "Full Name", "Email Address", "Phone Number", "Slots", "Accumulated Fine"};
        List<String[]> dataRows = new ArrayList<>();

        for (Member m : members) {
            dataRows.add(new String[]{
                    m.getId(),
                    m.getName(),
                    m.getEmail(),
                    m.getPhoneNumber(),
                    String.valueOf(m.getMaxBorrowLimit()),
                    String.format("%.2f VND", m.getFineMoney())
            });
        }

        if (dataRows.isEmpty()) {
            UIRender.renderError("No registered members found in the database.");
            return;
        }
        UIRender.renderTable(headers, dataRows);
    }
}