package config;

import models.Book;
import models.BorrowTransaction;
import models.Member;
import repositories.BookRepository;
import repositories.BorrowTransactionRepository;
import repositories.MemberRepository;
import repositories.io.Database;
import services.BookService;
import services.BorrowTransactionService;
import services.MemberService;
import services.ReportService;
import utilities.IDManager;
import utilities.InputController;
import utilities.TableRenderer;

import java.util.Scanner;
import java.util.stream.Collectors;

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

    private final String bookStoragePath = "book.txt";
    private final String memberStoragePath = "member.txt";
    private final String transactionStoragePath = "transaction.txt";

    public void syncIDCounter() {
        IDManager.bookIDGenerator.sync(
                bookRepository.findAll()
                        .stream()
                        .map(Book::getId)
                        .collect(Collectors.toList())
        );

        IDManager.memberIDGenerator.sync(
                memberRepository.findAll()
                        .stream()
                        .map(Member::getId)
                        .collect(Collectors.toList())
        );

        IDManager.transactionIDGenerator.sync(
                txRepository.findAll()
                        .stream().map(BorrowTransaction::getTransactionId)
                        .collect(Collectors.toList())
        );
    }

    private final Database<Book> bookDatabase = new Database<>(
            bookStoragePath, DataTransfer.BOOK_WRITER, DataTransfer.BOOK_READER
    );
    private final Database<Member> memberDatabase = new Database<>(
            memberStoragePath, DataTransfer.MEMBER_WRITER,
            DataTransfer.MEMBER_READER
    );
    private final Database<BorrowTransaction> borrowTransactionDatabase = new Database<>(
            transactionStoragePath, DataTransfer.TRANSACTION_WRITER,
            DataTransfer.TRANSACTION_READER
    );

    public AppContext() {
        scanner = new Scanner(System.in);
        input = new InputController(scanner);

        bookRepository = new BookRepository(bookDatabase);
        memberRepository = new MemberRepository(memberDatabase);
        txRepository = new BorrowTransactionRepository(borrowTransactionDatabase);

        bookService = new BookService(bookRepository);
        memberService = new MemberService(memberRepository);
        transactionService = new BorrowTransactionService(txRepository, bookRepository, memberRepository);
        reportService = new ReportService(transactionService, bookRepository);

        syncIDCounter();
        if (memberRepository.findAll().isEmpty()
                && bookRepository.findAll().isEmpty()
                && txRepository.findAll().isEmpty()) {
            DataSeeder.seed(this);
        }
    }
}
