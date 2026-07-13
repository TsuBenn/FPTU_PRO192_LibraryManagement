package controllers.handlers;

import config.AppContext;
import exceptions.AbortInputException;
import exceptions.BorrowPolicyException;
import exceptions.LibraryException;
import models.Book;
import models.BorrowTransaction;
import models.Member;
import models.TransactionStatus;
import services.BookService;
import services.BorrowTransactionService;
import services.MemberService;
import utilities.ConfirmResult;
import utilities.InputController;
import utilities.TableRenderer;
import utilities.UIRender;

import java.time.LocalDate;
import java.util.List;

public class CirculationHandler {
    private static final TableRenderer<BorrowTransaction> TX_RENDERER =
            new utilities.renderers.TransactionTableRenderer();

    private final BookService bookService;
    private final MemberService memberService;
    private final BorrowTransactionService transactionService;
    private final InputController input;

    public CirculationHandler(AppContext ctx) {
        this.bookService = ctx.bookService;
        this.transactionService = ctx.transactionService;
        this.memberService = ctx.memberService;
        this.input = ctx.input;
        transactionService.updateOverdueTransactions();
    }

    public void handleBorrow() {
        UIRender.clearScreen();
        try {
            Member member = input.searchAndSelectMember(memberService.getByQuery(
                    input.getString("Enter member search criteria (Name/ID): ")
            ));
            if (member == null) return;

            Book book = input.searchAndSelectBook(
                    bookService.getByQuery(
                            input.getString("Enter book search criteria (Title/Author/ID): ")
                    )
            );
            if (book == null) return;

            BorrowTransaction tx = new BorrowTransaction(
                    member.getId(), member.getName(),
                    book.getId(), book.getTitle(),
                    LocalDate.now());

            ConfirmResult confirm = input.getConfirmation();
            if (confirm != ConfirmResult.CONFIRM)
                return;
            transactionService.executeCheckout(member, book, tx);
            UIRender.renderSuccess("Book borrowed successfully.");
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        } catch (BorrowPolicyException e) {
            UIRender.renderError(e.getMessage());
        }
        UIRender.pauseEnter();
    }

    public void handleReturn() {
        UIRender.clearScreen();
        try {
            Member member = input.searchAndSelectMember(memberService.getByQuery(
                    input.getString("Enter member search criteria (Name/ID): ")
            ));
            if (member == null) return;

            List<BorrowTransaction> activeLoans =
                    transactionService.getActiveLoansByMember(member.getId());
            if (activeLoans.isEmpty()) {
                UIRender.renderError("No outstanding loans found.");
                UIRender.pauseEnter();
                return;
            }

            UIRender.renderTable(activeLoans, "Active Loans", TX_RENDERER);
            BorrowTransaction targetTx = input.selectTransactionFromList(activeLoans);
            if (targetTx == null) return;
            ConfirmResult confirm = input.getConfirmation();
            if (confirm != ConfirmResult.CONFIRM)
                return;

            Book book = bookService.getBookById(targetTx.getBookId());
            double fines = transactionService.processReturn(targetTx, member, book, LocalDate.now());

            if (fines == -1.0) {
                UIRender.renderError("Transaction is not currently active.");
            } else if (fines > 0) {
                UIRender.renderFineStatement(0.0, fines, fines);
            } else {
                UIRender.renderSuccess("Book returned on schedule.");
            }
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        }
        UIRender.pauseEnter();
    }

    public void handleLost() {
        UIRender.clearScreen();
        try {
            Member member = input.searchAndSelectMember(memberService.getByQuery(
                    input.getString("Enter member search criteria (Name/ID): ")
            ));
            if (member == null) return;

            List<BorrowTransaction> activeLoans =
                    transactionService.getActiveLoansByMember(member.getId());
            if (activeLoans.isEmpty()) {
                UIRender.renderError("No outstanding loans found.");
                UIRender.pauseEnter();
                return;
            }

            UIRender.renderTable(activeLoans, "Active Loans", TX_RENDERER);
            BorrowTransaction targetTx = input.selectTransactionFromList(activeLoans);
            if (targetTx == null) return;
            ConfirmResult confirm = input.getConfirmation();
            if (confirm != ConfirmResult.CONFIRM)
                return;

            Book book = bookService.getBookById(targetTx.getBookId());

            double[] receipt = transactionService.reportMissing(targetTx, member, book);
            if (receipt == null) {
                UIRender.renderError("Asset must be currently out on loan.");
            } else {
                UIRender.clearScreen();
                UIRender.renderFineStatement(receipt[0], receipt[1], receipt[2]);
            }
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        }
        UIRender.pauseEnter();
    }

    public void handlePayment() {
        Member member = input.searchAndSelectMember(memberService.getByQuery(
                input.getString("Enter member search criteria (Name/ID): ")
        ));
        if (member == null) return;

        List<BorrowTransaction> requiredPaidTransactions =
                transactionService.getRequiredPaidTransactionsByMember(member.getId());

        if (requiredPaidTransactions == null) {
            UIRender.renderSuccess(member.getName() + " has no fine to resolve!");
            return;
        }

        UIRender.renderTable(requiredPaidTransactions, "Required pay", TX_RENDERER);
        if (requiredPaidTransactions.isEmpty())
            return;
        try {
            while (true) {
                UIRender.renderTable(requiredPaidTransactions, "Required pay", TX_RENDERER);
                int resolveIndex = input.getInt("Enter the resolve index. Press :q to return: ");
                if (resolveIndex > requiredPaidTransactions.size())
                    throw new LibraryException("Invalid index!");
                BorrowTransaction transaction = requiredPaidTransactions.get(resolveIndex - 1);
                if (transaction.getTransactionStatus() == TransactionStatus.MISSING_AND_IS_PAID
                || transaction.getTransactionStatus() == TransactionStatus.IS_PAID) {
                    UIRender.renderSuccess("This transaction has been resolve!");
                    continue;
                }

                transactionService.processPayment(transaction, memberService.getMemberById(transaction.getMemberId()));
            }
        } catch (AbortInputException | LibraryException e) {
            UIRender.renderError(e.getMessage());
        }
    }
}
