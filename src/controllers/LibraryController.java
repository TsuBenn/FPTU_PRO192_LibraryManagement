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
import managers.ReportManager;
import utilities.Input;
import utilities.UIRender;
import utilities.Validator;

public class LibraryController {
    private BookManager bookManager;
    private MemberManager memberManager;
    private BorrowTransactionManager txManager;
    private ReportManager reportManager;

    public LibraryController() {
        this.bookManager = new BookManager();
        this.memberManager = new MemberManager();
        this.txManager = new BorrowTransactionManager(bookManager, memberManager);
        this.reportManager = new ReportManager(bookManager, memberManager, txManager);
        seedData();
    }

    public void start() {
        boolean running = true;
        String[] mainOptions = {
                "Book Management System",
                "Member Management System",
                "Borrow Circulation Center",
                "Return Processing Center",
                "Report Lost Book Asset",
                "Analytics & Reports Dashboard",
                "Exit Application Framework"
        };

        while (running) {
            UIRender.clearScreen();
            UIRender.renderMenu("Library Administration Framework Core", mainOptions);

            int choice = Input.getInt("Select system component option: ");
            switch (choice) {
                case 1: handleBookMenu(); break;
                case 2: handleMemberMenu(); break;
                case 3: handleBorrowWorkflow(); break;
                case 4: handleReturnWorkflow(); break;
                case 5: handleLostBookWorkflow(); break;
                case 6: handleReportMenu(); break;
                case 0: running = false; break;
                default:
                    UIRender.renderError("Invalid core framework option path selection!");
                    UIRender.pauseEnter();
            }
        }
    }

