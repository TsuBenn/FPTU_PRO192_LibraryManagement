package controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import managers.BookManager;
import managers.MemberManager;
import managers.BorrowTransactionManager;
import models.Book;
import models.Member;
import models.BorrowTransaction;
import utilities.Input;
import utilities.UIRender;
import utilities.Validator;
import utilities.IDManager;

public class LibraryController {
    private BookManager bookManager;
    private MemberManager memberManager;
    private BorrowTransactionManager txManager;

    public LibraryController() {
        this.bookManager = new BookManager();
        this.memberManager = new MemberManager();
        this.txManager = new BorrowTransactionManager(bookManager, memberManager);
        seedData();
    }

    public void start() {
        boolean running = true;
        String[] mainOptions = {
            "Book Management System",
            "Member Management System",
            "Borrow Circulation Center",
            "Return Processing Center"
        };

        while (running) {
            UIRender.clearScreen();
            UIRender.renderMenu("Library Core Main Framework", mainOptions);

            int choice = Input.getInt("Select an option: ");
            switch (choice) {
                case 1: handleBookMenu(); break;
                case 2: handleMemberMenu(); break;
                case 3: handleBorrowWorkflow(); break;
                case 4: handleReturnWorkflow(); break;
                case 0: running = false; break;
                default: 
                UIRender.renderError("Invalid option choice selection profile!");
                UIRender.pauseEnter();
            }
        }
    }

    // ==========================================
    // 1. BOOK MANAGEMENT SYSTEM PIPELINE (CRUD)
    // ==========================================
    private void handleBookMenu() {
        boolean inMenu = true;
        String[] bookOptions = { "Add Asset Book", "Remove Asset Book", "Update Book Details", "List Book Inventory" };

        while (inMenu) {
            UIRender.clearScreen();
            UIRender.renderMenu("Book Management Sub-Registry", bookOptions);
            int choice = Input.getInt("Select Action Index: ");

            if (choice == 1) { // ADD
                UIRender.clearScreen();
                UIRender.renderHeader("Inventory Creation Workspace");
                String id = IDManager.generateBookID();
                System.out.println("Auto Generated Track Key ID: " + id);

                String title = promptNonEmptyString("Enter Title: ");
                String author = promptNonEmptyString("Enter Author: ");
                String genre = promptNonEmptyString("Enter Genre: ");
                int year = Input.getInt("Enter Publication Year (YYYY): ");
                int qty = Input.getInt("Enter Total Inventory Quantity: ");

                if (bookManager.isDuplicateBook(title, author)) {
                    UIRender.renderError("Operation Aborted: An identical title by this author already exists.");
                    UIRender.pauseEnter();
                    continue;
                }

                Book book = new Book(id, title, author, genre, year, qty);
                if (bookManager.addBook(book)) {
                    UIRender.renderSuccess("Book created and recorded in system registries!");
                } else {
                    UIRender.renderError("Database engine rejected structural storage.");
                }
                UIRender.pauseEnter();

            } else if (choice == 2) { // REMOVE
                UIRender.clearScreen();
                UIRender.renderHeader("Inventory Purge Selection Module");
                Book b = searchAndSelectBook();
                if (b == null) continue;

                if (txManager.isBookCurrentlyBorrowed(b.getId())) {
                    UIRender.renderError("Purge Lock Triggered: This book asset has unresolved open loans active!");
                } else {
                    bookManager.removeBook(b.getId());
                    UIRender.renderSuccess("Asset record removed cleanly from underlying maps.");
                }
                UIRender.pauseEnter();

            } else if (choice == 3) { // UPDATE
                UIRender.clearScreen();
                UIRender.renderHeader("Asset Modification Workspace");
                Book b = searchAndSelectBook();
                if (b == null) continue;

                System.out.println("\n[INFO] Press ENTER without typing to skip field alteration & preserve values.\n");
                String t = Input.getString("Update Title [" + b.getTitle() + "]: ");
                String a = Input.getString("Update Author [" + b.getAuthor() + "]: ");
                String g = Input.getString("Update Genre [" + b.getGenre() + "]: ");

                String yrRaw = Input.getString("Update Publication Year [" + b.getPublicationYear() + "]: ");
                String qtyRaw = Input.getString("Update Quantity [" + b.getTotalQuantity() + "]: ");

                // Conditional processing assignments: empty string skips mutation pass
                if (!t.isEmpty()) b.setTitle(t);
                if (!a.isEmpty()) b.setAuthor(a);
                if (!g.isEmpty()) b.setGenre(g);
                if (!yrRaw.isEmpty() && Validator.isValidInt(yrRaw)) b.setPublicationYear(Integer.parseInt(yrRaw));
                if (!qtyRaw.isEmpty() && Validator.isValidInt(qtyRaw)) b.setTotalQuantity(Integer.parseInt(qtyRaw));

                UIRender.renderSuccess("Asset state vector updated successfully!");
                UIRender.pauseEnter();

            } else if (choice == 4) { // LIST ALL
                UIRender.clearScreen();
                UIRender.renderHeader("Full Library Asset Manifest");
                renderBookDatabaseTable(bookManager.getAllBooks());
                UIRender.pauseEnter();

            } else if (choice == 0) {
                inMenu = false;
            }
        }
    }

