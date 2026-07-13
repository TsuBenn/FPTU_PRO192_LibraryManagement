package utilities;

import config.AppContext;
import exceptions.AbortInputException;
import exceptions.InvalidBookException;
import models.Book;
import models.BookType;
import models.BorrowTransaction;
import models.Member;
import policies.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

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

    public double getDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase(ABORT_SIGNAL))
                throw new AbortInputException();
            if (Validator.isValidDouble(input))
                return Double.parseDouble(input);
            UIRender.renderError("Invalid decimal format. Please re-enter.");
        }
    }

    public int getValidPublicationYear() {
        while (true) {
            int year = getInt("Enter Publication Year (YYYY): ");
            if (year >= 0 && year <= LocalDate.now().getYear()) {
                return year;
            }
            UIRender.renderError("Year must be between 0 and " + LocalDate.now().getYear());
        }
    }

    public int getValidQuantity() {
        while (true) {
            int qty = getInt("Enter Total Stock Quantity: ");
            if (qty > 0) return qty;
            UIRender.renderError("Quantity must be greater than zero.");
        }
    }

    public double getValidReplacementCost() {
        while (true) {
            double cost = getDouble("Enter Book Replacement Compensation Cost (VND): ");
            if (cost >= 0) return cost;
            UIRender.renderError("Cost cannot be negative.");
        }
    }

    public int getChoice(String prompt, int maxOption) {
        while (true) {
            int choice = getInt(prompt);
            if (choice >= 0 && choice <= maxOption) return choice;
            UIRender.renderError("Selection out of bounds. Range [0-" + maxOption + "].");
        }
    }

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

    public Book inputNewBookData() {
        String title = getString("Enter Book Title: ");
        String author = getString("Enter Author: ");
        String genre = getString("Enter Genre: ");
        double price = getDouble("Enter book price(k). Eg: 1k -> 1000 vnd: ") * 1000;
        int year = getValidPublicationYear();
        int qty = getValidQuantity();

        System.out.println("\nSelect Book Type:");
        System.out.println("  [1] Novel (Standard Public Access)");
        System.out.println("  [2] TextBook (Academic Priority)");
        System.out.println("  [3] Document (Restricted to Teacher/Premium)");
        System.out.println("  [4] Limited Document (Strictly Read-Only/Archival)");
        int type = getChoice("Select type: ", 4);

        BookType bookType;
        switch (type) {
            case 2: bookType = BookType.TEXTBOOK; break;
            case 3: bookType = BookType.DOCUMENT; break;
            case 4: bookType = BookType.LIMITED; break;
            default: bookType = BookType.NOVEL; break;
        }
        return new Book(title, author, genre, year, price, qty, bookType);
    }

    public Member inputNewMemberData() {
        String name = getString("Enter Full Legal Name: ");
        String phone = getString("Enter Contact Phone: ");
        String email = getString("Enter System Email: ");

        System.out.println("\nSelect Member Account Tier:");
        System.out.println("  [1] Regular Member (Base Limits)");
        System.out.println("  [2] Student (Academic Tier)");
        System.out.println("  [3] Premium Member (High Quota)");
        System.out.println("  [4] Teacher (Maximum Priority Tier)");
        int type = getChoice("Select account tier index: ", 4);

        MemberPolicy policy;
        switch (type) {
            case 2: policy = new StudentPolicy(); break;
            case 3: policy = new PremiumPolicy(); break;
            case 4: policy = new TeacherPolicy(); break;
            default: policy = new RegularPolicy(); break;
        }
        return new Member(name, phone, email, policy);
    }

    public Book updateBookModelFields(Book currentBook) throws InvalidBookException {
        System.out.println("\n[INFO] Press Enter to skip a field and keep current values.\n");

        // 1. Đọc dữ liệu từ người dùng nhập vào
        String title = getString("Update Title [" + currentBook.getTitle() + "]: ");
        String author = getString("Update Author [" + currentBook.getAuthor() + "]: ");
        String genre = getString("Update Genre [" + currentBook.getGenre() + "]: ");
        String priceRaw = getString("Update Price [" + currentBook.getPrice() + "]. Eg: 1k -> 1000: ");
        String yearRaw = getString("Update Publication Year [" + currentBook.getPublicationYear() + "]: ");
        String qtyRaw = getString("Update Max Stock Quantity [" + currentBook.getTotalQuantity() + "]: ");

        String updatedTitle = title.isEmpty() ? currentBook.getTitle() : title;
        String updatedAuthor = author.isEmpty() ? currentBook.getAuthor() : author;
        String updatedGenre = genre.isEmpty() ? currentBook.getGenre() : genre;

        double updatedPrice = currentBook.getPrice();
        if (!priceRaw.isEmpty()) {
            if (!Validator.isValidDouble(priceRaw)) {
                throw new InvalidBookException("Invalid price input format!");
            }
            double parsedPrice = Double.parseDouble(priceRaw) * 1000;
            if (parsedPrice < 0) {
                throw new InvalidBookException("Price cannot be negative!");
            }
            updatedPrice = parsedPrice;
        }

        int updatedYear = currentBook.getPublicationYear();
        if (!yearRaw.isEmpty()) {
            if (!Validator.isValidInt(yearRaw)) {
                throw new InvalidBookException("Invalid publication year format!");
            }
            int parsedYear = Integer.parseInt(yearRaw);
            int currentSystemYear = LocalDate.now().getYear();
            if (parsedYear < 0 || parsedYear > currentSystemYear) {
                throw new InvalidBookException("Publication year must be between 0 and " + currentSystemYear);
            }
            updatedYear = parsedYear;
        }

        int updatedTotalQty = currentBook.getTotalQuantity();
        if (!qtyRaw.isEmpty()) {
            if (!Validator.isValidInt(qtyRaw)) {
                throw new InvalidBookException("Invalid stock quantity format!");
            }
            int parsedQty = Integer.parseInt(qtyRaw);
            if (parsedQty <= 0) {
                throw new InvalidBookException("Max stock quantity must be positive!");
            }
            updatedTotalQty = parsedQty;
        }

        Book newBook = new Book(
                currentBook.getId(),
                updatedTitle,
                updatedAuthor,
                updatedGenre,
                updatedPrice,
                updatedYear,
                updatedTotalQty,
                currentBook.getAvailableQuantity(),
                currentBook.getBookType()
                );

        newBook.setAvailableQuantity(currentBook.getAvailableQuantity()
                + (updatedTotalQty - currentBook.getTotalQuantity()));

        return newBook;
    }

    public Member updateMemberModelFields(Member currentMember) {
        System.out.println("\n[INFO] Press Enter to skip a field and keep current values.\n");
        String name = getString("Update Name [" + currentMember.getName() + "]: ");
        String phone = getString("Update Phone [" + currentMember.getPhone() + "]: ");
        String email = getString("Update Email [" + currentMember.getEmail() + "]: ");

        String updatedName = name.isEmpty() ? currentMember.getName() : name;
        String updatedPhone = phone.isEmpty() ? currentMember.getPhone() : phone;
        String updatedEmail = email.isEmpty() ? currentMember.getEmail() : email;

        Member updated = new Member(currentMember.getId(), updatedName, updatedPhone, updatedEmail,
                currentMember.getPolicy(), currentMember.getBorrowedCount());
        updated.copyHistoryFrom(currentMember);
        return updated;
    }

    public LocalDate inputCustomDueDate(LocalDate borrowDate) {
        while (true) {
            System.out.println("Enter Custom Due Date components:");
            int day = getInt("  Enter Day (DD): ");
            int month = getInt("  Enter Month (MM): ");
            int year = getInt("  Enter Year (YYYY): ");

            if (Validator.isValidDate(day, month, year)) {
                LocalDate date = LocalDate.of(year, month, day);
                if (!date.isBefore(borrowDate)) return date;
                UIRender.renderError("Due date cannot be before borrow date.");
            } else {
                UIRender.renderError("Invalid calendar date constructed. Re-enter.");
            }
        }
    }

    public BorrowTransaction selectTransactionFromList(List<BorrowTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) return null;
        int idx = getInt("Select transaction row index (1-" + transactions.size() + "): ") - 1;
        if (idx >= 0 && idx < transactions.size()) {
            return transactions.get(idx);
        }
        UIRender.renderError("Index reference mismatch.");
        return null;
    }

    public Member searchAndSelectMember(List<Member> matches) {
        if (matches.isEmpty()) {
            UIRender.renderError("No matching members found.");
            UIRender.pauseEnter();
            return null;
        }

        UIRender.renderTable(matches, "Search Results", AppContext.MEMBER_TABLE_RENDERER);
        if (matches.size() == 1) return matches.get(0);

        int idx = getChoice("Select member index (1-" + matches.size() + "): ", matches.size());
        return matches.get(idx - 1);
    }

    public Book searchAndSelectBook(List<Book> matches) {
        if (matches.isEmpty()) {
            UIRender.renderError("No matching book found.");
            UIRender.pauseEnter();
            return null;
        }

        UIRender.renderTable(matches, "Search Results", AppContext.BOOK_TABLE_RENDERER );
        if (matches.size() == 1) return matches.get(0);

        int idx = getChoice("Select member index (1-" + matches.size() + "): ", matches.size());
        return matches.get(idx - 1);
    }
}
