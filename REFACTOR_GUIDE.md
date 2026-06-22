# Hướng dẫn Refactor — Library Management System
> Tài liệu này dành cho AI agent thực hiện refactor toàn bộ dự án.
> Đọc toàn bộ trước khi chỉnh sửa bất kỳ file nào.

---

## Nguyên tắc bắt buộc

1. **Không thay đổi logic nghiệp vụ** — chỉ tái cấu trúc, không thêm/bớt tính năng.
2. **Thực hiện theo đúng thứ tự** các task bên dưới — các task sau phụ thuộc task trước.
3. **Sau mỗi task**, kiểm tra toàn bộ dự án vẫn compile được trước khi sang task tiếp theo.
4. **Không dùng thư viện ngoài** — Java Core thuần túy.

---

## Cấu trúc thư mục sau khi hoàn thành

```
config/
  AppContext.java
  DataSeeder.java

controllers/
  LibraryController.java
  handlers/
    BookHandler.java
    MemberHandler.java
    CirculationHandler.java
    ReportHandler.java

exceptions/
  LibraryException.java
  DuplicateEntryException.java
  EntityNotFoundException.java
  BorrowPolicyException.java
  InvalidOperationException.java
  AbortInputException.java

models/
  Book.java                  (giữ nguyên, thêm BookType enum)
  BookType.java              (enum mới)
  BorrowTransaction.java     (thêm snapshot fields)
  Member.java                (giữ nguyên, inject MemberPolicy)
  TransactionStatus.java     (giữ nguyên)

policies/
  MemberPolicy.java          (interface)
  RegularPolicy.java
  PremiumPolicy.java
  StudentPolicy.java
  TeacherPolicy.java

repositories/
  BookRepository.java        (đổi sang LinkedHashMap)
  MemberRepository.java      (đổi sang LinkedHashMap)
  BorrowTransactionRepository.java (đổi sang LinkedHashMap)

services/
  BookService.java           (return void, ném exception)
  MemberService.java         (return void, ném exception)
  BorrowTransactionService.java
  ReportService.java         (bỏ BookRepository dependency)
  BorrowPolicy.java          (interface, giữ nguyên)

utilities/
  IDManager.java             (giữ nguyên)
  InputController.java       (nhận Scanner qua constructor, thêm :q)
  UIRender.java              (generic renderTable)
  Validator.java             (đổi sang pre-compiled Pattern)
  TableRenderer.java         (interface mới)
  ConfirmResult.java         (enum mới)
  renderers/
    BookTableRenderer.java
    MemberTableRenderer.java
    TransactionTableRenderer.java

Main.java
```

---

## TASK 1 — Tạo Exception hierarchy

**Tạo mới toàn bộ, không sửa file cũ.**

### `exceptions/LibraryException.java`
```java
package exceptions;

public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}
```

### `exceptions/DuplicateEntryException.java`
```java
package exceptions;

public class DuplicateEntryException extends LibraryException {
    public DuplicateEntryException(String field, String value) {
        super("Duplicate entry: " + field + " '" + value + "' already exists.");
    }
}
```

### `exceptions/EntityNotFoundException.java`
```java
package exceptions;

public class EntityNotFoundException extends LibraryException {
    public EntityNotFoundException(String entityType, String id) {
        super(entityType + " with ID '" + id + "' not found.");
    }
}
```

### `exceptions/BorrowPolicyException.java`
```java
package exceptions;

public class BorrowPolicyException extends LibraryException {
    public BorrowPolicyException(String reason) {
        super("Borrow policy violation: " + reason);
    }
}
```

### `exceptions/InvalidOperationException.java`
```java
package exceptions;

public class InvalidOperationException extends LibraryException {
    public InvalidOperationException(String reason) {
        super("Operation not allowed: " + reason);
    }
}
```

### `exceptions/AbortInputException.java`
```java
package exceptions;

// Unchecked — tín hiệu điều hướng, không phải lỗi nghiệp vụ
public class AbortInputException extends RuntimeException {
    public AbortInputException() {
        super("Input sequence aborted by user.");
    }
}
```