    // =========================================================================
    // 1. BOOK MANAGEMENT SUBSYSTEM
    // =========================================================================
    private void handleBookMenu() {
        boolean inMenu = true;
        String[] bookOptions = { "Add Asset Book", "Remove Asset Book", "Update Book Details", "List Book Inventory" };

        while (inMenu) {
            UIRender.clearScreen();
            UIRender.renderMenu("Book Inventory Sub-Registry", bookOptions);
            int choice = Input.getInt("Select action index: ");

            if (choice == 1) { // CREATE
                UIRender.clearScreen();
                UIRender.renderHeader("Inventory Creation Workspace");

                String title = Input.getString("Enter Title: ").trim();
                String author = Input.getString("Enter Author: ").trim();
                String genre = Input.getString("Enter Genre: ").trim();

                int year = 0;
                while (true) {
                    String rawYear = Input.getString("Enter Publication Year (YYYY): ");
                    if (Validator.isValidInt(rawYear)) {
                        year = Integer.parseInt(rawYear);
                        if (year >= 0 && year <= LocalDate.now().getYear()) {
                            break;
                        }
                    }
                    UIRender.renderError("Year must be between 0 and " + LocalDate.now().getYear() + ".");
                }

                int qty = 0;
                while (true) {
                    String rawQty = Input.getString("Enter Total Stock Quantity: ");
                    if (Validator.isValidInt(rawQty)) {
                        qty = Integer.parseInt(rawQty);
                        if (qty > 0) {
                            break;
                        }
                    }
                    UIRender.renderError("Quantity must be greater than 0.");
                }

                Book book = new Book(title, author, genre, year, qty);
                bookManager.addBook(book);
                UIRender.renderSuccess("Book registered safely. ID: " + book.getId());
                UIRender.pauseEnter();

            } else if (choice == 2) { // DELETION
                UIRender.clearScreen();
                UIRender.renderHeader("Inventory Purge Selection Module");
                Book b = searchAndSelectBook();
                if (b == null) continue;

                if (b.getAvailableQuantity() != b.getTotalQuantity()) {
                    UIRender.renderError("Purge Lock Triggered: Copies of this book are currently out on loan!");
                } else {
                    bookManager.getAllBooks().remove(b);
                    UIRender.renderSuccess("Asset record purged cleanly.");
                }
                UIRender.pauseEnter();

            } else if (choice == 3) { // UPDATE (FIXED SPECIFIC LOGIC FOR YEAR AND MAX QUANTITY)
                UIRender.clearScreen();
                UIRender.renderHeader("Asset Modification Workspace");
                Book b = searchAndSelectBook();
                if (b == null) continue;

                System.out.println("\n[INFO] Press ENTER without typing to skip field alteration & preserve values.\n");
                String t = Input.getString("Update Title [" + b.getTitle() + "]: ").trim();
                String a = Input.getString("Update Author [" + b.getAuthor() + "]: ").trim();
                String g = Input.getString("Update Genre [" + b.getGenre() + "]: ").trim();

                if (!t.isEmpty()) b.setTitle(t);
                if (!a.isEmpty()) b.setAuthor(a);
                if (!g.isEmpty()) b.setGenre(g);

                // Fixed logic for: year
                while (true) {
                    String yrRaw = Input.getString("Update Publication Year [" + b.getPublicationYear() + "]: ");
                    if (yrRaw.isEmpty()) break;

                    if (Validator.isValidInt(yrRaw)) {
                        int parsedYear = Integer.parseInt(yrRaw);
                        if (parsedYear >= 0 && parsedYear <= LocalDate.now().getYear()) {
                            b.setPublicationYear(parsedYear);
                            break;
                        }
                    }
                    UIRender.renderError("Invalid Year: Must be between 0 and " + LocalDate.now().getYear() + ".");
                }

                // Fixed logic for: maxQuantity (Not allowed to touch realTimeQuantity directly)
                while (true) {
                    String qtyRaw = Input.getString("Update Max Stock Quantity [" + b.getTotalQuantity() + "]: ");
                    if (qtyRaw.isEmpty()) break;

                    if (Validator.isValidInt(qtyRaw)) {
                        int newTotal = Integer.parseInt(qtyRaw);
                        int checkedOut = b.getTotalQuantity() - b.getAvailableQuantity();

                        if (newTotal < checkedOut) {
                            UIRender.renderError("Validation Error: Max quantity cannot be less than current borrowed units (" + checkedOut + ").");
                        } else if (newTotal <= 0) {
                            UIRender.renderError("Validation Error: Total quantity must be greater than 0.");
                        } else {
                            b.setTotalQuantity(newTotal);
                            b.setAvailableQuantity(newTotal - checkedOut); // Recalculate realTimeQuantity automatically
                            UIRender.renderSuccess("Quantity properties updated successfully.");
                            break;
                        }
                    } else {
                        UIRender.renderError("Must be a valid integer number.");
                    }
                }
                UIRender.pauseEnter();

            } else if (choice == 4) {
                UIRender.clearScreen();
                UIRender.renderHeader("Full Active Library Asset Manifest");
                renderBookDatabaseTable(bookManager.getAllBooks());
                UIRender.pauseEnter();
            } else if (choice == 0) {
                inMenu = false;
            }
        }
    }

    // =========================================================================
    // 2. MEMBER MANAGEMENT SUBSYSTEM
    // =========================================================================
    private void handleMemberMenu() {
        boolean inMenu = true;
        String[] memberOptions = { "Register New Member", "Revoke Member Account", "Modify Member Details", "List Directory Roster" };

        while (inMenu) {
            UIRender.clearScreen();
            UIRender.renderMenu("Member Registry Sub-Framework", memberOptions);
            int choice = Input.getInt("Select action index: ");

            if (choice == 1) { // CREATE
                UIRender.clearScreen();
                UIRender.renderHeader("Account Registration Terminal");

                String name = Input.getString("Enter Full Legal Name: ").trim();
                String phone = Input.getString("Enter Contact Phone: ").trim();
                String email = Input.getString("Enter Email: ").trim();

                Member m = new Member(name, phone, email);
                memberManager.addMember(m);
                UIRender.renderSuccess("Member profile instantiated. Assigned ID: " + m.getId());
                UIRender.pauseEnter();

            } else if (choice == 2) { // DELETION
                UIRender.clearScreen();
                UIRender.renderHeader("Account Revocation Module");
                Member m = searchAndSelectMember();
                if (m == null) continue;

                if (countActiveBorrows(m.getId()) > 0) {
                    UIRender.renderError("Revocation Denied: Account currently holds outstanding borrowed assets!");
                } else {
                    memberManager.getAllMembers().remove(m);
                    UIRender.renderSuccess("User account card revoked cleanly.");
                }
                UIRender.pauseEnter();

            } else if (choice == 3) { // UPDATE
                UIRender.clearScreen();
                UIRender.renderHeader("Profile Modification Workspace");
                Member m = searchAndSelectMember();
                if (m == null) continue;

                System.out.println("\n[INFO] Press ENTER without typing to skip field alteration & preserve values.\n");
                String n = Input.getString("Update Name [" + m.getName() + "]: ").trim();
                String p = Input.getString("Update Phone [" + m.getPhone() + "]: ").trim();
                String e = Input.getString("Update Email [" + m.getEmail() + "]: ").trim();

                if (!n.isEmpty()) m.setName(n);
                if (!p.isEmpty()) m.setPhone(p);
                if (!e.isEmpty()) m.setEmail(e);

                UIRender.renderSuccess("Profile updates saved successfully.");
                UIRender.pauseEnter();

            } else if (choice == 4) {
                UIRender.clearScreen();
                UIRender.renderHeader("Registered Library Membership Directory");
                renderMemberDatabaseTable(memberManager.getAllMembers());
                UIRender.pauseEnter();
            } else if (choice == 0) {
                inMenu = false;
            }
        }
    }

