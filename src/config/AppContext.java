package config;

import models.Book;
import models.Member;
import repositories.*;
import services.*;
import utilities.InputController;
import utilities.TableRenderer;

import java.util.Scanner;

public class AppContext {
    public static final TableRenderer<Member> MEMBER_TABLE_RENDERER = new utilities.renderers.MemberTableRenderer();
    public static final TableRenderer<Book> BOOK_TABLE_RENDERER = new utilities.renderers.BookTableRenderer();

    public final Scanner scanner;
    public final InputController input;

    public final BookRepository bookRepository;
    public final MemberRepository memberRepository;
    public final BorrowTransactionRepository txRepository;

    public final BookService bookService;
    public final MemberService memberService;
    public final BorrowTransactionService transactionService;
    public final ReportService reportService;

    public AppContext() {
        scanner = new Scanner(System.in);
        input = new InputController(scanner);

        bookRepository = new BookRepository();
        memberRepository = new MemberRepository();
        txRepository = new BorrowTransactionRepository();

        bookService = new BookService(bookRepository);
        memberService = new MemberService(memberRepository);
        transactionService = new BorrowTransactionService(txRepository);
        reportService = new ReportService(transactionService, bookRepository);
    }
}
