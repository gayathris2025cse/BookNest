public class Member {
    private String memberId;
    private String name;
    private int borrowedCount;

    public Member(String memberId, String name, int borrowedCount) {
        this.memberId = memberId;
        this.name = name;
        this.borrowedCount = borrowedCount;
    }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getBorrowedCount() { return borrowedCount; }
    public void setBorrowedCount(int borrowedCount) { this.borrowedCount = borrowedCount; }
    public boolean canBorrow() { return borrowedCount < 3; }
}
