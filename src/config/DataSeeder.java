package config;

import models.*;
import policies.*;

public class DataSeeder {
    public static void seed(AppContext ctx) {
        ctx.bookRepository.save(new Book(
                "The Great Gatsby", "F. Scott Fitzgerald", "Classic", 1925, 100_000, 5, BookType.NOVEL));
        ctx.bookRepository.save(new Book(
                "1984", "George Orwell", "Dystopian", 1949, 200_000, 3, BookType.NOVEL));
        ctx.bookRepository.save(new Book(
                "How to get 10 mark in PRO_192", "FPT Instructor", "Academic", 2024, 300_000, 3, BookType.TEXTBOOK));

        ctx.memberRepository.save(new Member(
                "Benn", "0123456789", "benn@uni.edu.vn", new RegularPolicy()));
        ctx.memberRepository.save(new Member(
                "Nguyen Van Binh", "0373450305", "nguyenvanbinh542007@gmail.com", new StudentPolicy()));
    }
}
