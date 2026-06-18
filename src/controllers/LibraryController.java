package controllers;

import models.Book;
import models.BorrowTransaction;
import models.Member;
import repositories.BookRepository;
import repositories.BorrowTransactionRepository;
import repositories.MemberRepository;
import services.BookService;
import services.BorrowTransactionService;
import services.MemberService;
import services.ReportService;
import utilities.InputController;
import utilities.UIRender;

import java.time.LocalDate;
import java.util.List;

public class LibraryController {
    private final BookRepository bookRepository = new BookRepository();
    private final MemberRepository memberRepository = new MemberRepository();
    private final BorrowTransactionRepository txRepository = new BorrowTransactionRepository();

    private final BookService bookService = new BookService(bookRepository);
    private final MemberService memberService = new MemberService(memberRepository);
    private final BorrowTransactionService transactionService = new BorrowTransactionService(txRepository);
    private final ReportService reportService = new ReportService(bookRepository, transactionService);

    public LibraryController() {
        seedInitialDatabaseData();
    }

    public void start() {
        boolean executing = true;
        String[] modules = {
                "Book Management System", "Member Management System", "Borrow Circulation Center",
                "Return Processing Center", "Report Lost Book Asset", "Analytics & Reports Dashboard"
        };

        while (executing) {
            UIRender.clearScreen();
            UIRender.renderMenu("Library Administration Framework Core", modules);
            int selection = InputController.getChoice("Select component option: ", 6);
            switch (selection) {
                case 1: handleBookSubsystem(); break;
                case 2: handleMemberSubsystem(); break;
                case 3: handleBorrowWorkflow(); break;
                case 4: handleReturnWorkflow(); break;
                case 5: handleLostBookEmergency(); break;
                case 6: handleAnalyticsDashboard(); break;
                case 0: executing = false; break;
            }
        }
    }

    private void handleBookSubsystem() {
        boolean active = true;
        String[] choices = { "Add Asset Book", "Remove Asset Book", "Update Book Details", "List Book Inventory" };

        while (active) {
            UIRender.clearScreen();
            UIRender.renderMenu("Book Inventory Sub-Registry", choices);
            int processId = InputController.getChoice("Select action index: ", 4);

            switch (processId) {
                case 1:
                    UIRender.clearScreen();
                    if (bookService.registerBook(InputController.inputNewBookData()))
                        UIRender.renderSuccess("Book registered safely.");
                    else UIRender.renderError("Registration failed due to duplicate entry.");
                    UIRender.pauseEnter();
                    break;
                case 2:
                    UIRender.clearScreen();
                    Book targetDelete = BookService.searchAndSelectBook(bookRepository);
                    if (targetDelete == null) break;
                    if (targetDelete.getAvailableQuantity() != targetDelete.getTotalQuantity()) {
                        UIRender.renderError("Purge Lock Triggered: Active loans outstanding.");
                    } else {
                        bookRepository.delete(targetDelete);
                        UIRender.renderSuccess("Asset record purged cleanly.");
                    }
                    UIRender.pauseEnter();
                    break;
                case 3:
                    UIRender.clearScreen();
                    Book oldBook = BookService.searchAndSelectBook(bookRepository);
                    if (oldBook == null) break;

                    Book newBook = InputController.updateBookModelFields(oldBook);
                    if (bookService.updateBook(oldBook, newBook)) {
                        UIRender.renderSuccess("Asset modifications mapped and synchronized safely.");
                    } else {
                        UIRender.renderError("Update rejected: Capacity constraints bound failure.");
                    }
                    UIRender.pauseEnter();
                    break;
                case 4:
                    UIRender.clearScreen();
                    UIRender.renderBookContentTable(bookService.getAllBooks());
                    UIRender.pauseEnter();
                    break;
                case 0: active = false; break;
            }
        }
    }

    private void handleMemberSubsystem() {
        boolean active = true;
        String[] options = { "Register New Member", "Revoke Member Account", "Modify Member Details", "List Directory Roster" };

        while (active) {
            UIRender.clearScreen();
            UIRender.renderMenu("Member Registry Sub-Framework", options);
            int choice = InputController.getChoice("Select action index: ", 4);

            switch (choice) {
                case 1:
                    UIRender.clearScreen();
                    if (memberService.registerMember(InputController.inputNewMemberData()))
                        UIRender.renderSuccess("Member profile instantiated.");
                    else UIRender.renderError("Profile creation collapsed.");
                    UIRender.pauseEnter();
                    break;
                case 2:
                    UIRender.clearScreen();
                    Member targetDelete = MemberService.searchAndSelectMember(memberRepository);
                    if (targetDelete == null) break;
                    if (transactionService.countActiveLoans(targetDelete.getId()) > 0) {
                        UIRender.renderError("Revocation Denied: Outstanding open loans exist.");
                    } else {
                        memberRepository.delete(targetDelete);
                        UIRender.renderSuccess("User account card revoked cleanly.");
                    }
                    UIRender.pauseEnter();
                    break;
                case 3:
                    UIRender.clearScreen();
                    Member targetUpdate = MemberService.searchAndSelectMember(memberRepository);
                    if (targetUpdate != null) {
                        Member newMember = InputController.updateMemberModelFields(targetUpdate);
                        if (memberService.updateMember(targetUpdate, newMember)) {
                            UIRender.renderSuccess("Profile modifications saved and synced.");
                        }
                    }
                    UIRender.pauseEnter();
                    break;
                case 4:
                    UIRender.clearScreen();
                    UIRender.renderMemberContentTable(memberService.getAllMembers());
                    UIRender.pauseEnter();
                    break;
                case 0: active = false; break;
            }
        }
    }

