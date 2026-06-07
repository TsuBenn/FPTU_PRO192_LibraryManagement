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
        if (findMemberById(member.getId()) != null) return false;
        if (isContactDuplicate(member.getPhone(), member.getEmail())) return false;
        members.add(member);
        return true;
    }

    public boolean isContactDuplicate(String phone, String email) {
        for (Member m : members) {
            if (m.getPhone().trim().equals(phone.trim()) || m.getEmail().equalsIgnoreCase(email.trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean removeMember(String id) {
        Member m = findMemberById(id);
        if (m != null) {
            members.remove(m);
            return true;
        }
        return false;
    }

    public Member findMemberById(String id) {
        for (Member m : members) {
            if (m.getId().equalsIgnoreCase(id.trim())) return m;
        }
        return null;
    }

    public List<Member> searchMembers(String query) {
        List<Member> matches = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        for (Member m : members) {
            if (m.getId().toLowerCase().contains(lowerQuery) ||
                m.getName().toLowerCase().contains(lowerQuery)) {
                matches.add(m);
            }
        }
        return matches;
    }

    public List<Member> getAllMembers() { return members; }
}
