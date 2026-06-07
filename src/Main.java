import service.BookList;
import service.MemberList;
import managers.BorrowingManager;
import models.Book;
import models.Member;
import utilities.InputController;
import utilities.UIRender;

public class Main {
    private BookList bookRepo = new BookList();
    private MemberList memberRepo = new MemberList();
    private BorrowingManager txRepo = new BorrowingManager();

    public void seedData() {
        bookRepo.addBook(new Book("BOK00001", "Clean Code", "Robert C. Martin", "Technology", 2008, 5));
        bookRepo.addBook(new Book("BOK00002", "Effective Java", "Joshua Bloch", "Technology", 2018, 2));

        memberRepo.registerMember(new Member("MEM00001", "Alice Nguyen", "0901234567", "alice@gmail.com"));
        memberRepo.registerMember(new Member("MEM00002", "Bob Tran", "0987654321", "bob@gmail.com"));
    }

    public void runMenu() {
        // Cập nhật mảng chuỗi hiển thị giao diện theo đúng yêu cầu hàm renderMenu
        String[] menuOptions = {
                "Add New Book",
                "Register Member",
                "Issue Book Loan (Borrow)",
                "Process Book Return (Return)", // Thêm chức năng xử lý trả sách vào menu
                "Display All Books",
                "Display All Members",
                "Display All Transactions",
                "Exit System"
        };

        while (true) {
            UIRender.renderMenu("Library Management Dashboard", menuOptions);

            int choice = InputController.getInt("\nEnter your selection: ");

            switch (choice) {
                case 1:
                    UIRender.renderHeader("Add New Book");
                    String bId = InputController.getString("Enter Book ID: ");
                    String title = InputController.getString("Enter Title: ");
                    String author = InputController.getString("Enter Author: ");
                    String genre = InputController.getString("Enter Genre: ");
                    int year = InputController.getInt("Enter Publication Year: ");
                    int qty = InputController.getInt("Enter Initial Stock Quantity: ");
                    bookRepo.addBook(new Book(bId, title, author, genre, year, qty));
                    break;

                case 2:
                    UIRender.renderHeader("Register Member");
                    String mId = InputController.getString("Enter Member ID: ");
                    String name = InputController.getString("Enter Full Name: ");
                    String phone = InputController.getString("Enter Phone Number: ");
                    String email = InputController.getString("Enter Email Address: ");
                    memberRepo.registerMember(new Member(mId, name, phone, email));
                    break;

                case 3:
                    UIRender.renderHeader("Process Borrow Transaction");
                    String txId = InputController.getString("Enter Transaction ID: ");
                    String memberId = InputController.getString("Enter Member ID: ");
                    String bookId = InputController.getString("Enter Book ID: ");

                    Member targetMember = memberRepo.searchById(memberId);
                    Book targetBook = bookRepo.searchById(bookId);

                    txRepo.processBorrow(txId, targetMember, targetBook);
                    break;

                case 4: // CHỨC NĂNG MỚI: Tiếp nhận và xử lý trả sách từ thủ thư
                    UIRender.renderHeader("Process Book Return");
                    String returnTxId = InputController.getString("Enter Transaction ID to return: ");
                    txRepo.processReturn(returnTxId);
                    break;

                case 5:
                    bookRepo.displayAll();
                    break;

                case 6:
                    memberRepo.displayAll();
                    break;

                case 7:
                    txRepo.displayAllTransactions();
                    break;

                case 8: // Thoát hệ thống lúc này dịch chuyển xuống case số 8 vì mảng tăng thêm 1 phần tử
                    System.out.println("\nShutting down application. Goodbye!");
                    return;

                default:
                    UIRender.renderError("Invalid choice! Please choose a valid menu number.");
            }
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.seedData();
        app.runMenu();
    }
}