    // ==========================================
    // 2. MEMBER MANAGEMENT SYSTEM PIPELINE (CRUD)
    // ==========================================
    private void handleMemberMenu() {
        boolean inMenu = true;
        String[] memberOptions = { "Register New Member", "Revoke Member Account", "Modify Member Details", "List Directory Roster" };

        while (inMenu) {
            UIRender.clearScreen();
            UIRender.renderMenu("Member Registry Sub-Framework", memberOptions);
            int choice = Input.getInt("Select Action Index: ");

            if (choice == 1) { // ADD
                UIRender.clearScreen();
                UIRender.renderHeader("Account Registration Terminal");
                String id = IDManager.generateMemberID();
                System.out.println("Auto Generated Roster Key ID: " + id);

                String name = promptNonEmptyString("Enter Full Legal Name: ");

                String phone;
                while (true) {
                    phone = promptNonEmptyString("Enter Unique Contact Phone (10 digits): ");
                    if (!Validator.isValidPhone(phone)) {
                        UIRender.renderError("Regex Format Fault: Must be exactly 10 raw digits.");
                        continue;
                    }
                    break;
                }

                String email;
                while (true) {
                    email = promptNonEmptyString("Enter Unique Email Domain Address: ");
                    if (!Validator.isValidEmail(email)) {
                        UIRender.renderError("Regex Format Fault: Invalid email criteria framework match.");
                        continue;
                    }
                    break;
                }

                if (memberManager.isContactDuplicate(phone, email)) {
                    UIRender.renderError("Registration Aborted: Uniqueness conflict on contact phone or email data variables.");
                    UIRender.pauseEnter();
                    continue;
                }

                Member m = new Member(id, name, phone, email);
                if (memberManager.addMember(m)) {
                    UIRender.renderSuccess("Member database profile committed!");
                } else {
                    UIRender.renderError("Database engine rejected user mapping registration.");
                }
                UIRender.pauseEnter();

            } else if (choice == 2) { // REMOVE
                UIRender.clearScreen();
                UIRender.renderHeader("Account Revocation Module");
                Member m = searchAndSelectMember();
                if (m == null) continue;

                if (txManager.isMemberCurrentlyBorrowing(m.getId())) {
                    UIRender.renderError("Revocation Lock Triggered: Account has outstanding books checked out!");
                } else {
                    memberManager.removeMember(m.getId());
                    UIRender.renderSuccess("User account card revoked and purged.");
                }
                UIRender.pauseEnter();

            } else if (choice == 3) { // UPDATE
                UIRender.clearScreen();
                UIRender.renderHeader("Profile Modification Workspace");
                Member m = searchAndSelectMember();
                if (m == null) continue;

                System.out.println("\n[INFO] Press ENTER without typing to skip field alteration & preserve values.\n");
                String n = Input.getString("Update Name [" + m.getName() + "]: ");
                String p = Input.getString("Update Phone [" + m.getPhone() + "]: ");
                String e = Input.getString("Update Email [" + m.getEmail() + "]: ");

                if (!n.isEmpty()) m.setName(n);
                if (!p.isEmpty() && Validator.isValidPhone(p)) m.setPhone(p);
                if (!e.isEmpty() && Validator.isValidEmail(e)) m.setEmail(e);

                UIRender.renderSuccess("Profile state values mutated successfully!");
                UIRender.pauseEnter();

            } else if (choice == 4) { // LIST ALL
                UIRender.clearScreen();
                UIRender.renderHeader("Registered Library Membership Directory");
                renderMemberDatabaseTable(memberManager.getAllMembers());
                UIRender.pauseEnter();

            } else if (choice == 0) {
                inMenu = false;
            }
        }
    }

