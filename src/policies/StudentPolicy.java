package policies;

public class StudentPolicy extends RegularPolicy {
    @Override public String getTierName() { return "Student"; }
}