    private void handleBorrowWorkflow() {
        UIRender.clearScreen();
        Member member = MemberService.searchAndSelectMember(memberRepository);
        if (member == null) return;

        Book book = BookService.searchAndSelectBook(bookRepository);
        if (book == null) return;

        BorrowTransaction tx = new BorrowTransaction(member.getId(), book.getId(), LocalDate.now());

        if (transactionService.executeCheckout(member, book, tx) ) {
            UIRender.renderSuccess("Circulation loan ledger updated and verified.");
        } else {
            UIRender.renderError("Checkout Rejected: Policy violation, stock empty, or quota reached.");
        }
        UIRender.pauseEnter();
    }

    private void handleReturnWorkflow() {
        UIRender.clearScreen();
        Member member = MemberService.searchAndSelectMember(memberRepository);
        if (member == null) return;

        List<BorrowTransaction> activeLoans = transactionService.getActiveLoansByMember(member.getId());
        if (activeLoans.isEmpty()) {
            UIRender.renderError("No outstanding open balances found.");
            UIRender.pauseEnter();
            return;
        }

        UIRender.renderTransactionTable(activeLoans);
        BorrowTransaction targetTx = InputController.selectTransactionFromList(activeLoans);
        if (targetTx == null) return;

        Book book = bookService.getBookById(targetTx.getBookId());

        double fines = transactionService.processReturn(targetTx, member, book, LocalDate.now());

        if (fines == -1.0) {
            UIRender.renderError("State Violation: Transaction is locked or not currently active.");
        } else if (fines > 0) {
            UIRender.renderFineStatement(0.0, fines, fines);
        } else {
            UIRender.renderSuccess("Asset returned on schedule. Liability balance cleared.");
        }
        UIRender.pauseEnter();
    }

    // 3. ĐỒNG BỘ: THAY THẾ LOGIC REPORT MISSING (REPORT_MISSING)
    private void handleLostBookEmergency() {
        UIRender.clearScreen();
        Member member = MemberService.searchAndSelectMember(memberRepository);
        if (member == null) return;

        List<BorrowTransaction> activeLoans = transactionService.getActiveLoansByMember(member.getId());
        if (activeLoans.isEmpty()) {
            UIRender.renderError("Possesses zero outstanding active open accounts.");
            UIRender.pauseEnter();
            return;
        }

        UIRender.renderTransactionTable(activeLoans);
        BorrowTransaction targetTx = InputController.selectTransactionFromList(activeLoans);
        if (targetTx == null) return;

        Book book = bookService.getBookById(targetTx.getBookId());
        double customCost = InputController.getValidReplacementCost();

        // ĐỒNG BỘ: Gọi Service reportMissing (thay thế cho processLostBook cũ)
        double[] receiptInvoice = transactionService.reportMissing(targetTx, member, book, customCost);

        if (receiptInvoice == null) {
            UIRender.renderError("State Violation: Process aborted. Asset must be currently out on loan.");
        } else {
            UIRender.clearScreen();
            UIRender.renderFineStatement(receiptInvoice[0], receiptInvoice[1], receiptInvoice[2]);
        }
        UIRender.pauseEnter();
    }

    private void handleAnalyticsDashboard() {
        boolean status = true;
        String[] menu = {
                "View Specific Member Borrowing History", "View All Transactions Log",
                "View Active Overdue Books Report", "View Most Popular Books Analytics"
        };

        while (status) {
            UIRender.clearScreen();
            UIRender.renderMenu("Library Performance Analytics Center", menu);
            int selection = InputController.getChoice("Select Analytics Action Matrix Item: ", 4);
            switch (selection) {
                case 1:
                    Member m = MemberService.searchAndSelectMember(memberRepository);
                    if (m != null) UIRender.renderTransactionTable(reportService.getSortedMemberHistory(m));
                    UIRender.pauseEnter();
                    break;
                case 2:
                    UIRender.renderTransactionTable(reportService.getMasterLogSorted());
                    UIRender.pauseEnter();
                    break;
                case 3:
                    UIRender.renderTransactionTable(reportService.getOverdueAssets());
                    UIRender.pauseEnter();
                    break;
                case 4:
                    int cap = InputController.getInt("Enter popularity threshold display cap count: ");
                    UIRender.renderBookContentTable(reportService.getPopularBooks(cap));
                    UIRender.pauseEnter();
                    break;
                case 0: status = false; break;
            }
        }
    }

    private void seedInitialDatabaseData() {
        bookRepository.save(new Book("The Great Gatsby", "F. Scott Fitzgerald", "Classic", 1925, 5));
        bookRepository.save(new Book("1984", "George Orwell", "Dystopian", 1949, 3));
        bookRepository.save(new Book("How to get 10 mark in PRO_192", "FPT Instructor", "Academic Trick", 2024, 3));
        memberRepository.save(new Member("Benn", "0123456789", "benn@uni.edu.vn"));
        memberRepository.save(new Member("Nguyen Van Binh", "0373450305", "nguyenvanbinh542007@gmail.com"));
    }
}