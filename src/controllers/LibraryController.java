package controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.Book;
import models.Member;
import models.BorrowTransaction;
import managers.BookManager;
import managers.MemberManager;
import managers.BorrowTransactionManager;
import utilities.Input;
import utilities.UIRender;
import utilities.Validator;

public class LibraryController {
    private BookManager bookManager;
    private MemberManager memberManager;
    private BorrowTransactionManager txManager;

    public LibraryController() {
        this.bookManager = new BookManager();
        this.memberManager = new MemberManager();
        this.txManager = new BorrowTransactionManager();
        seedData();
    }

    public void start() {
        boolean running = true;
        String[] menuOptions = {
            "Add New Book",
            "View All Books",
            "Register Member",
            "View All Members",
            "Borrow a Book",
            "Exit Application"
        };

        while (running) {
            UIRender.clearScreen();
            UIRender.renderMenu("Milestone 1 System Core", menuOptions);
            int choice = Input.getInt("Select option index: ");

            switch (choice) {
                case 1:
                    addNewBookWorkflow();
                    break;
                case 2:
                    listAllBooksWorkflow();
                    break;
                case 3:
                    registerMemberWorkflow();
                    break;
                case 4:
                    listAllMembersWorkflow();
                    break;
                case 5:
                    borrowBookWorkflow();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    UIRender.renderError("Invalid action profile menu index selection.");
                    UIRender.pauseEnter();
            }
        }
    }

    private void addNewBookWorkflow() {
        UIRender.clearScreen();
        UIRender.renderHeader("Add Book Workspace Instance");
        String id = Input.getString("Enter Book ID: ");
        String title = Input.getString("Enter Title: ");
        String author = Input.getString("Enter Author: ");
        String genre = Input.getString("Enter Genre: ");
        int year = Input.getInt("Enter Publication Year: ");
        int qty = Input.getInt("Enter Total Quantity: ");

        Book b = new Book(id, title, author, genre, year, qty);
        if (bookManager.addBook(b)) {
            UIRender.renderSuccess("Book created and registered in-memory mapping.");
        } else {
            UIRender.renderError("Constraint Fault: Book ID duplicate detected.");
        }
        UIRender.pauseEnter();
    }

    private void listAllBooksWorkflow() {
        UIRender.clearScreen();
        UIRender.renderHeader("System Asset Inventory Catalog");
        
        List<Book> books = bookManager.getAllBooks();
        String[] headers = {"ID", "Title Name", "Author Name", "Genre Reference", "Year", "Stock Left"};
        List<String[]> rows = new ArrayList<>();
        
        for (Book b : books) {
            rows.add(new String[] {
                b.getId(), b.getTitle(), b.getAuthor(), b.getGenre(),
                String.valueOf(b.getPublicationYear()), String.valueOf(b.getAvailableQuantity())
            });
        }
        UIRender.renderTable(headers, rows);
        UIRender.pauseEnter();
    }

    private void registerMemberWorkflow() {
        UIRender.clearScreen();
        UIRender.renderHeader("Membership Account Registration Form");
        String id = Input.getString("Enter Member ID: ");
        String name = Input.getString("Enter Member Full Name: ");
        
        String phone = Input.getString("Enter Contact Phone Number (10 digits): ");
        if (!Validator.isValidPhone(phone)) {
            UIRender.renderError("Format Error: Phone number must follow 10 digit structure format validation rules.");
            UIRender.pauseEnter();
            return;
        }

        String email = Input.getString("Enter Active Email Address: ");
        if (!Validator.isValidEmail(email)) {
            UIRender.renderError("Format Error: Email string breaks structural validation rules template pattern.");
            UIRender.pauseEnter();
            return;
        }

        Member m = new Member(id, name, phone, email);
        if (memberManager.addMember(m)) {
            UIRender.renderSuccess("Member profile registered successfully on current thread.");
        } else {
            UIRender.renderError("Constraint Fault: Member ID duplicate match detected.");
        }
        UIRender.pauseEnter();
    }

    private void listAllMembersWorkflow() {
        UIRender.clearScreen();
        UIRender.renderHeader("System Registered Roster Index");
        
        List<Member> members = memberManager.getAllMembers();
        String[] headers = {"ID Code", "Member Name String", "Phone Connection", "Email Domain Link"};
        List<String[]> rows = new ArrayList<>();
        
        for (Member m : members) {
            rows.add(new String[] { m.getId(), m.getName(), m.getPhone(), m.getEmail() });
        }
        UIRender.renderTable(headers, rows);
        UIRender.pauseEnter();
    }

    private void borrowBookWorkflow() {
        UIRender.clearScreen();
        UIRender.renderHeader("Circulation Desk Loan Processing Line");
        String txId = Input.getString("Enter Transaction Assignment Tracking ID: ");
        String memberId = Input.getString("Enter Target Member ID: ");
        String bookId = Input.getString("Enter Target Book ID: ");

        System.out.println("\nSpecify Check-out Calendar Date Timestamps:");
        int d = Input.getInt("Day (DD): ");
        int m = Input.getInt("Month (MM): ");
        int y = Input.getInt("Year (YYYY): ");

        if (!Validator.isValidDate(d, m, y)) {
            UIRender.renderError("Chronology Failure: Date constraints parameter parsing mismatch.");
            UIRender.pauseEnter();
            return;
        }
        LocalDate borrowDate = LocalDate.of(y, m, d);

        txManager.borrowBook(txId, memberId, bookId, borrowDate);
        UIRender.renderSuccess("Circulation transaction record built structurally without runtime filters constraint checking.");
        UIRender.pauseEnter();
    }

    private void seedData() {
        bookManager.addBook(new Book("B001", "The Great Gatsby", "F. Scott Fitzgerald", "Classic", 1925, 5));
        bookManager.addBook(new Book("B002", "1984", "George Orwell", "Dystopian", 1949, 3));
        memberManager.addMember(new Member("M001", "Benn", "0123456789", "benn@uni.edu.vn"));
    }
}
