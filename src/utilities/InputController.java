package utilities;

import java.util.Scanner;
import java.time.LocalDate;
import java.util.List;

import models.*;

public class InputController {
    private static final Scanner scanner = new Scanner(System.in);

    public static String getString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int getInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (Validator.isValidInt(input)) {
                return Integer.parseInt(input);
            }
            UIRender.renderError("Invalid integer format. Please re-enter.");
        }
    }

    public static double getDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (Validator.isValidDouble(input)) {
                return Double.parseDouble(input);
            }
            UIRender.renderError("Invalid decimal format. Please re-enter.");
        }
    }

    public static int getValidPublicationYear() {
        while (true) {
            int year = getInt("Enter Publication Year (YYYY): ");
            if (year >= 0 && year <= LocalDate.now().getYear()) {
                return year;
            }
            UIRender.renderError("Year must be between 0 and " + LocalDate.now().getYear());
        }
    }

    public static int getValidQuantity() {
        while (true) {
            int qty = getInt("Enter Total Stock Quantity: ");
            if (qty > 0) return qty;
            UIRender.renderError("Quantity must be greater than zero.");
        }
    }

    public static double getValidReplacementCost() {
        while (true) {
            double cost = getDouble("Enter Book Replacement Compensation Cost (VND): ");
            if (cost >= 0) return cost;
            UIRender.renderError("Cost cannot be negative.");
        }
    }

    public static int getChoice(String prompt, int maxOption) {
        while (true) {
            int choice = getInt(prompt);
            if (choice >= 0 && choice <= maxOption) return choice;
            UIRender.renderError("Selection out of bounds. Range [0-" + maxOption + "].");
        }
    }

    public static Book inputNewBookData() {
        String title = getString("Enter Book Title: ");
        String author = getString("Enter Author: ");
        String genre = getString("Enter Genre: ");
        int year = getValidPublicationYear();
        int qty = getValidQuantity();

        System.out.println("\nSelect Asset Sub-Class Category:");
        System.out.println("  [1] Novel Book (Standard Public Access)");
        System.out.println("  [2] TextBook (Academic Priority)");
        System.out.println("  [3] Document (Restricted to Teacher/Premium)");
        System.out.println("  [4] Limited Document (Strictly Read-Only/Archival)");
        int type = getChoice("Select class identity type: ", 4);

        switch (type) {
            case 2: return new TextBook(title, author, genre, year, qty);
            case 3: return new Document(title, author, genre, year, qty);
            case 4: return new LimitedDocument(title, author, genre, year, qty);
            default: return new NovelBook(title, author, genre, year, qty);
        }
    }

    public static Member inputNewMemberData() {
        String name = getString("Enter Full Legal Name: ");
        String phone = getString("Enter Contact Phone: ");
        String email = getString("Enter System Email: ");

        System.out.println("\nSelect Member Account Tier:");
        System.out.println("  [1] Regular Member (Base Limits)");
        System.out.println("  [2] Student (Academic Tier)");
        System.out.println("  [3] Premium Member (High Quota)");
        System.out.println("  [4] Teacher (Maximum Priority Tier)");
        int type = getChoice("Select account tier index: ", 4);

        switch (type) {
            case 2: return new Student(name, phone, email);
            case 3: return new PremiumMember(name, phone, email);
            case 4: return new Teacher(name, phone, email);
            default: return new RegularMember(name, phone, email);
        }
    }

    /**
     * FIXED: This method now generates and RETURNS a completely NEW Book object instance.
     */
    public static Book updateBookModelFields(Book currentBook) {
        System.out.println("\n[INFO] Press Enter to skip a field and keep current values.\n");
        String title = getString("Update Title [" + currentBook.getTitle() + "]: ");
        String author = getString("Update Author [" + currentBook.getAuthor() + "]: ");
        String genre = getString("Update Genre [" + currentBook.getGenre() + "]: ");

        String updatedTitle = title.isEmpty() ? currentBook.getTitle() : title;
        String updatedAuthor = author.isEmpty() ? currentBook.getAuthor() : author;
        String updatedGenre = genre.isEmpty() ? currentBook.getGenre() : genre;

        int updatedYear = currentBook.getPublicationYear();
        while (true) {
            String yearRaw = getString("Update Publication Year [" + currentBook.getPublicationYear() + "]: ");
            if (yearRaw.isEmpty()) break;
            if (Validator.isValidInt(yearRaw)) {
                int year = Integer.parseInt(yearRaw);
                if (year >= 0 && year <= LocalDate.now().getYear()) {
                    updatedYear = year;
                    break;
                }
            }
            UIRender.renderError("Invalid publication year limits.");
        }

        int updatedTotalQty = currentBook.getTotalQuantity();
        while (true) {
            String qtyRaw = getString("Update Max Stock Quantity [" + currentBook.getTotalQuantity() + "]: ");
            if (qtyRaw.isEmpty()) break;
            if (Validator.isValidInt(qtyRaw)) {
                int total = Integer.parseInt(qtyRaw);
                if (total <= 0) {
                    UIRender.renderError("Quantity must be positive.");
                } else {
                    updatedTotalQty = total;
                    break;
                }
            } else {
                UIRender.renderError("Invalid integer format.");
            }
        }

        // Deep copy instantiation simulation
        Book newBook = new Book(updatedTitle, updatedAuthor, updatedGenre, updatedYear, updatedTotalQty);
        // Retain original identity token system key and runtime balances
        // Reflection setting simulation or manual internal sync via standard setup wrapper
        newBook.setAvailableQuantity(currentBook.getAvailableQuantity() + (updatedTotalQty - currentBook.getTotalQuantity()));
        return newBook;
    }

    /**
     * FIXED: Generates and RETURNS a completely NEW Member object instance.
     */
    public static Member updateMemberModelFields(Member currentMember) {
        System.out.println("\n[INFO] Press Enter to skip a field and keep current values.\n");
        String name = getString("Update Name [" + currentMember.getName() + "]: ");
        String phone = getString("Update Phone [" + currentMember.getPhone() + "]: ");
        String email = getString("Update Email [" + currentMember.getEmail() + "]: ");

        String updatedName = name.isEmpty() ? currentMember.getName() : name;
        String updatedPhone = phone.isEmpty() ? currentMember.getPhone() : phone;
        String updatedEmail = email.isEmpty() ? currentMember.getEmail() : email;

        return new Member(updatedName, updatedPhone, updatedEmail);
    }

    public static LocalDate inputCustomDueDate(LocalDate borrowDate) {
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

    public static BorrowTransaction selectTransactionFromList(List<BorrowTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) return null;
        int idx = getInt("Select transaction row index target selection (1-" + transactions.size() + "): ") - 1;
        if (idx >= 0 && idx < transactions.size()) {
            return transactions.get(idx);
        }
        UIRender.renderError("Index reference mismatch.");
        return null;
    }
}