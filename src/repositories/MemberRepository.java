package repositories;

import java.util.*;

import models.Member;

public class MemberRepository {
    private final Map<String, Member> database = new HashMap<>();

    public void save(Member member) {
        database.put(member.getId(), member);
    }

    public void delete(Member member) {
        database.remove(member.getId());
    }

    public void update(Member oldMember, Member newMember) {
        if (database.containsKey(oldMember.getId())) {
            database.put(oldMember.getId(), newMember);
        }
    }

    public List<Member> findAll() {
        return new ArrayList<>(database.values());
    }

    public Member findById(String id) {
        return database.get(id);
    }
}
