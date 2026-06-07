import service.*;
import models.*;
import utilities.InputController;
import utilities.UIRender;

import java.time.LocalDate;

public class Main {
    private static final BookManagement bookRepo = new BookManagement();
    private static final MemberManagement memberRepo = new MemberManagement();
    private static final BorrowTransactionManagement txRepo = new BorrowTransactionManagement();

    private static final LibraryService coreService = new LibraryService(bookRepo, memberRepo, txRepo);
    private static final ReportManagement reportEngine = new ReportManagement(bookRepo, memberRepo, txRepo);

    public static void main(String[] args) {
        seedData();

        while (true) {
            UIRender.clearScreen();
            String[] menus = {
                    "Catalog Terminal (CRUD Book)",
                    "Membership Terminal (CRUD Member)",
                    "Circulation Desk (Borrow & Return)",
                    "Analytics & Reporting Center"
            };
            UIRender.renderMenu("Library Core Decoupled Management System Engine", menus);
            int cat = InputController.getInt("Select domain department: ");

            switch (cat) {
                case 1: bookCliLoop(); break;
                case 2: memberCliLoop(); break;
                case 3: circulationCliLoop(); break;
                case 4: reportCliLoop(); break;
                case 0: UIRender.renderSuccess("Exiting production workspace. System shutdown cleanly."); System.exit(0);
            }
        }
    }

    private static void bookCliLoop() {
        while (true) {
            UIRender.clearScreen();
            String[] ops = {"Add Book", "Display Inventory Status", "Modify Records", "Liquidate Book (Hard-Delete)"};
            UIRender.renderMenu("Catalog Asset Sub-Terminal", ops);
            int op = InputController.getInt("Select: ");
            if (op == 0) return;

            switch (op) {
                case 1: Book b = bookRepo.inputBook(); if (b!=null) bookRepo.add(b); break;
                case 2: bookRepo.displayAllBooks(); break;
                case 3:
                    String upId = InputController.getString("Book ID: ");
                    Book data = bookRepo.inputBook();
                    if (data != null) bookRepo.update(upId, data);
                    break;
                case 4:
                    String delId = InputController.getString("Book ID to Liquidate: ");
                    coreService.deleteBook(delId);
                    break;
            }
            UIRender.pauseEnter();
        }
    }

    private static void memberCliLoop() {
        while (true) {
            UIRender.clearScreen();
            String[] ops = {"Register New Profile", "Display Directory", "Modify Profile Details", "Expunge Account (Hard-Delete)"};
            UIRender.renderMenu("Membership Registration Sub-Terminal", ops);
            int op = InputController.getInt("Select: ");
            if (op == 0) return;

            switch (op) {
                case 1: Member m = memberRepo.inputMember(); if(m!=null) memberRepo.addMember(m); break;
                case 2: memberRepo.displayAllMembers(); break;
                case 3:
                    String mid = InputController.getString("Member ID: ");
                    Member data = memberRepo.inputMember();
                    if (data != null) memberRepo.updateMember(mid, data);
                    break;
                case 4:
                    String wipeId = InputController.getString("Member ID to Wipe: ");
                    if(!coreService.deleteMember(wipeId))
                        UIRender.renderError("Remove failed! No member found!");
                    break;
            }
            UIRender.pauseEnter();
        }
    }

    private static void circulationCliLoop() {
        while (true) {
            UIRender.clearScreen();
            String[] ops = {"Book Borrow Check-out Event", "Book Return Check-in Event", "View Raw Trans Journal Logs"};
            UIRender.renderMenu("Circulation Operations Service Desk", ops);
            int op = InputController.getInt("Select: ");
            if (op == 0) return;

            switch (op) {
                case 1:
                    coreService.borrowBook(InputController.getString("Member ID: "), InputController.getString("Book ID: "));
                    break;
                case 2:
                    String txId = InputController.getString("Transaction ID: ");
                    double cash = InputController.getDouble(" upfront fine cash paid: ");
                    coreService.returnBook(txId, cash);
                    break;
                case 3: txRepo.displayRawTransactions(); break;
            }
            UIRender.pauseEnter();
        }
    }

