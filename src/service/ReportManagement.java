package service;

import models.Book;
import models.Member;
import models.BorrowTransaction;
import models.TransactionStatus;
import utilities.UIRender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportManagement {
    private final BookManagement bookManagement;
    private final MemberManagement memberManagement;
    private final BorrowTransactionManagement transactionManagement;

    public ReportManagement(BookManagement bookManagement, MemberManagement memberManagement, BorrowTransactionManagement transactionManagement) {
        this.bookManagement = bookManagement;
        this.memberManagement = memberManagement;
        this.transactionManagement = transactionManagement;
    }

    public void displayMemberHistory(String memberId) {
        Member member = memberManagement.findById(memberId);
        if (member == null) { UIRender.renderError("Member profile missing from directory."); return; }

        UIRender.renderHeader("Borrowing History For Member: " + member.getName().toUpperCase());
        String[] headers = {"TX ID", "Book Title Status", "Checkout Date", "Status Log"};
        List<String[]> dataRows = new ArrayList<>();

        List<String> txIds = member.getTransactionIds();

        for (String id : txIds) {
            BorrowTransaction tx = transactionManagement.findById(id);
            if (tx != null) {
                Book book = bookManagement.findById(tx.getBookId());
                String bookTitle = (book != null) ? book.getTitle() : "[DELETED BOOK SNAPSHOT]";

                dataRows.add(new String[]{
                        tx.getId(), bookTitle, tx.getBorrowDate().toString(), tx.getStatus().toString()
                });
            }
        }
        UIRender.renderTable(headers, dataRows);
    }

    public void displayOverdueTransactions() {
        UIRender.renderHeader("Live Overdue Transactions Alert Log");
        String[] headers = {"TX ID", "Borrower Name", "Book Title", "Due Date Status"};
        List<String[]> dataRows = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (BorrowTransaction tx : transactionManagement.getAllTransactions()) {
            if (tx.getStatus() == TransactionStatus.ACTIVE && today.isAfter(tx.getDueDate())) {

                // Tra cứu thông tin Member & Book qua ID liên kết
                Member m = memberManagement.findById(tx.getMemberId());
                Book b = bookManagement.findById(tx.getBookId());

                String memberName = (m != null) ? m.getName() : "[GHOST MEMBER]";
                String bookTitle = (b != null) ? b.getTitle() : "[GHOST BOOK]";

                dataRows.add(new String[]{
                        tx.getId(), memberName, bookTitle, "OVERDUE (Due: " + tx.getDueDate() + ")"
                });
            }
        }
        if (dataRows.isEmpty()) { UIRender.renderSuccess("Perfect! No overdue entries found."); return; }
        UIRender.renderTable(headers, dataRows);
    }

    public void displayMembersInDebt() {
        UIRender.renderHeader("Outstanding Debt Financial Reports");
        String[] headers = {"Member ID", "Full Name", "Email Address", "Unpaid Balance"};
        List<String[]> dataRows = new ArrayList<>();

        for (Member m : memberManagement.getAllMember()) {
            if (m.getFineMoney() > 0.0) {
                dataRows.add(new String[]{
                        m.getId(), m.getName(), m.getEmail(), String.format("%.2f VND", m.getFineMoney())
                });
            }
        }
        if (dataRows.isEmpty()) { UIRender.renderSuccess("Great! All members have cleared their balances."); return; }
        UIRender.renderTable(headers, dataRows);
    }

    public void displayPopularBooks() {
        UIRender.renderHeader("Popular Books Traction Frequency Rankings");
        String[] headers = {"Book ID", "Title", "Author", "Total Loan Frequency"};
        List<String[]> dataRows = new ArrayList<>();

        for (Book b : bookManagement.getAllBooks()) {
            int count = 0;
            for (BorrowTransaction tx : transactionManagement.getAllTransactions()) {
                if (tx.getBookId().equalsIgnoreCase(b.getId())) {
                    count++;
                }
            }
            if (count > 0) {
                dataRows.add(new String[]{ b.getId(), b.getTitle(), b.getAuthor(), String.valueOf(count) });
            }
        }
        dataRows.sort((a,b) -> Integer.compare(Integer.parseInt(b[3]), Integer.parseInt(a[3])));
        UIRender.renderTable(headers, dataRows);
    }
}