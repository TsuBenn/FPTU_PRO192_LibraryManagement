package config;

import models.*;
import policies.*;

public class DataSeeder {
    protected static void seed(AppContext ctx) {
        // ====================== BOOKS ======================
        ctx.bookRepository.save(new Book(
                "The Great Gatsby", "F. Scott Fitzgerald", "Classic", 1925, 100_000, 5, BookType.NOVEL));

        ctx.bookRepository.save(new Book(
                "1984", "George Orwell", "Dystopian", 1949, 200_000, 3, BookType.NOVEL));

        ctx.bookRepository.save(new Book(
                "How to get 10 mark in PRO_192", "FPT Instructor", "Academic", 2024, 300_000, 3, BookType.TEXTBOOK));

        ctx.bookRepository.save(new Book(
                "Clean Code", "Robert C. Martin", "Programming", 2008, 450_000, 7, BookType.TEXTBOOK));

        ctx.bookRepository.save(new Book(
                "Effective Java", "Joshua Bloch", "Programming", 2018, 550_000, 6, BookType.TEXTBOOK));

        ctx.bookRepository.save(new Book(
                "To Kill a Mockingbird", "Harper Lee", "Classic", 1960, 180_000, 4, BookType.NOVEL));

        ctx.bookRepository.save(new Book(
                "The Hobbit", "J.R.R. Tolkien", "Fantasy", 1937, 250_000, 8, BookType.NOVEL));

        ctx.bookRepository.save(new Book(
                "Atomic Habits", "James Clear", "Self-help", 2018, 320_000, 10, BookType.LIMITED));

        ctx.bookRepository.save(new Book(
                "Introduction to Algorithms", "Thomas H. Cormen", "Computer Science", 2022, 900_000, 2, BookType.TEXTBOOK));

        ctx.bookRepository.save(new Book(
                "Harry Potter and the Philosopher's Stone", "J.K. Rowling", "Fantasy", 1997, 220_000, 12, BookType.NOVEL));


// ====================== MEMBERS ======================
        ctx.memberRepository.save(new Member(
                "Benn", "0123456789", "benn@uni.edu.vn", new RegularPolicy()));

        ctx.memberRepository.save(new Member(
                "Nguyen Van Binh", "0373450305", "nguyenvanbinh542007@gmail.com", new StudentPolicy()));

        ctx.memberRepository.save(new Member(
                "Tran Thi Mai", "0912345678", "mai.tran@gmail.com", new RegularPolicy()));

        ctx.memberRepository.save(new Member(
                "Le Hoang Nam", "0987654321", "nam.le@fpt.edu.vn", new StudentPolicy()));

        ctx.memberRepository.save(new Member(
                "Pham Quoc Anh", "0909123456", "anh.pham@gmail.com", new PremiumPolicy()));

        ctx.memberRepository.save(new Member(
                "Vo Minh Quan", "0933555777", "quan.vo@uni.edu.vn", new StudentPolicy()));

        ctx.memberRepository.save(new Member(
                "Nguyen Thu Ha", "0966888999", "ha.nguyen@gmail.com", new RegularPolicy()));

        ctx.memberRepository.save(new Member(
                "Do Tuan Kiet", "0977111222", "kiet.do@gmail.com", new PremiumPolicy()));

        ctx.memberRepository.save(new Member(
                "Hoang Gia Bao", "0944333222", "bao.hoang@fpt.edu.vn", new StudentPolicy()));

        ctx.memberRepository.save(new Member(
                "Bui Thanh Son", "0388888888", "son.bui@gmail.com", new RegularPolicy()));
    }
}
