package services;

import java.util.List;
import java.util.stream.Collectors;
import models.Member;
import repositories.MemberRepository;
import utilities.InputController;
import utilities.UIRender;

public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public boolean registerMember(Member member) {
        if (memberRepository.findById(member.getId()) != null) return false;
        if (isContactDuplicate(member.getPhone(), member.getEmail())) return false;
        memberRepository.save(member);
        return true;
    }

    public boolean updateMember(Member oldMember, Member newMember) {
        if (oldMember == null || newMember == null) return false;
        memberRepository.update(oldMember, newMember);
        return true;
    }

    private boolean isContactDuplicate(String phone, String email) {
        return memberRepository.findAll().stream()
                .anyMatch(m -> m.getPhone().trim().equals(phone.trim()) || m.getEmail().equalsIgnoreCase(email.trim()));
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(String id) {
        return memberRepository.findById(id);
    }

    public static Member searchAndSelectMember(MemberRepository repository) {
        String query = InputController.getString("Enter Member Identity Search Query (Name/ID): ");
        List<Member> matches = repository.findAll().stream()
                .filter(m -> m.getId().toLowerCase().contains(query.toLowerCase()) ||
                        m.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            UIRender.renderError("Zero direct matching member records found.");
            UIRender.pauseEnter();
            return null;
        }

        UIRender.renderMemberContentTable(matches);
        return matches.get(0);
    }
}