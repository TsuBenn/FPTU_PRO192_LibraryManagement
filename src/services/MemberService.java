package services;

import exceptions.DuplicateEntryException;
import exceptions.EntityNotFoundException;
import exceptions.InvalidOperationException;
import models.Member;
import repositories.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void registerMember(Member member) throws DuplicateEntryException {
        if (isPhoneDuplicate(member.getPhone()))
            throw new DuplicateEntryException("Phone", member.getPhone());
        if (isEmailDuplicate(member.getEmail()))
            throw new DuplicateEntryException("Email", member.getEmail());
        memberRepository.save(member);
    }

    public void updateMember(Member oldMember, Member newMember)
            throws EntityNotFoundException, DuplicateEntryException {
        if (oldMember == null)
            throw new EntityNotFoundException("Member", "unknown");
        if (isPhoneDuplicateExcluding(newMember.getPhone(), oldMember.getId()))
            throw new DuplicateEntryException("Phone", newMember.getPhone());
        if (isEmailDuplicateExcluding(newMember.getEmail(), oldMember.getId()))
            throw new DuplicateEntryException("Email", newMember.getEmail());
        memberRepository.update(oldMember, newMember);
    }

    public void deleteMember(Member member, int activeLoans)
            throws InvalidOperationException {
        if (activeLoans > 0)
            throw new InvalidOperationException(
                    "Cannot delete member with " + activeLoans + " active loan(s).");
        memberRepository.delete(member);
    }

    private boolean isPhoneDuplicate(String phone) {
        return memberRepository.findAll().stream()
                .anyMatch(m -> m.getPhone().trim().equals(phone.trim()));
    }

    private boolean isEmailDuplicate(String email) {
        return memberRepository.findAll().stream()
                .anyMatch(m -> m.getEmail().equalsIgnoreCase(email.trim()));
    }

    private boolean isPhoneDuplicateExcluding(String phone, String excludeId) {
        return memberRepository.findAll().stream()
                .anyMatch(m -> !m.getId().equals(excludeId)
                        && m.getPhone().trim().equals(phone.trim()));
    }

    private boolean isEmailDuplicateExcluding(String email, String excludeId) {
        return memberRepository.findAll().stream()
                .anyMatch(m -> !m.getId().equals(excludeId)
                        && m.getEmail().equalsIgnoreCase(email.trim()));
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(String id) {
        return memberRepository.findById(id);
    }

    public List<Member> getByQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMembers();
        }
        String lowerCaseQuery = query.toLowerCase();
        return memberRepository.findAll().stream()
                .filter(m -> m.getId().toLowerCase().contains(lowerCaseQuery)
                        || m.getName().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
    }
}
