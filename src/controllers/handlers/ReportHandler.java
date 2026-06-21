package controllers.handlers;

import com.sun.javaws.security.AppPolicy;
import config.AppContext;
import exceptions.AbortInputException;
import models.Book;
import models.BorrowTransaction;
import models.Member;
import repositories.MemberRepository;
import services.MemberService;
import services.ReportService;
import utilities.*;

import java.util.List;
import java.util.stream.Collectors;

public class ReportHandler {
    private static final TableRenderer<BorrowTransaction> TX_RENDERER =
            new utilities.renderers.TransactionTableRenderer();

    private final ReportService reportService;
    private final MemberService memberService;
    private final InputController input;

    public ReportHandler(AppContext ctx) {
        this.reportService = ctx.reportService;
        this.memberService = ctx.memberService;
        this.input = ctx.input;
    }

    public void handle() {
        boolean active = true;
        String[] choices = {
                "Member Borrowing History", "All Transactions Log",
                "Overdue Books Report", "Most Popular Books"
        };
        while (active) {
            UIRender.clearScreen();
            UIRender.renderMenu("Analytics Dashboard", choices);
            switch (input.getChoice("Select: ", 4)) {
                case 1: handleMemberHistory(); break;
                case 2: handleMasterLog(); break;
                case 3: handleOverdue(); break;
                case 4: handlePopularBooks(); break;
                case 0: active = false; break;
            }
        }
    }

    private void handleMemberHistory() {
        UIRender.clearScreen();
        try {
            Member member = input.searchAndSelectMember(
                    memberService.getByQuery(
                            input.getString("Enter member info to find: ")
                    )
            );
            if (member != null) {
                UIRender.renderTable(reportService.getSortedMemberHistory(member),
                        "Member History", TX_RENDERER);
            }
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        }
        UIRender.pauseEnter();
    }

    private void handleMasterLog() {
        UIRender.clearScreen();
        UIRender.renderTable(reportService.getMasterLogSorted(), "Transaction Log", TX_RENDERER);
        UIRender.pauseEnter();
    }

    private void handleOverdue() {
        UIRender.clearScreen();
        UIRender.renderTable(reportService.getOverdueAssets(), "Overdue Report", TX_RENDERER);
        UIRender.pauseEnter();
    }

    private void handlePopularBooks() {
        UIRender.clearScreen();
        try {
            int cap = input.getInt("Enter display cap count: ");
            UIRender.renderTable(reportService.getPopularBooks(cap), "Popular Books", AppContext.BOOK_TABLE_RENDERER);
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        }
        UIRender.pauseEnter();
    }
}
