package config;

import models.*;
import policies.MemberPolicy;
import repositories.io.Writeable;
import repositories.io.Readable;

import java.time.LocalDate;

public class DataTransfer {
    public static final Readable<Book> BOOK_READER = contents -> {
        String[] p = contents.split(",");
        String id = p[0].trim();
        String title = p[1].trim();
        String author = p[2].trim();
        String genre = p[3].trim();
        Double price = Double.parseDouble(p[4].trim());
        Integer publicationYear = Integer.parseInt(p[5].trim());
        Integer totalQuantity = Integer.parseInt(p[6].trim());
        Integer availableQuantity = Integer.parseInt(p[7].trim());
        BookType bookType = BookType.valueOf(p[8].trim());
        return new Book(
                id,                    // p[0]
                title,                 // p[1]
                author,                // p[2]
                genre,                 // p[3]
                price,                 // p[4]
                publicationYear,       // p[5]
                totalQuantity,         // p[6]
                availableQuantity,     // p[7]
                bookType               // p[8]
        );
    };

    public static final Writeable<Book> BOOK_WRITER = b -> String.join(",",
            b.getId(),                 // p[0]
            b.getTitle(),              // p[1]
            b.getAuthor(),             // p[2]
            b.getGenre(),              // p[3]
            String.valueOf(b.getPrice()),              // p[4]
            String.valueOf(b.getPublicationYear()),    // p[5]
            String.valueOf(b.getTotalQuantity()),      // p[6]
            String.valueOf(b.getAvailableQuantity()),  // p[7]
            String.valueOf(b.getBookType())            // p[8]
    );


    // =========================================================================
    // 2. LOGIC CHO MODEL: MEMBER
    // =========================================================================
    public static final Readable<Member> MEMBER_READER = contents -> {
        String[] p = contents.split(",");
        String id = p[0].trim();
        String name = p[1].trim();
        String phone = p[2].trim();
        String email = p[3].trim();
        MemberPolicy policy = MemberPolicy.valueOf(p[4].trim());
        Integer borrowedCount = Integer.parseInt(p[5].trim());
        Double fine = Double.parseDouble(p[6]);
        return new Member(
                id,                    // p[0]
                name,                  // p[1]
                phone,                 // p[2]
                email,                 // p[3]
                fine,
                policy,                // p[4]
                borrowedCount         // p[6]
        );
    };

    public static final Writeable<Member> MEMBER_WRITER = m -> String.join(",",
            m.getId(),                             // p[0]
            m.getName(),                           // p[1]
            m.getPhone(),                          // p[2]
            m.getEmail(),                          // p[3]
            m.getPolicy().getTierName(),           // p[4]
            String.valueOf(m.getBorrowedCount()),  // p[5]
            String.valueOf(m.getFine())
    );


    // =========================================================================
    // 3. LOGIC CHO MODEL: BORROW TRANSACTION
    // =========================================================================
    public static final Readable<BorrowTransaction> TRANSACTION_READER = contents -> {
        String[] p = contents.split(",");
        String transactionId = p[0].trim();
        String memberId = p[1].trim();
        String memberName = p[2].trim();
        String bookId = p[3].trim();
        String bookTitle = p[4].trim();
        TransactionStatus transactionStatus = TransactionStatus.valueOf(p[5].trim());
        LocalDate borrowDate = LocalDate.parse(p[6].trim());
        LocalDate dueDate = LocalDate.parse(p[7].trim());
        LocalDate returnDate = p[8].trim().equals("null") ? null : LocalDate.parse(p[8].trim());
        Double finePaid = Double.parseDouble(p[9].trim());
        return new BorrowTransaction(
                transactionId,         // p[0]
                memberId,              // p[1]
                memberName,            // p[2]
                bookId,                // p[3]
                bookTitle,             // p[4]
                transactionStatus,     // p[5]
                borrowDate,            // p[6]
                dueDate,               // p[7]
                returnDate,            // p[8]
                finePaid               // p[9]
        );
    };

    public static final Writeable<BorrowTransaction> TRANSACTION_WRITER = t -> String.join(",",
            t.getTransactionId(),              // p[0]
            t.getMemberId(),                   // p[1]
            t.getMemberName(),                 // p[2]
            t.getBookId(),                     // p[3]
            t.getBookTitle(),                  // p[4]
            String.valueOf(t.getTransactionStatus()), // p[5]
            t.getBorrowDate().toString(),      // p[6]
            t.getDueDate().toString(),         // p[7]
            t.getReturnDate() == null ? "null" : t.getReturnDate().toString(), // p[8]
            String.valueOf(t.getFinePaid())    // p[9]
    );
}