---

## TASK 2 — Tạo MemberPolicy (Strategy Pattern)

**Xóa các file:** `models/RegularMember.java`, `models/PremiumMember.java`,
`models/Student.java`, `models/Teacher.java`

**Tạo mới package `policies/`:**

### `policies/MemberPolicy.java`
```java
package policies;

import models.BookType;

public interface MemberPolicy {
    int getBorrowLimit();
    int getLoanDurationDays();
    double getLateFinePerDay();
    double getLostBookMultiplier();
    boolean canBorrow(BookType bookType);
    String getTierName();
}
```

### `policies/RegularPolicy.java`
```java
package policies;

import models.BookType;

public class RegularPolicy implements MemberPolicy {
    @Override public int getBorrowLimit()             { return 3; }
    @Override public int getLoanDurationDays()        { return 14; }
    @Override public double getLateFinePerDay()       { return 5000.0; }
    @Override public double getLostBookMultiplier()   { return 1.0; }
    @Override public String getTierName()             { return "Regular"; }

    @Override
    public boolean canBorrow(BookType type) {
        return type == BookType.NOVEL || type == BookType.TEXTBOOK;
    }
}
```

### `policies/PremiumPolicy.java`
```java
package policies;

import models.BookType;

public class PremiumPolicy implements MemberPolicy {
    @Override public int getBorrowLimit()             { return 7; }
    @Override public int getLoanDurationDays()        { return 28; }
    @Override public double getLateFinePerDay()       { return 10000.0; }
    @Override public double getLostBookMultiplier()   { return 1.25; }
    @Override public String getTierName()             { return "Premium"; }

    @Override
    public boolean canBorrow(BookType type) {
        return type != BookType.LIMITED;
    }
}
```

### `policies/StudentPolicy.java`
```java
package policies;

public class StudentPolicy extends RegularPolicy {
    @Override public String getTierName() { return "Student"; }
}
```

### `policies/TeacherPolicy.java`
```java
package policies;

public class TeacherPolicy extends PremiumPolicy {
    @Override public String getTierName() { return "Teacher"; }
}
```

---

## TASK 3 — Thêm BookType enum, sửa Book và Member

### `models/BookType.java` (tạo mới)
```java
package models;

public enum BookType {
    NOVEL, DOCUMENT, LIMITED, TEXTBOOK
}
```

### `models/Book.java` — thêm field `bookType`

Thêm vào constructor và getter. Xóa các subclass:
`models/NovelBook.java`, `models/Document.java`,
`models/LimitedDocument.java`, `models/TextBook.java`

Sửa `Book.java`:
- Thêm field `private final BookType bookType`
- Thêm tham số `BookType bookType` vào constructor
- Thêm `getBookType()` getter
- Sửa `toString()` thêm `bookType.name()` ở cuối

### `models/Member.java` — inject MemberPolicy

Xóa toàn bộ field `currentBorrowLimit` kiểu int.
Thêm field `private MemberPolicy policy`.
Sửa constructor nhận thêm `MemberPolicy policy`.

Thêm các method delegate sang policy:
```java
public int    getBorrowLimit()           { return policy.getBorrowLimit(); }
public int    getLoanDurationDays()      { return policy.getLoanDurationDays(); }
public double getLateFinePerDay()        { return policy.getLateFinePerDay(); }
public double getLostBookMultiplier()    { return policy.getLostBookMultiplier(); }
public boolean canBorrow(BookType type)  { return policy.canBorrow(type); }
public String getTierName()              { return policy.getTierName(); }

public void upgradePolicy(MemberPolicy newPolicy) {
    this.policy = newPolicy;
}

// Thay thế getCurrentBorrowLimit / setCurrentBorrowLimit bằng:
private int borrowedCount = 0;
public int getRemainingBorrowSlots()     { return policy.getBorrowLimit() - borrowedCount; }
public void incrementBorrowedCount()     { this.borrowedCount++; }
public void decrementBorrowedCount()     { this.borrowedCount--; }
```

