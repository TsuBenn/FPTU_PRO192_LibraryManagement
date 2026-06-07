package managers;

import models.Book;
import models.BorrowTransaction;
import models.Member;
import utilities.UIRender;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportManager {
    private final BookManager bookManager;
    private final MemberManager memberManager;
    private final BorrowTransactionManager borrowTransactionManager;

    private static final Comparator<BorrowTransaction> NEWEST_BORROW_TRANSCATION_COMPARATOR =
            (a, b) -> {
                LocalDate returnA = a.getReturnDate();
                LocalDate returnB = b.getReturnDate();

                if (returnA != null && returnB != null) {
                    int cmp = returnB.compareTo(returnA);
                    if (cmp != 0) {
                        return cmp;
                    }
                } else if (returnA != null) {
                    return -1;
                } else if (returnB != null) {
                    return 1;
                }

                return b.getBorrowDate().compareTo(a.getBorrowDate());
            };

    public ReportManager(BookManager bookManager, MemberManager memberManager, BorrowTransactionManager borrowTransactionManager) {
        this.bookManager = bookManager;
        this.memberManager = memberManager;
        this.borrowTransactionManager = borrowTransactionManager;
    }

    public void viewMemberBorrowingHistory(Member member) {
        if (member == null) {
            UIRender.renderError("Don't have any member to display borrow history!");
            return;
        }

        // Retrieves all transaction records associated with this specific member's unique ID key signature
        List<BorrowTransaction> memberTransactions = borrowTransactionManager.getTransactions()
                .stream()
                .filter(tx -> tx.getMemberId().equalsIgnoreCase(member.getId()))
                .sorted(NEWEST_BORROW_TRANSCATION_COMPARATOR)
                .collect(Collectors.toList());

        if (memberTransactions.isEmpty()) {
            System.out.println("\nNo recorded borrowing actions found registered to account card: " + member.getName());
            return;
        }

        UIRender.renderHeader("Historical Borrow Tracking Ledger for: " + member.getName());
        String[] headers = {"Tx ID", "Asset Book Title", "Borrow Date", "Due Date", "Status State / Fine Settlement"};
        List<String[]> rows = new ArrayList<>();

        for (BorrowTransaction tx : memberTransactions) {
            Book book = bookManager.findBookById(tx.getBookId());
            String title = (book != null) ? book.getTitle() : "Asset Record Detached From Active Registries";

            String status;
            if (tx.getReturnDate() != null) {
                status = "Completed on " + tx.getReturnDate() + " (Paid: " + String.format("%,.0f", tx.getFinePaid()) + " VND)";
            } else {
                double liveFine = borrowTransactionManager.calculateCurrentFine(tx);
                status = (liveFine > 0) ? "OVERDUE (Accruing: " + String.format("%,.0f", liveFine) + " VND)" : "Active / Out on Loan";
            }

            rows.add(new String[]{
                    tx.getTransactionId(),
                    title,
                    tx.getBorrowDate().toString(),
                    tx.getDueDate().toString(),
                    status
            });
        }
        UIRender.renderTable(headers, rows);
    }

    public void viewAllTransactionsPrioritizingReturns() {
        List<BorrowTransaction> borrowTransactions = borrowTransactionManager.getTransactions()
                .stream()
                .sorted(NEWEST_BORROW_TRANSCATION_COMPARATOR)
                .collect(Collectors.toList());

        if (borrowTransactions.isEmpty()) {
            UIRender.renderError("The master library transaction log data matrix is currently empty.");
            return;
        }

        UIRender.renderHeader("Master Transaction Log Ledger (Settled First)");
        String[] headers = {"Tx ID", "Member Name", "Book Title", "Borrow Date", "Return Date", "Fine Summary Status"};
        List<String[]> rows = new ArrayList<>();

        for (BorrowTransaction tx : borrowTransactions) {
            Member member = memberManager.findMemberById(tx.getMemberId());
            Book book = bookManager.findBookById(tx.getBookId());

            String borrowerName = (member != null) ? member.getName() : "Purged Account Profile Link";
            String bookTitle = (book != null) ? book.getTitle() : "Asset Record Detached";

            String returnDateStr;
            String fineStr;
            if (tx.getReturnDate() != null) {
                returnDateStr = tx.getReturnDate().toString();
                fineStr = String.format("%,.0f VND [LOCKED]", tx.getFinePaid());
            } else {
                returnDateStr = "OUT ON LOAN";
                double currentFine = borrowTransactionManager.calculateCurrentFine(tx);
                fineStr = (currentFine > 0) ? String.format("%,.0f VND [ACCRUING]", currentFine) : "0 VND";
            }

            rows.add(new String[]{
                    tx.getTransactionId(),
                    borrowerName,
                    bookTitle,
                    tx.getBorrowDate().toString(),
                    returnDateStr,
                    fineStr
            });
        }
        UIRender.renderTable(headers, rows);
    }

    public void viewTopBorrowingMembers(int memLimit) {
        if (memLimit <= 0) memLimit = 5;

        List<Map.Entry<String, Long>> result = borrowTransactionManager.getTransactions()
                .stream()
                .collect(Collectors.groupingBy(BorrowTransaction::getMemberId, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(memLimit)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            UIRender.renderError("No transaction logs exist to compile active membership analytics.");
            return;
        }

        UIRender.renderHeader("Top " + memLimit + " Most Active Library Members");
        String[] headers = {"Rank Index", "Member ID Code", "Legal Identity Name", "Total Strategic Borrowings"};
        List<String[]> rows = new ArrayList<>();

        int rank = 1;
        for (Map.Entry<String, Long> entry : result) {
            Member member = memberManager.findMemberById(entry.getKey());
            String memberName = (member != null) ? member.getName() : "Purged Account Card Link";

            rows.add(new String[]{
                    String.valueOf(rank++),
                    entry.getKey(),
                    memberName,
                    entry.getValue() + " Times Checked Out"
            });
        }
        UIRender.renderTable(headers, rows);
    }

    public void viewMostPopularBooks(int bookLimit) {
        if (bookLimit <= 0) bookLimit = 5;

        // Group transaction collections by unique internal Book ID tracking keys
        List<Map.Entry<String, Long>> result = borrowTransactionManager.getTransactions()
                .stream()
                .collect(Collectors.groupingBy(BorrowTransaction::getBookId, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(bookLimit)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            UIRender.renderError("No circulation activity records exist to compile popular asset metrics.");
            return;
        }

        UIRender.renderHeader("Top " + bookLimit + " Most Popular Library Resource Books");
        String[] headers = {"Rank Index", "Book ID Code", "Inventory Book Title Line", "Author Reference", "Frequency Rank"};
        List<String[]> rows = new ArrayList<>();

        int rank = 1;
        for (Map.Entry<String, Long> entry : result) {
            Book book = bookManager.findBookById(entry.getKey());
            if (book == null) continue; // Skips formatting overhead if resource metadata has been completely detached

            rows.add(new String[]{
                    String.valueOf(rank++),
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    entry.getValue() + " Loans Dispatched"
            });
        }
        UIRender.renderTable(headers, rows);
    }

    public void viewOverdueBooks() {
        LocalDate today = LocalDate.now();

        // Filters down transaction lists to grab unreturned records past their calculated due dates
        List<BorrowTransaction> overdueTransactions = borrowTransactionManager.getTransactions()
                .stream()
                .filter(tx -> tx.getReturnDate() == null && today.isAfter(tx.getDueDate()))
                .sorted(NEWEST_BORROW_TRANSCATION_COMPARATOR)
                .collect(Collectors.toList());

        if (overdueTransactions.isEmpty()) {
            System.out.println("\nExcellent status check: Zero active overdue circulation records verified.");
            return;
        }

        UIRender.renderHeader("Active Overdue Asset Circulation Violations Manifest");
        String[] headers = {"Book ID Key", "Asset Book Title Line", "Borrower Legal Name", "Target Due Date", "Elapsed Vector", "Live Fine Accrued"};
        List<String[]> rows = new ArrayList<>();

        for (BorrowTransaction tx : overdueTransactions) {
            Book book = bookManager.findBookById(tx.getBookId());
            Member member = memberManager.findMemberById(tx.getMemberId());

            String title = (book != null) ? book.getTitle() : "Unknown Asset Record Link";
            String borrower = (member != null) ? member.getName() : "Unknown Member Card Link";

            long daysLate = ChronoUnit.DAYS.between(tx.getDueDate(), today);
            double currentAccruedFine = borrowTransactionManager.calculateCurrentFine(tx);

            rows.add(new String[]{
                    tx.getBookId(),
                    title,
                    borrower,
                    tx.getDueDate().toString(),
                    daysLate + " Days Past Due",
                    String.format("%,.0f VND", currentAccruedFine)
            });
        }
        UIRender.renderTable(headers, rows);
    }
}