    // =========================================================================
    // 3. BORROW CIRCULATION MODULE
    // =========================================================================
    private void handleBorrowWorkflow() {
        UIRender.clearScreen();
        UIRender.renderHeader("Circulation Desk: Identify Member");
        Member member = searchAndSelectMember();
        if (member == null) return;

        UIRender.renderHeader("Circulation Desk: Identify Target Book Resource");
        Book book = searchAndSelectBook();
        if (book == null) return;

        if (book.getAvailableQuantity() <= 0) {
            UIRender.renderError("Checkout Rejected: Out of Stock.");
            UIRender.pauseEnter();
            return;
        }
        if (countActiveBorrows(member.getId()) >= member.getBorrowLimit()) {
            UIRender.renderError("Checkout Rejected: Member has reached their open loan limit.");
            UIRender.pauseEnter();
            return;
        }
        if (isBookAlreadyBorrowedByMember(member.getId(), book.getId())) {
            UIRender.renderError("Checkout Rejected: Double-instance restriction. Member already holds an active copy.");
            UIRender.pauseEnter();
            return;
        }

        LocalDate borrowDate = LocalDate.now();
        BorrowTransaction tx = new BorrowTransaction(member.getId(), book.getId(), borrowDate);

        System.out.println("\nDue Date Configuration Pipeline (Default is 14 days):");
        System.out.println("  [1] Retain standard baseline (Due date: " + tx.getDueDate() + ")");
        System.out.println("  [2] Configure specific custom return date threshold");
        int option = Input.getInt("Select structural strategy index: ");

        if (option == 2) {
            // Fixed logic for: dueDate validation
            while (true) {
                System.out.println("\n");
                LocalDate customDueDate = Input.getDate("--- Input Custom Due Date ---");

                if (customDueDate.isBefore(borrowDate)) {
                    UIRender.renderError("Chronology Violation: Due date cannot be set behind the transaction checkout date.");
                } else {
                    tx.setDueDate(customDueDate);
                    break;
                }
            }
        }

        txManager.getTransactions().add(tx);
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);

