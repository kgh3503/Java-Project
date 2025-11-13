import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDao {

    /**
     * 새로운 목표를 DB에 저장합니다. (Create)
     */
    public boolean addGoal(Goal goal) {
        String sql = "INSERT INTO goals (user_id, type, category, target_amount, year, month) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, goal.getUserId());
            pstmt.setString(2, goal.getType());
            // 카테고리가 null이면 DB에도 NULL로 저장
            pstmt.setString(3, goal.getCategory()); 
            pstmt.setDouble(4, goal.getTargetAmount());
            pstmt.setInt(5, goal.getYear());
            pstmt.setInt(6, goal.getMonth());
            
            int affectedRows = pstmt.executeUpdate();

            // 생성된 ID 가져와서 Goal 객체에 설정 (선택 사항)
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        // goal.setGoalId(rs.getInt(1)); // Goal 클래스에 setGoalId가 있다면 사용
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            // UNIQUE KEY 제약 조건 위반 시 (동일한 목표가 이미 존재)
            if (e.getSQLState().equals("23000")) {
                System.err.println("DB 목표 저장 오류: 동일한 목표가 이미 존재합니다. " + e.getMessage());
                return false;
            }
            System.err.println("DB 목표 저장 오류: " + e.getMessage());
            return false;
        }
    }

    /**
     * 특정 월의 모든 목표를 조회합니다. (Read)
     */
    public List<Goal> getGoalsByMonth(int userId, int year, int month) {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT id, user_id, type, category, target_amount, year, month FROM goals WHERE user_id = ? AND year = ? AND month = ?";
        
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Goal goal = new Goal(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("target_amount"),
                        rs.getInt("year"),
                        rs.getInt("month")
                    );
                    goals.add(goal);
                }
            }
        } catch (SQLException e) {
            System.err.println("월별 목표 조회 중 DB 오류 발생: " + e.getMessage());
        }
        return goals;
    }
}