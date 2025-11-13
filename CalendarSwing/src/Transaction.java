public class Transaction {
    private int transactionId; // PK - 거래 식별자
    private int userId;        // FK - 사용자 ID
    private String date;       // 거래 날짜 ("yyyy-MM-dd")
    private String type;       // 유형 ("수입" 또는 "지출")
    private double amount;     // 금액
    private String category;   // 카테고리
    private String content;    // 상세 내용 (메모 기능 포함)

    // ✅ DB 저장 전: ID가 없는 생성자
    public Transaction(int userId, String date, String type, double amount, String category, String content) {
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
    }

    // ✅ DB에서 불러올 때 사용: ID 포함 생성자
    public Transaction(int transactionId, int userId, String date, String type, double amount, String category, String content) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
    }

    // Getter 메서드
    public int getTransactionId() { return transactionId; }
    public int getUserId() { return userId; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
}