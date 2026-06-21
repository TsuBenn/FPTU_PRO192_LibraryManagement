package utilities.renderers;

import models.Member;
import utilities.TableRenderer;

public class MemberTableRenderer implements TableRenderer<Member> {
    @Override
    public String[] getHeaders() {
        return new String[]{"ID", "Name", "Phone", "Email", "Fine", "Limit", "Tier"};
    }

    @Override
    public String[] toRow(Member m) {
        return new String[]{
                m.getId(), m.getName(), m.getPhone(), m.getEmail(),
                String.valueOf(m.getFine()),
                String.valueOf(m.getBorrowLimit()),
                m.getTierName()
        };
    }
}