Sửa `toString()`:
```java
return id + "|" + name + "|" + phone + "|" + email
     + "|" + getBorrowLimit() + "|" + getTierName();
```

---

## TASK 4 — Thêm snapshot vào BorrowTransaction

### `models/BorrowTransaction.java`

Thêm 2 field snapshot (final, không thay đổi sau khi tạo):
```java
private final String memberName;
private final String bookTitle;
```

Sửa constructor:
```java
public BorrowTransaction(String memberId, String memberName,
                         String bookId,   String bookTitle,
                         LocalDate borrowDate) {
    this.transactionId = IDManager.transactionIDGenerator.newID();
    this.memberId   = memberId;
    this.memberName = memberName;
    this.bookId     = bookId;
    this.bookTitle  = bookTitle;
    this.borrowDate = borrowDate;
}
```

Thêm getter:
```java
public String getMemberName() { return memberName; }
public String getBookTitle()  { return bookTitle; }
```

---

## TASK 5 — Sửa Repository dùng LinkedHashMap

Áp dụng cho cả 3 repository: `BookRepository`, `MemberRepository`,
`BorrowTransactionRepository`.

Mẫu chung (lấy `BookRepository` làm ví dụ):
```java
// Đổi từ:
private final List<Book> database = new ArrayList<>();

// Sang:
private final Map<String, Book> database = new LinkedHashMap<>();

// save:
public void save(Book book) { database.put(book.getId(), book); }

// delete:
public void delete(Book book) { database.remove(book.getId()); }

// findById — O(1):
public Book findById(String id) { return database.get(id); }

// findAll:
public List<Book> findAll() { return new ArrayList<>(database.values()); }

// update — thay thế trực tiếp theo key:
public void update(Book oldBook, Book newBook) {
    if (database.containsKey(oldBook.getId())) {
        database.put(oldBook.getId(), newBook);
    }
}
```

`BorrowTransactionRepository` không có `update` và `delete` — giữ nguyên,
chỉ đổi storage sang Map với key là `transactionId`.

---

## TASK 6 — Sửa Validator dùng pre-compiled Pattern

### `utilities/Validator.java`

Xóa toàn bộ nội dung, viết lại:
```java
package utilities;

import java.time.YearMonth;
import java.util.regex.Pattern;

public class Validator {

    private static final Pattern INT_PATTERN   = Pattern.compile("^[+-]?[0-9]+$");
    private static final Pattern DOUBLE_PATTERN= Pattern.compile("^[+-]?[0-9]+(\\.[0-9]+)?$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[0-9a-zA-Z._%+-]+@[0-9a-zA-Z.-]+\\.[A-Za-z]{2,6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    private Validator() {}

    public static boolean isValidInt(String str) {
        return str != null && INT_PATTERN.matcher(str).matches();
    }

    public static boolean isValidDouble(String str) {
        return str != null && DOUBLE_PATTERN.matcher(str).matches();
    }

    public static boolean isValidEmail(String str) {
        return str != null && EMAIL_PATTERN.matcher(str).matches();
    }

    public static boolean isValidPhone(String str) {
        return str != null && PHONE_PATTERN.matcher(str).matches();
    }

    public static boolean isValidDate(int d, int m, int y) {
        if (y < 1 || y > 9999 || m < 1 || m > 12) return false;
        int daysInMonth = YearMonth.of(y, m).lengthOfMonth();
        return d > 0 && d <= daysInMonth;
    }
}
```

---

## TASK 7 — Sửa InputController nhận Scanner qua Constructor

### `utilities/InputController.java`

**Xóa** `private static final Scanner scanner = new Scanner(System.in)`

