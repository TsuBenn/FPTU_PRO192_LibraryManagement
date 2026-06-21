package controllers.handlers;

import config.AppContext;
import exceptions.AbortInputException;
import exceptions.DuplicateEntryException;
import exceptions.InvalidOperationException;
import exceptions.LibraryException;
import models.Member;
import repositories.MemberRepository;
import services.BorrowTransactionService;
import services.MemberService;
import utilities.*;

import java.util.List;
import java.util.stream.Collectors;

public class MemberHandler {
    private final MemberService memberService;
    private final BorrowTransactionService transactionService;
    private final BorrowTransactionService borrowTransactionService;
    private final InputController input;

    public MemberHandler(AppContext ctx) {
        this.memberService = ctx.memberService;
        this.transactionService = ctx.transactionService;
        this.borrowTransactionService = ctx.transactionService;
        this.input = ctx.input;
    }

    public void handle() {
        boolean active = true;
        String[] choices = {"Register Member", "Remove Member", "Update Member", "List Members"};
        while (active) {
            UIRender.clearScreen();
            UIRender.renderMenu("Member Management", choices);
            switch (input.getChoice("Select: ", 4)) {
                case 1: handleAdd(); break;
                case 2: handleDelete(); break;
                case 3: handleUpdate(); break;
                case 4: handleList(); break;
                case 0: active = false; break;
            }
        }
    }

    private void handleAdd() {
        UIRender.clearScreen();
        try {
            Member member = input.inputNewMemberData();
            UIRender.renderMemberPreview(member);
            ConfirmResult result = input.getConfirmation();
            if (result == ConfirmResult.CONFIRM) {
                memberService.registerMember(member);
                UIRender.renderSuccess("Member registered.");
            } else if (result == ConfirmResult.EDIT) {
                member = input.updateMemberModelFields(member);
                UIRender.renderMemberPreview(member);
                if (input.getConfirmation() == ConfirmResult.CONFIRM) {
                    memberService.registerMember(member);
                    UIRender.renderSuccess("Member registered.");
                }
            }
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        } catch (DuplicateEntryException e) {
            UIRender.renderError(e.getMessage());
        }
        UIRender.pauseEnter();
    }

    private void handleDelete() {
        UIRender.clearScreen();
        try {
            Member target = input.searchAndSelectMember(memberService.getByQuery(
                    input.getString("Enter member search criteria (Name/ID): ")
            ));
            if (target == null) return;
            int activeLoans = transactionService.countActiveLoans(target.getId());
            memberService.deleteMember(target, activeLoans);
            borrowTransactionService.markTransactionsAsMemberRemoved(target.getId());
            UIRender.renderSuccess("Member removed.");
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        } catch (InvalidOperationException e) {
            UIRender.renderError(e.getMessage());
        }
        UIRender.pauseEnter();
    }

    private void handleUpdate() {
        UIRender.clearScreen();
        try {
            Member oldMember = input.searchAndSelectMember(memberService.getByQuery(
                    input.getString("Enter member search criteria (Name/ID): ")
            ));
            if (oldMember == null) return;

            Member newMember = input.updateMemberModelFields(oldMember);
            UIRender.renderMemberPreview(newMember);
            ConfirmResult result = input.getConfirmation();
            if (result == ConfirmResult.CONFIRM) {
                memberService.updateMember(oldMember, newMember);
                UIRender.renderSuccess("Member updated.");
            } else if (result == ConfirmResult.EDIT) {
                newMember = input.updateMemberModelFields(newMember);
                memberService.updateMember(oldMember, newMember);
                UIRender.renderSuccess("Member updated.");
            }
        } catch (AbortInputException e) {
            UIRender.renderError("Cancelled. Returning to menu.");
        } catch (LibraryException e) {
            UIRender.renderError(e.getMessage());
        }
        UIRender.pauseEnter();
    }

    private void handleList() {
        UIRender.clearScreen();
        UIRender.renderTable(memberService.getAllMembers(), "Member Directory", AppContext.MEMBER_TABLE_RENDERER);
        UIRender.pauseEnter();
    }
}
