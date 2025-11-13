import java.util.Map;
import java.util.List;

/**
 * 목표 설정 및 거래 내역 대비 진행 상황을 확인하는 로직을 담당합니다.
 */
public class GoalManager {
    
    // 목표 관리는 기존의 TransactionDao와 상호작용해야 합니다.
    private final TransactionDao transactionDao; 
    private final GoalDao goalDao;

    public GoalManager(TransactionDao transactionDao, GoalDao goalDao) {
        this.transactionDao = transactionDao;
        this.goalDao = goalDao;
        // 실제 운영 환경에서는 Goal을 저장/조회하는 GoalDao가 필요합니다.
    }
    
    /**
     * [Placeholder] 목표를 설정하고 Goal 객체를 생성합니다.
     * (실제로는 이 메서드가 DB에 Goal 객체를 저장해야 합니다.)
     */
    public boolean setGoal(int userId, String type, String category, double targetAmount, int year, int month) {
        Goal newGoal = new Goal(userId, type, category, targetAmount, year, month);
        System.out.println("[GoalManager] 목표 설정 완료: " + newGoal.toString());
        // 🚨 이 위치에 GoalDao.saveGoal(newGoal) 로직이 들어가야 합니다.
        return goalDao.addGoal(newGoal);
    }
    
    public List<Goal> getGoalsByMonth(int userId, int year, int month) {
        return goalDao.getGoalsByMonth(userId, year, month);
    }

    /**
     * 특정 목표에 대한 현재까지의 지출/수입 금액을 계산합니다.
     * (TransactionDao의 기존 월별 합계 조회 메서드를 재활용합니다.)
     */
    public double checkProgress(Goal goal) {
        if (goal.getCategory() == null) {
            // 1. 카테고리가 없는 경우: 월별 전체 수입/지출 목표
            Map<String, Double> summary = transactionDao.getMonthlySummary(
                goal.getUserId(), goal.getYear(), goal.getMonth()
            );
            // 목표 유형(수입/지출)에 해당하는 총액 반환
            return summary.getOrDefault(goal.getType(), 0.0);
            
        } else {
            // 2. 카테고리가 있는 경우: 카테고리별 목표
            Map<String, Double> categorySummary = transactionDao.getCategorySummary(
                goal.getUserId(), goal.getYear(), goal.getMonth(), goal.getType()
            );
            // 해당 카테고리에 해당하는 총액 반환
            return categorySummary.getOrDefault(goal.getCategory(), 0.0);
        }
    }
    
    /**
     * 목표 대비 현재 달성률을 퍼센트(%)로 반환합니다.
     * (수입 목표는 달성률이 높을수록 좋고, 지출 목표는 낮을수록 좋습니다.)
     */
    public double getAchievementRate(Goal goal) {
        double progress = checkProgress(goal);
        
        // 목표 금액이 0이거나 음수이면 계산 불가 (또는 100% 반환)
        if (goal.getTargetAmount() <= 0) {
            return 0.0; 
        }

        double rate = (progress / goal.getTargetAmount()) * 100.0;
        
        if (goal.getType().equals("지출")) {
            // 지출 목표: 달성률 100%는 목표 금액과 같은 지출을 의미. 
            // 100%를 초과하면 목표 초과입니다.
            return rate;
        } else {
            // 수입 목표: 목표 수입액 대비 실제 수입액
            return rate;
        }
    }
}