Sửa thành instance-based, nhận Scanner từ ngoài:
```java
package utilities;

import exceptions.AbortInputException;
import java.util.Scanner;
// ... các import khác giữ nguyên

public class InputController {
    private static final String ABORT_SIGNAL = ":q";
    private final Scanner scanner;

    public InputController(Scanner scanner) {
        this.scanner = scanner;
    }

    public String getString(String prompt) {
        System.out.print(prompt + " (':q' to cancel) ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase(ABORT_SIGNAL))
            throw new AbortInputException();
        return input;
    }

    public int getInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase(ABORT_SIGNAL))
                throw new AbortInputException();
            if (Validator.isValidInt(input))
                return Integer.parseInt(input);
            UIRender.renderError("Invalid integer format. Please re-enter.");
        }
    }

    // getDouble, getChoice, getValidPublicationYear, getValidQuantity,
    // getValidReplacementCost — sửa tương tự: dùng this.scanner, thêm :q check

    // inputNewBookData, inputNewMemberData, updateBookModelFields,
    // updateMemberModelFields — sửa để tạo đúng BookType enum và MemberPolicy
    // thay vì tạo subclass (NovelBook → new Book(..., BookType.NOVEL))
}
```

Thêm enum `ConfirmResult`:
```java
// utilities/ConfirmResult.java
package utilities;

public enum ConfirmResult { CONFIRM, CANCEL, EDIT }
```

Thêm method vào `InputController`:
```java
public ConfirmResult getConfirmation() {
    while (true) {
        System.out.println("\n  [Y] Confirm    [N] Cancel    [E] Edit");
        String input = getString("Your choice: ").toUpperCase();
        switch (input) {
            case "Y": return ConfirmResult.CONFIRM;
            case "N": return ConfirmResult.CANCEL;
            case "E": return ConfirmResult.EDIT;
            default:  UIRender.renderError("Please enter Y, N, or E.");
        }
    }
}
```

---

## TASK 8 — Sửa UIRender thành generic + TableRenderer

### `utilities/TableRenderer.java` (tạo mới)
```java
package utilities;

public interface TableRenderer<T> {
    String[] getHeaders();
    String[] toRow(T item);
}
```

### `utilities/UIRender.java`

Xóa 3 method cũ: `renderBookContentTable`, `renderMemberContentTable`,
`renderTransactionTable`.

Thêm 1 method generic thay thế:
```java
public static <T> void renderTable(List<T> items, String title, TableRenderer<T> renderer) {
    renderHeader(title);
    if (items == null || items.isEmpty()) {
        renderError("No data available.");
        return;
    }
    String[] headers = renderer.getHeaders();
    int colCount = headers.length;
    int[] widths = new int[colCount];
    for (int i = 0; i < colCount; i++) widths[i] = headers[i].length();

    List<String[]> rows = new ArrayList<>();
    for (T item : items) {
        String[] row = renderer.toRow(item);
        rows.add(row);
        for (int i = 0; i < colCount; i++)
            if (i < row.length) widths[i] = Math.max(widths[i], row[i].length());
    }

    String border = buildGridBorder(widths);
    System.out.println(border);
    printRow(headers, widths);
    System.out.println(border);
    for (String[] row : rows) printRow(row, widths);
    System.out.println(border);
}

private static void printRow(String[] cells, int[] widths) {
    System.out.print("|");
    for (int i = 0; i < widths.length; i++)
        System.out.printf(" %-" + widths[i] + "s |", i < cells.length ? cells[i] : "");
    System.out.println();
}
```

Thêm preview methods:
```java
public static void renderBookPreview(Book b) {
    renderHeader("Review Book Information");
    System.out.println("  Title  : " + b.getTitle());
    System.out.println("  Author : " + b.getAuthor());
    System.out.println("  Genre  : " + b.getGenre());
    System.out.println("  Year   : " + b.getPublicationYear());
    System.out.println("  Stock  : " + b.getTotalQuantity());
    System.out.println("  Type   : " + b.getBookType());
    System.out.println(DIVIDER_LINE);
}

public static void renderMemberPreview(Member m) {
    renderHeader("Review Member Information");
    System.out.println("  Name   : " + m.getName());
    System.out.println("  Phone  : " + m.getPhone());
    System.out.println("  Email  : " + m.getEmail());
    System.out.println("  Tier   : " + m.getTierName());
    System.out.println(DIVIDER_LINE);
}
```

