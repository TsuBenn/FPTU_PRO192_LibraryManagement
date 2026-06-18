package repositories;

import java.util.ArrayList;
import java.util.List;
import models.Member;

public class MemberRepository {
    private final List<Member> database = new ArrayList<>();

    public void save(Member member) {
        database.add(member);
    }

    public void delete(Member member) {
        database.remove(member);
    }

    public void update(Member oldMember, Member newMember) {
        int index = database.indexOf(oldMember);
        if (index != -1) {
            database.set(index, newMember);
        }
    }

    public List<Member> findAll() {
        return database;
    }

    public Member findById(String id) {
        return database.stream()
                .filter(m -> m.getId().equalsIgnoreCase(id.trim()))
                .findFirst()
                .orElse(null);
    }
}