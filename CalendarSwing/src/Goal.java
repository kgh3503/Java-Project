public class Goal {
    private int goalId;        // PK: 목표 식별자
    private int userId;        // FK: 사용자 ID
    private String type;       // 목표 유형 ("수입" 또는 "지출")
    private String category;   // 카테고리 (null 또는 ""이면 전체 금액 목표)
    private double targetAmount; // 목표 금액
    private int year;          // 목표 연도
    private int month;         // 목표 월 (월별 목표로 가정)

    /**
     * 새로운 목표를 생성합니다. (goalId는 DB에서 자동 할당된다고 가정)
     */
    public Goal(int userId, String type, String category, double targetAmount, int year, int month) {
        this.userId = userId;
        this.type = type;
        this.category = (category == null || category.trim().isEmpty()) ? null : category;
        this.targetAmount = targetAmount;
        this.year = year;
        this.month = month;
    }

    // DB에서 불러올 때 사용
    public Goal(int goalId, int userId, String type, String category, double targetAmount, int year, int month) {
        this(userId, type, category, targetAmount, year, month);
        this.goalId = goalId;
    }

    // Getter 메서드
    public int getGoalId() { return goalId; }
    public int getUserId() { return userId; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getTargetAmount() { return targetAmount; }
    public int getYear() { return year; }
    public int getMonth() { return month; }

    // 목표 정보 출력 (디버깅용)
    @Override
    public String toString() {
        String catInfo = (category == null) ? "전체" : category;
        return String.format("%d년 %d월 %s 목표 (%s): %,.0f원", 
            year, month, type, catInfo, targetAmount);
    }
}