### `utilities/renderers/BookTableRenderer.java` (tạo mới)
```java
package utilities.renderers;

import models.Book;
import utilities.TableRenderer;

public class BookTableRenderer implements TableRenderer<Book> {
    @Override
    public String[] getHeaders() {
        return new String[]{"ID", "Title", "Author", "Genre", "Year", "Available", "Total", "Type"};
    }

    @Override
    public String[] toRow(Book b) {
        return new String[]{
            b.getId(), b.getTitle(), b.getAuthor(), b.getGenre(),
            String.valueOf(b.getPublicationYear()),
            String.valueOf(b.getAvailableQuantity()),
            String.valueOf(b.getTotalQuantity()),
            b.getBookType().name()
        };
    }
}
```

### `utilities/renderers/MemberTableRenderer.java` (tạo mới)
```java
package utilities.renderers;

import models.Member;
import utilities.TableRenderer;

public class MemberTableRenderer implements TableRenderer<Member> {
    @Override
    public String[] getHeaders() {
        return new String[]{"ID", "Name", "Phone", "Email", "Limit", "Tier"};
    }

    @Override
    public String[] toRow(Member m) {
        return new String[]{
            m.getId(), m.getName(), m.getPhone(), m.getEmail(),
            String.valueOf(m.getBorrowLimit()),
            m.getTierName()
        };
    }
}
```

### `utilities/renderers/TransactionTableRenderer.java` (tạo mới)
```java
package utilities.renderers;

import models.BorrowTransaction;
import utilities.TableRenderer;

public class TransactionTableRenderer implements TableRenderer<BorrowTransaction> {
    @Override
    public String[] getHeaders() {
        return new String[]{"TX ID", "Member", "Book", "Borrow", "Due", "Return", "Status", "Fine"};
    }

    @Override
    public String[] toRow(BorrowTransaction tx) {
        return new String[]{
            tx.getTransactionId(),
            tx.getMemberName(),      // snapshot — không cần lookup
            tx.getBookTitle(),       // snapshot — không cần lookup
            tx.getBorrowDate().toString(),
            tx.getDueDate() != null ? tx.getDueDate().toString() : "-",
            tx.getReturnDate() != null ? tx.getReturnDate().toString() : "OUT",
            tx.getTransactionStatus().name(),
            String.format("%.0f", tx.getFinePaid())
        };
    }
}
```

---

## TASK 9 — Sửa Services ném Exception thay vì return boolean

### `services/BookService.java`

Sửa signature tất cả method:
```java
// Trước:
public boolean registerBook(Book book) { ... return false; }

// Sau:
public void registerBook(Book book) throws DuplicateEntryException {
    if (isDuplicate(book.getTitle(), book.getAuthor()))
        throw new DuplicateEntryException("Title+Author",
            book.getTitle() + " / " + book.getAuthor());
    bookRepository.save(book);
}

public void updateBook(Book oldBook, Book newBook)
        throws EntityNotFoundException, InvalidOperationException {
    if (oldBook == null)
        throw new EntityNotFoundException("Book", "unknown");
    int activeLoans = oldBook.getTotalQuantity() - oldBook.getAvailableQuantity();
    if (newBook.getTotalQuantity() < activeLoans)
        throw new InvalidOperationException(
            "New quantity (" + newBook.getTotalQuantity() +
            ") is less than active loans (" + activeLoans + ").");
    bookRepository.update(oldBook, newBook);
}

public void deleteBook(Book book) throws InvalidOperationException {
    if (book.getAvailableQuantity() != book.getTotalQuantity())
        throw new InvalidOperationException(
            "Cannot delete book with active loans outstanding.");
    bookRepository.delete(book);
}
```

Xóa `UIRender` import và mọi lời gọi UIRender khỏi BookService.

### `services/MemberService.java`

