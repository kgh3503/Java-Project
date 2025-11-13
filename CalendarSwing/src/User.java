public class User {
    private int userId;           // PK (Primary Key) - 사용자 식별자
    private String username;      // 사용자 아이디
    private String passwordHash;  // 비밀번호 (해시 처리된 값)

    // 회원가입 및 새로운 사용자 생성 시 사용
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
    
    // DB에서 데이터 로드 시 사용 (ID 포함)
    public User(int userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // DAO 및 다른 클래스에서 필요한 Getter 메서드
    public int getUserId() { 
        return userId; 
    }
    
    public String getUsername() { 
        return username; 
    }
    
    public String getPasswordHash() { 
        return passwordHash; 
    }
    
    // userId Setter는 DB에서 ID를 할당받을 때 필요할 수 있음
    public void setUserId(int userId) {
        this.userId = userId;
    }
}