    // ==========================================
    // 3. CIRCULATION LOAN SYSTEM WORKFLOW (BORROW)
    // ==========================================
    private void handleBorrowWorkflow() {
        boolean inMenu = true;
        String[] borrowOptions = { "Create Borrow Transaction", "List All Circulation Ledgers" };

        while (inMenu) {
            UIRender.clearScreen();
            UIRender.renderMenu("Borrow Circulation Sub-Framework", borrowOptions);
            int choice = Input.getInt("Select Action Index: ");

            if (choice == 1) {
                executeBorrowTransaction();
            } else if (choice == 2) {
                UIRender.clearScreen();
                UIRender.renderHeader("Master Circulation Transaction Ledger");

                List<BorrowTransaction> allTx = txManager.getTransactions();
                if (allTx.isEmpty()) {
                    UIRender.renderError("No transaction history discovered in system ledger entries.");
                    UIRender.pauseEnter();
                    continue;
                }

                String[] headers = {"Tx ID", "Member Name", "Book Title", "Borrow Date", "Due Date", "Status / Fine"};
                List<String[]> tableRows = new ArrayList<>();

                for (BorrowTransaction tx : allTx) {
                    // Resolve human-readable entities from raw tracking string keys
                    Member m = memberManager.findMemberById(tx.getMemberId());
                    Book b = bookManager.findBookById(tx.getBookId());

                    String memberName = (m != null) ? m.getName() : "Unknown Member";
                    String bookTitle = (b != null) ? b.getTitle() : "Unknown Asset";

                    // Determine status vector display attributes
                    String status;
                    if (tx.getReturnDate() != null) {
                        status = "RETURNED (Fine Paid: " + String.format("%,.0f", tx.getFinePaid()) + " VND)";
                    } else {
                        // Check if it's currently overdue relative to today's date
                        if (LocalDate.now().isAfter(tx.getDueDate())) {
                            status = "OVERDUE !!";
                        } else {
                            status = "ACTIVE / OUTSTANDING";
                        }
                    }

                    tableRows.add(new String[] {
                        tx.getTransactionId(),
                        memberName,
                        bookTitle,
                        tx.getBorrowDate().toString(),
                        tx.getDueDate().toString(),
                        status
                    });
                }

                UIRender.renderTable(headers, tableRows);
                UIRender.pauseEnter();
            } else if (choice == 0) {
                inMenu = false;
            }
        }
    }

    // Helper extracting the core checkout logic to keep the sub-menu layout highly readable
    private void executeBorrowTransaction() {
        UIRender.clearScreen();
        UIRender.renderHeader("Circulation Terminal: Identify Loanee");
        Member member = searchAndSelectMember();
        if (member == null) return;

        System.out.println("\n");
        UIRender.renderHeader("Circulation Terminal: Identify Target Book Resource");
        Book book = searchAndSelectBook();
        if (book == null) return;

        // Validation safeguards
        if (book.getAvailableQuantity() <= 0) {
            UIRender.renderError("Loan Rejected: Stock Exhausted. All copies of this asset are out.");
            UIRender.pauseEnter();
            return;
        }
        if (txManager.countActiveBorrows(member.getId()) >= member.getBorrowLimit()) {
            UIRender.renderError("Loan Rejected: User profile allocation boundary threshold exceeded (Max 3).");
            UIRender.pauseEnter();
            return;
        }
        if (txManager.isBookAlreadyBorrowedByMember(member.getId(), book.getId())) {
            UIRender.renderError("Loan Rejected: Multi-instance restriction. Member holds an open copy of this asset.");
            UIRender.pauseEnter();
            return;
        }

        System.out.println("\n");
        System.out.println("Processing System Chronology Configuration Initialization:");
        String borrowPrompt = "Provide Check-out Anchor Timestamp [Leave empty for System Clock Today]:";
        System.out.println(borrowPrompt);

        String dayCheck = Input.getString("Enter day (DD) or hit enter: ");
        LocalDate borrowDate;

        if (dayCheck.isEmpty()) {
            borrowDate = LocalDate.now();
            System.out.println("No parameters received. Resolved clock assignment: " + borrowDate);
        } else {
            if (!Validator.isValidInt(dayCheck)) {
                UIRender.renderError("Invalid day variable template format input.");
                UIRender.pauseEnter();
                return;
            }
            int d = Integer.parseInt(dayCheck);
            int m = Input.getInt("Enter month (MM): ");
            int y = Input.getInt("Enter year (YYYY): ");
            if (!Validator.isValidDate(d, m, y)) {
                UIRender.renderError("Calendar logic verification crash. Non-existent parameters profile.");
                UIRender.pauseEnter();
                return;
            }
            borrowDate = LocalDate.of(y, m, d);
        }

        System.out.println("\nLoan Duration Criteria Processing Configuration Matrix:");
        System.out.println("  [1] Assign specific concrete calendar deadline date");
        System.out.println("  [2] Inject relative lifespan tracking days allowance metrics");
        int dueChoice = Input.getInt("Select execution option: ");
        LocalDate dueDate = null;

        if (dueChoice == 1) {
            System.out.println("\n");
            dueDate = Input.getDate("--- Input Targeted Return Date Cap Limits ---");
        } else if (dueChoice == 2) {
            int durationDays = Input.getInt("Enter allowed circulation days: ");
            if (durationDays <= 0) {
                UIRender.renderError("Boundary violation rules. Lifespan must match real progressive timeline vectors.");
                UIRender.pauseEnter();
                return;
            }
            dueDate = borrowDate.plusDays(durationDays);
        } else {
            UIRender.renderError("Invalid process strategy blueprint configuration fallback.");
            UIRender.pauseEnter();
            return;
        }

        if (dueDate.isBefore(borrowDate)) {
            UIRender.renderError("Chronological Matrix Error: Deadline limit cannot sit prior to checkout timestamps.");
            UIRender.pauseEnter();
            return;
        }

        String txId = IDManager.generateTransactionID();
        if (txManager.borrowBook(txId, member.getId(), book.getId(), borrowDate, dueDate)) {
            UIRender.renderSuccess("Circulation sequence secured. Allocation Record ID: " + txId);
        } else {
            UIRender.renderError("Core operation transaction engine failure initialization parameters.");
        }
        UIRender.pauseEnter();
    }

