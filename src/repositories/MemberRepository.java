package repositories;

import models.Member;
import repositories.io.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberRepository {
    private final Map<String, Member> database = new HashMap<>();
    private final Database<Member> memberDatabase;

    public MemberRepository(Database<Member> memberDatabase) {
        this.memberDatabase = memberDatabase;
        memberDatabase.loadContent().forEach(
                d -> database.put(d.getId(), d)
        );
    }


    public void save(Member member) {
        database.put(member.getId(), member);
        memberDatabase.save(database);
    }

    public void delete(Member member) {
        database.remove(member.getId());
        memberDatabase.save(database);
    }

    public void update(Member oldMember, Member newMember) {
        System.out.println("Chạy hàm update số 2 của member database");
        if (database.containsKey(oldMember.getId())) {
            System.out.println("Đã tìm thấy và update!");
            database.put(oldMember.getId(), newMember);
            memberDatabase.save(database);
        }
    }

    public void update(Member member) {
        System.out.println("Chạy hàm update số 1 của member database");
        if (database.containsKey(member.getId())) {
            System.out.println("Đã tìm thấy và update!");
            database.put(member.getId(), member);
            memberDatabase.save(database);
        }
    }

    public List<Member> findAll() {
        return new ArrayList<>(database.values());
    }

    public Member findById(String id) {
        return database.get(id);
    }
}