Sửa tương tự:
```java
public void registerMember(Member member) throws DuplicateEntryException {
    if (isPhoneDuplicate(member.getPhone()))
        throw new DuplicateEntryException("Phone", member.getPhone());
    if (isEmailDuplicate(member.getEmail()))
        throw new DuplicateEntryException("Email", member.getEmail());
    memberRepository.save(member);
}

public void updateMember(Member oldMember, Member newMember)
        throws EntityNotFoundException, DuplicateEntryException {
    if (oldMember == null)
        throw new EntityNotFoundException("Member", "unknown");
    // check duplicate excluding self
    memberRepository.update(oldMember, newMember);
}

public void deleteMember(Member member, int activeLoans)
        throws InvalidOperationException {
    if (activeLoans > 0)
        throw new InvalidOperationException(
            "Cannot delete member with " + activeLoans + " active loan(s).");
    memberRepository.delete(member);
}
```

### `services/BorrowTransactionService.java`

Xóa toàn bộ `instanceof` check. Sửa các method:

```java
@Override
public boolean isEligibleToBorrow(Member member, Book book) {
    // Giữ method này cho BorrowPolicy interface
    // Nhưng logic bên trong không dùng instanceof nữa
    return book.getAvailableQuantity() > 0
        && member.getRemainingBorrowSlots() > 0
        && member.canBorrow(book.getBookType()); // delegate sang Policy
}

@Override
public double calculateFine(BorrowTransaction tx, Member member, Book book) {
    LocalDate endDate = tx.getReturnDate() != null ? tx.getReturnDate() : LocalDate.now();
    long daysLate = ChronoUnit.DAYS.between(tx.getDueDate(), endDate);
    if (daysLate <= 0) return 0.0;
    return daysLate * member.getLateFinePerDay(); // delegate sang Policy
}

@Override
public int getMaxBorrowDay(Member member) {
    return member.getLoanDurationDays(); // delegate sang Policy
}

public void executeCheckout(Member member, Book book, BorrowTransaction tx)
        throws BorrowPolicyException {
    if (book.getAvailableQuantity() <= 0)
        throw new BorrowPolicyException(
            "Book '" + book.getTitle() + "' is out of stock.");
    if (member.getRemainingBorrowSlots() <= 0)
        throw new BorrowPolicyException(
            "Member '" + member.getName() + "' has reached borrow limit of "
            + member.getBorrowLimit() + ".");
    if (!member.canBorrow(book.getBookType()))
        throw new BorrowPolicyException(
            "Tier '" + member.getTierName() + "' cannot borrow "
            + book.getBookType() + " books.");

    tx.setTransactionStatus(TransactionStatus.BORROWING);
    tx.setDueDate(tx.getBorrowDate().plusDays(getMaxBorrowDay(member)));
    txRepository.save(tx);
    book.setAvailableQuantity(book.getAvailableQuantity() - 1);
    member.incrementBorrowedCount();
    member.addTransactionInfo(tx);
}
```

### `services/ReportService.java`

Xóa `BookRepository` khỏi dependency hoàn toàn:
```java
public class ReportService {
    private final BorrowTransactionService transactionService;
    // KHÔNG có BookRepository — snapshot đã có trong BorrowTransaction

    public ReportService(BorrowTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Các method giữ nguyên logic, chỉ bỏ BookRepository lookup
    // getPopularBooks vẫn cần BookRepository — giữ lại dependency này
    // hoặc trả về Map<String, Long> thay vì List<Book>
}
```

> **Lưu ý:** `getPopularBooks()` cần `BookRepository` để lấy thông tin đầy đủ
> của sách. Giữ lại dependency này riêng cho method đó là chấp nhận được.

---

## TASK 10 — Tạo config/AppContext và config/DataSeeder