    // ==========================================
    // 4. CIRCULATION RETURN PIPELINE WORKFLOW
    // ==========================================
    private void handleReturnWorkflow() {
        UIRender.clearScreen();
        UIRender.renderHeader("Return System Terminal: Identify Returning Member");
        Member member = searchAndSelectMember();
        if (member == null) return;

        System.out.println("\n");
        UIRender.renderHeader("Outstanding Open Loans Log Checklist for: " + member.getName());
        List<BorrowTransaction> activeLoans = txManager.getActiveTransactionsByMember(member.getId());

        if (activeLoans.isEmpty()) {
            UIRender.renderError("No outstanding active structural ledger accounts found matched to user.");
            UIRender.pauseEnter();
            return;
        }

        String[] loanHeaders = {"Index", "Asset Key", "Resource Book Title", "Borrow Anchor Date", "Allocated Due Date"};
        List<String[]> loanTable = new ArrayList<>();
        for (int i = 0; i < activeLoans.size(); i++) {
            BorrowTransaction tx = activeLoans.get(i);
            Book book = bookManager.findBookById(tx.getBookId());
            String title = (book != null) ? book.getTitle() : "Asset Decoupled From Registry Store Maps";

            loanTable.add(new String[] {
                String.valueOf(i + 1),
                tx.getBookId(),
                title,
                tx.getBorrowDate().toString(),
                tx.getDueDate().toString()
            });
        }
        UIRender.renderTable(loanHeaders, loanTable);

        int lIdx = Input.getInt("Select ledger target sequence row index to reconcile: ") - 1;
        if (lIdx < 0 || lIdx >= activeLoans.size()) return;
        BorrowTransaction targetTx = activeLoans.get(lIdx);

        System.out.println("\n");
        UIRender.renderHeader("Processing Return Asset Mapping Node Verification");
        LocalDate borrowDate = targetTx.getBorrowDate();
        LocalDate returnDate = null;

        System.out.println("Reconciliation Date Verification Execution Strategies:");
        System.out.println("  [1] Pull live active system clock timestamp configuration [TODAY]");
        System.out.println("  [2] Declare explicit specific manual calendar calendar entry");
        System.out.println("  [3] Input duration relative days tracking value from loan anchor point");
        int retStrategy = Input.getInt("Strategy configuration code profile: ");

        if (retStrategy == 1) {
            returnDate = LocalDate.now();
            System.out.println("Clock tracking verified automatically. Assignment: " + returnDate);
        } else if (retStrategy == 2) {
            System.out.println("\n");
            returnDate = Input.getDate("--- Enter Concrete Actual Return Timestamp ---");
        } else if (retStrategy == 3) {
            System.out.println("\nAsset checked out tracking anchor date was: " + borrowDate);
            int daysKept = Input.getInt("Enter total progressive elapsed tracking days retained: ");
            if (daysKept < 0) {
                UIRender.renderError("Timeline metrics vectors constraint boundary parameters fault.");
                UIRender.pauseEnter();
                return;
            }
            returnDate = borrowDate.plusDays(daysKept);
        } else {
            UIRender.renderError("Invalid configuration profile processing matrix selected path.");
            UIRender.pauseEnter();
            return;
        }

        if (returnDate.isBefore(borrowDate)) {
            UIRender.renderError("Chronological Paradox Error: Return execution cannot postdate historical emergence moments.");
            UIRender.pauseEnter();
            return;
        }

        double fine = txManager.returnBook(member.getId(), targetTx.getBookId(), returnDate);
        if (fine > 0) {
            UIRender.renderSuccess("Asset reclaimed successfully. Late status pipeline execution triggered!");
            System.out.printf(">>> DISCOVERED PENALTY ACCOUNT BALANCE OWED: %,.0f VND\n", fine);
        } else {
            UIRender.renderSuccess("Asset tracking vector restored on-schedule. Zero account fee liability metrics.");
        }
        UIRender.pauseEnter();
    }