        UIRender.renderSuccess("Circulation record established. Reference key: " + tx.getTransactionId());
        UIRender.pauseEnter();
    }

    // =========================================================================
    // 4. RETURN RECONCILIATION PROCESSING
    // =========================================================================
    private void handleReturnWorkflow() {
        UIRender.clearScreen();
        UIRender.renderHeader("Return Processing: Identify Member");
        Member member = searchAndSelectMember();
        if (member == null) return;

        List<BorrowTransaction> activeLoans = new ArrayList<>();
        for (BorrowTransaction tx : txManager.getTransactions()) {
            if (tx.getMemberId().equalsIgnoreCase(member.getId()) && tx.getReturnDate() == null) {
                activeLoans.add(tx);
            }
        }

        if (activeLoans.isEmpty()) {
            UIRender.renderError("No outstanding active open loan balances found.");
            UIRender.pauseEnter();
            return;
        }

        String[] headers = {"Index", "Asset Key", "Resource Title", "Borrow Date", "Allocated Due Date"};
        List<String[]> loanTable = new ArrayList<>();
        for (int i = 0; i < activeLoans.size(); i++) {
            BorrowTransaction tx = activeLoans.get(i);
            Book book = bookManager.findBookById(tx.getBookId());
            String title = (book != null) ? book.getTitle() : "Detached Record";
            loanTable.add(new String[] { String.valueOf(i + 1), tx.getBookId(), title, tx.getBorrowDate().toString(), tx.getDueDate().toString() });
        }
        UIRender.renderTable(headers, loanTable);

        int idx = Input.getInt("Select transaction index target to restore: ") - 1;
        if (idx < 0 || idx >= activeLoans.size()) return;
        BorrowTransaction targetTx = activeLoans.get(idx);

        if (targetTx.getReturnDate() != null) {
            UIRender.renderError("Mutation Rejection: This completed transaction record is locked.");
            UIRender.pauseEnter();
            return;
        }

        LocalDate returnDate = LocalDate.now();
        targetTx.setReturnDate(returnDate);

        Book book = bookManager.findBookById(targetTx.getBookId());
        if (book != null) {
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        }

        long daysPastDue = java.time.temporal.ChronoUnit.DAYS.between(targetTx.getDueDate(), returnDate);
        if (daysPastDue > 0) {
            double fine = daysPastDue * 5000.0;
            targetTx.setFinePaid(fine);
            System.out.printf("\n>>> LATE PENALTY FEES INCURRED: %,.0f VND\n", fine);
        } else {
            UIRender.renderSuccess("Asset returned on schedule. Total liability: 0 VND.");
        }
        UIRender.pauseEnter();
    }

    // =========================================================================
    // 5. EMERGENCY PIPELINE: LOST BOOK DECLARATION
    // =========================================================================
    private void handleLostBookWorkflow() {
        UIRender.clearScreen();
        UIRender.renderHeader("Lost Asset Emergency Declaration Terminal");
        Member member = searchAndSelectMember();
        if (member == null) return;

        List<BorrowTransaction> activeLoans = new ArrayList<>();
        for (BorrowTransaction tx : txManager.getTransactions()) {
            if (tx.getMemberId().equalsIgnoreCase(member.getId()) && tx.getReturnDate() == null) {
                activeLoans.add(tx);
            }
        }

        if (activeLoans.isEmpty()) {
            UIRender.renderError("This member profile possesses zero outstanding active accounts.");
            UIRender.pauseEnter();
            return;
        }

        String[] headers = {"Index", "Asset Key", "Resource Title", "Expected Due Date"};
        List<String[]> loanTable = new ArrayList<>();
        for (int i = 0; i < activeLoans.size(); i++) {
            BorrowTransaction tx = activeLoans.get(i);
            Book book = bookManager.findBookById(tx.getBookId());
            String title = (book != null) ? book.getTitle() : "Detached Record";
            loanTable.add(new String[] { String.valueOf(i + 1), tx.getBookId(), title, tx.getDueDate().toString() });
        }
        UIRender.renderTable(headers, loanTable);

        int idx = Input.getInt("Select row index matching the lost library book: ") - 1;
        if (idx < 0 || idx >= activeLoans.size()) return;
        BorrowTransaction targetTx = activeLoans.get(idx);

        if (targetTx.getReturnDate() != null) {
            UIRender.renderError("Mutation Rejection: This completed transaction history is locked.");
            UIRender.pauseEnter();
            return;
        }

        Book book = bookManager.findBookById(targetTx.getBookId());
        if (book == null) {
            UIRender.renderError("System Fault: Targeted inventory source record not found.");
            UIRender.pauseEnter();
            return;
        }

        double assetValue = -1.0;
        while (assetValue < 0) {
            String rawVal = Input.getString("Enter standard book assessment replacement value (VND): ");
            boolean numeric = true;
            for (char c : rawVal.toCharArray()) {
                if (!Character.isDigit(c) && c != '.') numeric = false;
            }
            if (numeric && !rawVal.isEmpty()) {
                assetValue = Double.parseDouble(rawVal);
                if (assetValue >= 0) break;
            }
            UIRender.renderError("Replacement cost cannot evaluate as a negative number.");
        }

        LocalDate today = LocalDate.now();
        targetTx.setReturnDate(today);

        long daysPastDue = java.time.temporal.ChronoUnit.DAYS.between(targetTx.getDueDate(), today);
        double latePenalty = (daysPastDue > 0) ? (daysPastDue * 5000.0) : 0.0;
        double replacementCost = assetValue * 1.00;
        double totalCompoundFine = replacementCost + latePenalty;
        targetTx.setFinePaid(totalCompoundFine);

        book.setTotalQuantity(book.getTotalQuantity() - 1);

        UIRender.clearScreen();
        UIRender.renderHeader("Emergency Incident Billing Summary Receipt");
        System.out.println("Status Event Case: Asset Permanently Lost");
        System.out.printf("1. Base Book Replacement Charge (100%% Value): %,.0f VND\n", replacementCost);
        System.out.printf("2. Accumulated Late Processing Fine:          %,.0f VND\n", latePenalty);
        System.out.println("---------------------------------------------------------------------");
        System.out.printf("TOTAL INCIDENT PENALTY SETTLE LIABILITY:     %,.0f VND\n", totalCompoundFine);
        System.out.println("\n[INFO] Inventory metrics updated. Total copies scaled downward.");
        UIRender.pauseEnter();
    }

    // =========================================================================
    // 6. ANALYTICS Dashboard
    // =========================================================================
    private void handleReportMenu() {
        boolean inMenu = true;
        String[] reportOptions = {
                "View Specific Member Borrowing History",
                "View All Transactions (Repayments First)",
                "View Top Active Members List",
                "View Active Overdue Books Report",
                "View Most Popular Books Analytics"
        };

        while (inMenu) {
            UIRender.clearScreen();
            UIRender.renderMenu("Library Performance & Analytics Reports Dashboard", reportOptions);
            int choice = Input.getInt("Select Report Target Index: ");

            switch (choice) {
                case 1:
                    reportManager.viewMemberBorrowingHistory(searchAndSelectMember());
                    UIRender.pauseEnter();
                    break;
                case 2:
                    reportManager.viewAllTransactionsPrioritizingReturns();
                    UIRender.pauseEnter();
                    break;
                case 3:
                    int memLimit = Input.getInt("Enter max member threshold count (0 for default 5): ");
                    reportManager.viewTopBorrowingMembers(memLimit);
                    UIRender.pauseEnter();
                    break;
                case 4:
                    reportManager.viewOverdueBooks();
                    UIRender.pauseEnter();
                    break;
                case 5:
                    int bookLimit = Input.getInt("Enter max popular books threshold count (0 for default 5): ");
                    reportManager.viewMostPopularBooks(bookLimit);
                    UIRender.pauseEnter();
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    UIRender.renderError("Invalid analytics report choice.");
                    UIRender.pauseEnter();
            }
        }
    }

    // =========================================================================
    // PERSISTENT UTILITY PATTERNS
    // =========================================================================
    private Book searchAndSelectBook() {
        String query = Input.getString("Enter Book Search Filter Criteria (Title/ID/Author Query): ");
        List<Book> matches = new ArrayList<>();
        for (Book b : bookManager.getAllBooks()) {
            if (b.getId().toLowerCase().contains(query.toLowerCase()) ||
                    b.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    b.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                matches.add(b);
            }
        }

        if (matches.isEmpty()) {
            UIRender.renderError("No matching records found.");
            UIRender.pauseEnter();
            return null;
        }

        String[] headers = {"Index", "ID Code", "Title Line", "Author String", "Stock Status"};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            Book b = matches.get(i);
            rows.add(new String[] {
                    String.valueOf(i + 1), b.getId(), b.getTitle(), b.getAuthor(),
                    b.getAvailableQuantity() + "/" + b.getTotalQuantity() + " Units"
            });
        }
        UIRender.renderTable(headers, rows);

        int idx = Input.getInt("Select target index line row match item: ") - 1;
        if (idx < 0 || idx >= matches.size()) {
            UIRender.renderError("Index exception context boundary failure.");
            UIRender.pauseEnter();
            return null;
        }
        return matches.get(idx);
    }

    private Member searchAndSelectMember() {
        String query = Input.getString("Enter Member Identity Search Query Parameter (Name/ID): ");
        List<Member> matches = new ArrayList<>();
        for (Member m : memberManager.getAllMembers()) {
            if (m.getId().toLowerCase().contains(query.toLowerCase()) ||
                    m.getName().toLowerCase().contains(query.toLowerCase())) {
                matches.add(m);
            }
        }

        if (matches.isEmpty()) {
            UIRender.renderError("Zero direct roster entries matched.");
            UIRender.pauseEnter();
            return null;
        }

        // Changed Header to include Allocation Limits
        String[] headers = {"Index", "User ID Link", "Legal Profile Name", "Active Loans / Limit"};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            Member m = matches.get(i);
            int activeLoans = countActiveBorrows(m.getId());

            rows.add(new String[] {
                    String.valueOf(i + 1),
                    m.getId(),
                    m.getName(),
                    activeLoans + " / " + m.getBorrowLimit() + " Books" // Displays e.g., "2 / 5 Books"
            });
        }
        UIRender.renderTable(headers, rows);

        int idx = Input.getInt("Select member match target sequence index: ") - 1;
        if (idx < 0 || idx >= matches.size()) {
            UIRender.renderError("Index execution context parameters violation.");
            UIRender.pauseEnter();
            return null;
        }
        return matches.get(idx);
    }

    private void renderBookDatabaseTable(List<Book> dataList) {
        String[] headers = {"ID Key", "Title Name", "Author", "Genre Sub-Class", "Year", "Stock Available", "Total Bound"};
        List<String[]> rowMappings = new ArrayList<>();
        for (Book b : dataList) {
            rowMappings.add(new String[] {
                    b.getId(), b.getTitle(), b.getAuthor(), b.getGenre(),
                    String.valueOf(b.getPublicationYear()), String.valueOf(b.getAvailableQuantity()), String.valueOf(b.getTotalQuantity())
            });
        }
        UIRender.renderTable(headers, rowMappings);
    }

    private void renderMemberDatabaseTable(List<Member> dataList) {
        // Appended limits tracking to the display manifest
        String[] headers = {"User Key", "Legal Identity Name", "Verified Phone", "System Email", "Quota Burden"};
        List<String[]> rowMappings = new ArrayList<>();
        for (Member m : dataList) {
            int activeLoans = countActiveBorrows(m.getId());
            int remainingLimit = m.getBorrowLimit() - activeLoans;

            rowMappings.add(new String[] {
                    m.getId(),
                    m.getName(),
                    m.getPhone(),
                    m.getEmail(),
                    activeLoans + " Out (" + remainingLimit + " Left)" // Displays e.g., "1 Out (4 Left)"
            });
        }
        UIRender.renderTable(headers, rowMappings);
    }

    private int countActiveBorrows(String memberId) {
        int count = 0;
        for (BorrowTransaction tx : txManager.getTransactions()) {
            if (tx.getMemberId().equalsIgnoreCase(memberId) && tx.getReturnDate() == null) count++;
        }
        return count;
    }

    private boolean isBookAlreadyBorrowedByMember(String memberId, String bookId) {
        for (BorrowTransaction tx : txManager.getTransactions()) {
            if (tx.getMemberId().equalsIgnoreCase(memberId) && tx.getBookId().equalsIgnoreCase(bookId) && tx.getReturnDate() == null) return true;
        }
        return false;
    }

    private void seedData() {
        bookManager.addBook(new Book("The Great Gatsby", "F. Scott Fitzgerald", "Classic", 1925, 5));
        bookManager.addBook(new Book("1984", "George Orwell", "Dystopian", 1949, 3));
        memberManager.addMember(new Member("Benn", "0123456789", "benn@uni.edu.vn"));
    }
}