### `config/AppContext.java`
```java
package config;

import repositories.*;
import services.*;
import utilities.InputController;
import java.util.Scanner;

public class AppContext {
    public final Scanner scanner;
    public final InputController input;

    public final BookRepository       bookRepository;
    public final MemberRepository     memberRepository;
    public final BorrowTransactionRepository txRepository;

    public final BookService              bookService;
    public final MemberService            memberService;
    public final BorrowTransactionService transactionService;
    public final ReportService            reportService;

    public AppContext() {
        scanner    = new Scanner(System.in);
        input      = new InputController(scanner);

        bookRepository    = new BookRepository();
        memberRepository  = new MemberRepository();
        txRepository      = new BorrowTransactionRepository();

        bookService        = new BookService(bookRepository);
        memberService      = new MemberService(memberRepository);
        transactionService = new BorrowTransactionService(txRepository);
        reportService      = new ReportService(transactionService, bookRepository);
    }
}
```

### `config/DataSeeder.java`
```java
package config;

import models.*;
import policies.*;

public class DataSeeder {
    public static void seed(AppContext ctx) {
        ctx.bookRepository.save(new Book(
            "The Great Gatsby", "F. Scott Fitzgerald", "Classic", 1925, 5, BookType.NOVEL));
        ctx.bookRepository.save(new Book(
            "1984", "George Orwell", "Dystopian", 1949, 3, BookType.NOVEL));
        ctx.bookRepository.save(new Book(
            "How to get 10 mark in PRO_192", "FPT Instructor", "Academic", 2024, 3, BookType.TEXTBOOK));

        ctx.memberRepository.save(new Member(
            "Benn", "0123456789", "benn@uni.edu.vn", new RegularPolicy()));
        ctx.memberRepository.save(new Member(
            "Nguyen Van Binh", "0373450305", "nguyenvanbinh542007@gmail.com", new StudentPolicy()));
    }
}
```

---

## TASK 11 — Tách Controller thành Handlers (SRP)

Tạo package `controllers/handlers/`. Mỗi handler nhận `AppContext` qua constructor.

### Mẫu chung cho mỗi Handler

```java
public class BookHandler {
    private static final TableRenderer<Book> RENDERER = new BookTableRenderer();

    private final BookService    bookService;
    private final BookRepository bookRepository;
    private final InputController input;

    public BookHandler(AppContext ctx) {
        this.bookService    = ctx.bookService;
        this.bookRepository = ctx.bookRepository;
        this.input          = ctx.input;
    }

    public void handle() {
        boolean active = true;
        String[] choices = {"Add Book", "Remove Book", "Update Book", "List Books"};
        while (active) {
            UIRender.clearScreen();
            UIRender.renderMenu("Book Management", choices);
            switch (input.getChoice("Select: ", 4)) {
                case 1: handleAdd();    break;
                case 2: handleDelete(); break;
                case 3: handleUpdate(); break;
                case 4: handleList();   break;
                case 0: active = false; break;
            }
        }
    }

    private void handleAdd() {
        UIRender.clearScreen();
        try {
            Book book = input.inputNewBookData();
            UIRender.renderBookPreview(book);
            ConfirmResult result = input.getConfirmation();
            if (result == ConfirmResult.CONFIRM) {
                bookService.registerBook(book);
                UIRender.renderSuccess("Book registered.");
            } else if (result == ConfirmResult.EDIT) {
                book = input.updateBookModelFields(book);
                bookService.registerBook(book);
                UIRender.renderSuccess("Book registered.");
            }
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        } catch (DuplicateEntryException e) {
            UIRender.renderError(e.getMessage());
        }
        UIRender.pauseEnter();
    }

    // handleDelete, handleUpdate, handleList — theo cùng pattern:
    // try { ... service call ... }
    // catch AbortInputException → renderError cancel
    // catch LibraryException    → renderError e.getMessage()
}
```

Tạo tương tự: `MemberHandler`, `CirculationHandler`, `ReportHandler`.

**`CirculationHandler`** gom cả 3 method: `handleBorrow()`, `handleReturn()`, `handleLost()`.