    private static void reportCliLoop() {
        while (true) {
            UIRender.clearScreen();
            String[] ops = {"View Individual Member Borrow History Logs", "View Overdue Alert Reports", "View Debt Financial Status", "View Popular Books Tractions"};
            UIRender.renderMenu("Reporting, Auditing & Data Join Engine", ops);
            int op = InputController.getInt("Select: ");
            if (op == 0) return;

            switch (op) {
                case 1: reportEngine.displayMemberHistory(InputController.getString("Member ID: ")); break;
                case 2: reportEngine.displayOverdueTransactions(); break;
                case 3: reportEngine.displayMembersInDebt(); break;
                case 4: reportEngine.displayPopularBooks(); break;
            }
            UIRender.pauseEnter();
        }
    }

    public static void seedData() {
        UIRender.renderHeader("SYSTEM DATA SEEDING INITIALIZATION");

        // 1. Khởi tạo và nạp danh sách Sách mẫu (BOKxxxxx)
        Book b1 = new Book("BOK00001", "Clean Code", "Robert C. Martin", "Technology", 2008, 3, 120000);
        Book b2 = new Book("BOK00002", "Effective Java", "Joshua Bloch", "Technology", 2018, 2, 150000);
        Book b3 = new Book("BOK00003", "Design Patterns", "Gang of Four", "Software Engineering", 1994, 1, 200000);

        bookRepo.add(b1);
        bookRepo.add(b2);
        bookRepo.add(b3);

        // 2. Khởi tạo và nạp danh sách Thành viên mẫu (MEMxxxxx)
        Member m1 = new Member("MEM00001", "Alice Nguyen", "alice@gmail.com", "0901234567", 3);
        Member m2 = new Member("MEM00002", "Bob Tran", "bob@gmail.com", "0987654321", 2);

        memberRepo.addMember(m1);
        memberRepo.addMember(m2);

        // 3. Khởi tạo và nạp danh sách Giao dịch mẫu (BTSxxxxx)
        // Vì kiến trúc mới quy định BorrowTransaction lưu reference bằng ID chuỗi, ta chèn trực tiếp:
        java.time.LocalDate today = java.time.LocalDate.now();

        // Giao dịch 1: Alice mượn sách Clean Code (Hạn 14 ngày)
        BorrowTransaction tx1 = new BorrowTransaction("BTS00001", "BOK00001", "MEM00001", today, today.plusDays(14));
        txRepo.addTransaction(tx1);
        m1.getTransactionIds().add(tx1.getId()); // Append ID vào hồ sơ Member
        b1.setRealtimeQuantity(b1.getRealtimeQuantity() - 1); // Trừ kho vật lý
        m1.setMaxBorrowLimit(m1.getMaxBorrowLimit() - 1);     // Trừ slot mượn của Member

        // Giao dịch 2: Bob mượn sách Effective Java (Hạn 14 ngày)
        BorrowTransaction tx2 = new BorrowTransaction("BTS00002", "BOK00002", "MEM00002", today, today.plusDays(14));
        txRepo.addTransaction(tx2);
        m2.getTransactionIds().add(tx2.getId()); // Append ID vào hồ sơ Member
        b2.setRealtimeQuantity(b2.getRealtimeQuantity() - 1); // Trừ kho vật lý
        m2.setMaxBorrowLimit(m2.getMaxBorrowLimit() - 1);     // Trừ slot mượn của Member

        System.out.println("[SUCCESS] Hardcoded seed data successfully injected into RAM repositories.");

        // Xuất bảng kiểm tra trạng thái sau khi seed
        bookRepo.displayAllBooks();
        memberRepo.displayAllMembers();
        txRepo.displayRawTransactions();

        UIRender.pauseEnter("Press Enter to deploy main dashboard console...");
    }
}