    // ==========================================
    // SELECTION LAYER HELPER UTILITIES (TUI REFACTOR)
    // ==========================================
    private Book searchAndSelectBook() {
        String q = Input.getString("Enter Book Title, ID, or Author Query: ");
        List<Book> matches = bookManager.searchBooks(q);
        if (matches.isEmpty()) {
            UIRender.renderError("No catalog match records discovered filtering with argument criteria.");
            UIRender.pauseEnter();
            return null;
        }

        String[] headers = {"Index", "ID Code", "Title Line", "Author Reference", "Stock Status"};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            Book b = matches.get(i);
            rows.add(new String[] {
                String.valueOf(i + 1), b.getId(), b.getTitle(), b.getAuthor(),
                b.getAvailableQuantity() + "/" + b.getTotalQuantity() + " Units"
            });
        }
        UIRender.renderTable(headers, rows);

        int idx = Input.getInt("Select book collection index row target line: ") - 1;
        if (idx < 0 || idx >= matches.size()) {
            UIRender.renderError("Index execution context boundary violation.");
            UIRender.pauseEnter();
            return null;
        }
        return matches.get(idx);
    }

    private Member searchAndSelectMember() {
        String q = Input.getString("Enter Member Identity Search Query (Name/ID): ");
        List<Member> matches = memberManager.searchMembers(q);
        if (matches.isEmpty()) {
            UIRender.renderError("Zero direct roster matches verified targeting current parameter conditions.");
            UIRender.pauseEnter();
            return null;
        }

        String[] headers = {"Index", "User ID", "Legal Member Label Name", "Configured Phone Tracker"};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            Member m = matches.get(i);
            rows.add(new String[] { String.valueOf(i + 1), m.getId(), m.getName(), m.getPhone() });
        }
        UIRender.renderTable(headers, rows);

        int idx = Input.getInt("Select member identity row tracking layout index: ") - 1;
        if (idx < 0 || idx >= matches.size()) {
            UIRender.renderError("Index calculation context parameters violation array space boundaries.");
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
        String[] headers = {"User Key", "Legal Identity Name", "Verified Phone Contact Link", "System Domain Email"};
        List<String[]> rowMappings = new ArrayList<>();
        for (Member m : dataList) {
            rowMappings.add(new String[] { m.getId(), m.getName(), m.getPhone(), m.getEmail() });
        }
        UIRender.renderTable(headers, rowMappings);
    }

    private String promptNonEmptyString(String outputPromptMessage) {
        while (true) {
            String readBuffer = Input.getString(outputPromptMessage);
            if (readBuffer.isEmpty()) {
                UIRender.renderError("Constraint Violation Exception: Entry field variables cannot remain empty.");
                continue;
            }
            return readBuffer;
        }
    }

    private void seedData() {
        bookManager.addBook(new Book(IDManager.generateBookID(), "The Great Gatsby", "F. Scott Fitzgerald", "Classic", 1925, 5));
        bookManager.addBook(new Book(IDManager.generateBookID(), "1984", "George Orwell", "Dystopian", 1949, 3));
        memberManager.addMember(new Member(IDManager.generateMemberID(), "Benn", "0123456789", "benn@uni.edu.vn"));
    }
}