Lưu ý quan trọng khi tạo BorrowTransaction trong `CirculationHandler.handleBorrow()`:
```java
// Truyền snapshot vào constructor
BorrowTransaction tx = new BorrowTransaction(
    member.getId(),   member.getName(),
    book.getId(),     book.getTitle(),
    LocalDate.now()
);
```

---

## TASK 12 — Sửa LibraryController thành router thuần túy

```java
package controllers;

import config.AppContext;
import config.DataSeeder;
import controllers.handlers.*;
import utilities.InputController;
import utilities.UIRender;

public class LibraryController {
    private final BookHandler        bookHandler;
    private final MemberHandler      memberHandler;
    private final CirculationHandler circulationHandler;
    private final ReportHandler      reportHandler;
    private final InputController    input;

    public LibraryController() {
        AppContext ctx = new AppContext();
        DataSeeder.seed(ctx);

        this.input              = ctx.input;
        this.bookHandler        = new BookHandler(ctx);
        this.memberHandler      = new MemberHandler(ctx);
        this.circulationHandler = new CirculationHandler(ctx);
        this.reportHandler      = new ReportHandler(ctx);
    }

    public void start() {
        boolean running = true;
        String[] modules = {
            "Book Management", "Member Management",
            "Borrow Book", "Return Book", "Report Lost Book",
            "Analytics Dashboard"
        };
        while (running) {
            UIRender.clearScreen();
            UIRender.renderMenu("Library System", modules);
            switch (input.getChoice("Select: ", 6)) {
                case 1: bookHandler.handle();               break;
                case 2: memberHandler.handle();             break;
                case 3: circulationHandler.handleBorrow();  break;
                case 4: circulationHandler.handleReturn();  break;
                case 5: circulationHandler.handleLost();    break;
                case 6: reportHandler.handle();             break;
                case 0: running = false;                    break;
            }
        }
        // Đóng Scanner khi thoát — chỉ đóng 1 lần duy nhất ở đây
        // ctx.scanner.close(); // uncomment nếu cần
    }
}
```

---

## TASK 13 — Sửa Main.java

```java
import controllers.LibraryController;

public class Main {
    public static void main(String[] args) {
        new LibraryController().start();
    }
}
```

---

## Checklist kiểm tra sau khi hoàn thành

### Compile
- [ ] Toàn bộ project compile không có lỗi
- [ ] Không có `import` nào trỏ đến class đã xóa
  (`RegularMember`, `PremiumMember`, `Student`, `Teacher`,
  `NovelBook`, `Document`, `LimitedDocument`, `TextBook`)

### Không còn vi phạm
- [ ] Không có `instanceof` nào trong Service layer
- [ ] Không có `UIRender` call nào trong Service layer
- [ ] Không có `return false` / `return true` nào thay cho Exception trong Service
- [ ] `InputController` không có `static Scanner` — nhận qua constructor
- [ ] `Validator` dùng `Pattern.compile` pre-compiled, không dùng `String.matches()`
- [ ] `BorrowTransaction` có field `memberName` và `bookTitle` (final)
- [ ] `TransactionTableRenderer.toRow()` không gọi bất kỳ Repository nào
- [ ] Tất cả Repository dùng `LinkedHashMap`, `findById` là O(1)

### Đúng với Audit Log
- [ ] Entry #002: Scanner chỉ tạo 1 lần trong `AppContext`, truyền vào `InputController`
- [ ] Entry #004: Snapshot `memberName`, `bookTitle` trong `BorrowTransaction`
- [ ] Entry #007: `Validator` dùng pre-compiled `Pattern`
- [ ] Entry #005: `processReturn()` tính phạt, set trạng thái `NOT_PAID` nếu có phạt
  (không đóng giao dịch khi còn nợ)

### Hành vi người dùng
- [ ] Gõ `:q` ở bất kỳ prompt nào → thoát về menu, không mất dữ liệu
- [ ] Chọn nhầm menu → `AbortInputException` được bắt tại Handler, hiển thị "Cancelled"
- [ ] Sau khi nhập xong → hiển thị preview → hỏi Y/N/E trước khi lưu
