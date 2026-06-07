package service;

import models.Member;
import utilities.UIRender;
import java.util.ArrayList;

public class MemberList {
    private ArrayList<Member> members = new ArrayList<>();

    public boolean registerMember(Member member) {
        if (member == null) {
            UIRender.renderError("Cannot register empty member data!");
            return false;
        }

        // CHỐT CHẶN XÁC THỰC: Chống trùng lặp mã ID Thành viên trong hệ thống
        if (searchById(member.getMemberId()) != null) {
            UIRender.renderError("Member ID '" + member.getMemberId() + "' is already registered!");
            return false;
        }

        members.add(member);
        return true;
    }

    public Member searchById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (Member m : members) {
            if (m.getMemberId().equalsIgnoreCase(id.trim())) {
                return m;
            }
        }
        return null;
    }

    public void displayAll() {
        UIRender.renderHeader("Member Directory");
        if (members.isEmpty()) {
            System.out.println("No registered members found in the system.");
            return;
        }
        for (Member m : members) {
            System.out.printf("ID: %s | Name: %s | Phone: %s | Active Loans: %d/%d\n",
                    m.getMemberId(), m.getName(), m.getPhone(), m.getBorrowedCount(), m.getBorrowLimit());
        }
    }
}