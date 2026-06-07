package managers;

import java.util.ArrayList;
import java.util.List;
import models.Member;

public class MemberManager {
    private List<Member> members;

    public MemberManager() {
        this.members = new ArrayList<>();
    }

    public boolean addMember(Member member) {
        if (findMemberById(member.getId()) != null) {
            return false;
        }
        members.add(member);
        return true;
    }

    public Member findMemberById(String id) {
        for (Member m : members) {
            if (m.getId().equalsIgnoreCase(id)) {
                return m;
            }
        }
        return null;
    }

    public List<Member> getAllMembers() {
        return members;
    }
}
