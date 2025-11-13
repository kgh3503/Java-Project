import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDao {

    /**
     * [이름 변경] 새로운 거래 내역을 DB에 저장합니다. (Create)
     */
    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (user_id, date, type, amount, category, content) VALUES (?, ?, ?, ?, ?, ?)";
        
        // 🚨 [수정] getConnection() -> connect()
        try (Connection conn = DatabaseManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 🚨 [수정] Transaction.java 파일에 맞게 getter 사용
            pstmt.setInt(1, transaction.getUserId());
            pstmt.setString(2, transaction.getDate());
            pstmt.setString(3, transaction.getType());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setString(5, transaction.getCategory());
            pstmt.setString(6, transaction.getContent());
            
            pstmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println("DB 거래 저장 오류: " + e.getMessage());
            return false;
        }
    }

    /**
     * [신규 추가] 특정 사용자의 특정 날짜(YYYY-MM-DD)에 해당하는 모든 거래 내역을 조회합니다.
     */
    public List<Transaction> getTransactionsByDate(int userId, String date) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT id, user_id, date, type, amount, category, content "
                   + "FROM transactions "
                   + "WHERE user_id = ? AND date = ? "
                   + "ORDER BY id ASC";

        // 🚨 [수정] getConnection() -> connect()
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, date);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 🚨 [수정] Transaction.java의 ID 포함 생성자에 맞게 수정
                    Transaction transaction = new Transaction(
                        rs.getInt("id"), // "id"가 DB 컬럼명이라고 가정
                        rs.getInt("user_id"),
                        rs.getString("date"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("content")
                    );
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("특정 날짜 거래 조회 중 DB 오류: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * [신규 추가] 거래 ID를 기준으로 특정 거래 내역 1건을 삭제합니다. (Delete)
     */
    public boolean deleteTransactionById(int transactionId) {
        // 🚨 [수정] DB의 PK 컬럼이 'transactionId'가 아닌 'id'일 수 있음
        // 🚨 만약 DB 컬럼명이 'transactionId'가 맞다면 "id = ?" 를 "transactionId = ?"로 변경하세요.
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        // 🚨 [수정] getConnection() -> connect()
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, transactionId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("DB 거래 삭제 오류: " + e.getMessage());
            return false;
        }
    }

    /**
     * [기존 유지] 특정 사용자의 특정 월에 해당하는 모든 거래 내역을 조회합니다. (Read)
     */
    public List<Transaction> findByMonthAndUser(int userId, int year, int month) {
        List<Transaction> transactions = new ArrayList<>();
        String monthPattern = String.format("%d-%02d-%%", year, month); 
        String sql = "SELECT id, user_id, date, type, amount, category, content "
                   + "FROM transactions "
                   + "WHERE user_id = ? AND date LIKE ? "
                   + "ORDER BY date ASC";
        
        // 🚨 [수정] getConnection() -> connect()
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, monthPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 🚨 [수정] Transaction.java의 ID 포함 생성자에 맞게 수정
                    Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("date"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("content")
                    );
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("월별 거래 조회 중 DB 오류 발생: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * [기존 유지] 그래프 기능을 위한 메서드 1: 월별 총 수입/지출 합계 조회
     */
    public Map<String, Double> getMonthlySummary(int userId, int year, int month) {
        Map<String, Double> summary = new HashMap<>();
        summary.put("수입", 0.0);
        summary.put("지출", 0.0);
        
        String monthPattern = String.format("%d-%02d-%%", year, month);
        
        String sql = "SELECT type, SUM(amount) as total_amount "
                   + "FROM transactions "
                   + "WHERE user_id = ? AND date LIKE ? "
                   + "GROUP BY type";
        
        // 🚨 [수정] getConnection() -> connect()
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, monthPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    summary.put(rs.getString("type"), rs.getDouble("total_amount"));
                }
            }
        } catch (SQLException e) {
            System.err.println("월별 요약 조회 중 DB 오류 발생: " + e.getMessage());
        }
        
        return summary;
    }

    /**
     * [기존 유지] 그래프 기능을 위한 메서드 2: 월별 카테고리별 합계 조회
     */
    public Map<String, Double> getCategorySummary(int userId, int year, int month, String type) {
        Map<String, Double> categorySummary = new HashMap<>();
        String monthPattern = String.format("%d-%02d-%%", year, month);
        
        String sql = "SELECT category, SUM(amount) as total_amount "
                   + "FROM transactions "
                   + "WHERE user_id = ? AND date LIKE ? AND type = ? "
                   + "GROUP BY category "
                   + "ORDER BY total_amount DESC";
        
        // 🚨 [수정] getConnection() -> connect()
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, monthPattern);
            pstmt.setString(3, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categorySummary.put(rs.getString("category"), rs.getDouble("total_amount"));
                }
            }
        } catch (SQLException e) {
            System.err.println("카테고리 요약 조회 중 DB 오류 발생: " + e.getMessage());
        }
        
        return categorySummary;
    }

    /**
     * [신규 추가] 그래프 기능을 위한 메서드 3: 연간 월별 수입/지출 합계 조회
     * @return Map<String, double[]> : "수입", "지출" 키로 12개월치(0=1월, 11=12월) 배열 반환
     */
    public Map<String, double[]> getYearlySummary(int userId, int year) {
        Map<String, double[]> yearlyData = new HashMap<>();
        // 12개월(인덱스 0~11) 배열을 0.0으로 초기화
        yearlyData.put("수입", new double[12]);
        yearlyData.put("지출", new double[12]);

        // YEAR()와 MONTH() 함수는 MySQL에서 작동합니다.
        String sql = "SELECT MONTH(date) as month, type, SUM(amount) as total_amount "
                   + "FROM transactions "
                   + "WHERE user_id = ? AND YEAR(date) = ? "
                   + "GROUP BY MONTH(date), type";
                   
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, year);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("month"); // 1~12
                    String type = rs.getString("type");
                    double total = rs.getDouble("total_amount");

                    if (yearlyData.containsKey(type)) {
                        // 월(1~12)을 배열 인덱스(0~11)로 변환
                        yearlyData.get(type)[month - 1] = total; 
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("연간 요약 조회 중 DB 오류 발생: " + e.getMessage());
        }
        
        return yearlyData;
    }
}