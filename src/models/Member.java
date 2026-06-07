package models;

import sun.plugin2.message.RemoteCAContextIdMessage;
import utilities.IDManager;

public class Member {

    private final String id;
    private String name;
    private String phone;
    private String email;

    public Member(String name, String phone, String email) {
        id = IDManager.memberIDGenerator.newID();
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Milestone 3 Hook: dynamic limit extraction instead of hardcoding '3' in loops
    public int getBorrowLimit() {
        return 3; 
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

}
