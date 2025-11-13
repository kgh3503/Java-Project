import javax.swing.*;
import javax.swing.border.TitledBorder; 
import javax.swing.JSplitPane; // JSplitPane import
import javax.swing.JTabbedPane; // 👈 [추가] JTabbedPane import
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File; 
import java.util.Map;
import java.util.Locale;
import java.text.NumberFormat;


public class AnalysisView extends JDialog implements ActionListener { 

    private final User currentUser;
    private final int year;
    private final int month;
    private final TransactionDao transactionDao = new TransactionDao();
    
    private JButton exportExcelBtn; // Excel 출력 버튼 필드
    private JButton compareMonthBtn; // 👈 [추가] 지난 달 비교 버튼 필드

    public AnalysisView(JFrame owner, User user, int year, int month) {
        // 👈 [수정] 제목에서 '월'을 제거하고 '연도'만 표시
        super(owner, String.format("%d년 분석", year), true); 
        this.currentUser = user;
        this.year = year;
        this.month = month;

        setSize(800, 550); 
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);
        
        // --- 1. 탭 패널 생성 ---
        JTabbedPane tabbedPane = new JTabbedPane();

        // --- 2. [월별 분석] 탭 (기존 내용) ---
        JPanel monthlyTabPanel = createMonthlyTabPanel();
        tabbedPane.addTab(String.format("%d월 분석", month), monthlyTabPanel);

        // --- 3. [연간 분석] 탭 (새로운 내용) ---
        JPanel yearlyTabPanel = createYearlyTabPanel();
        tabbedPane.addTab(String.format("%d년 전체 분석", year), yearlyTabPanel);

        // --- 4. 메인 프레임에 탭 패널 추가 ---
        add(tabbedPane, BorderLayout.CENTER);

        // --- 5. 하단 버튼 패널 (비교 버튼, Excel 버튼) ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // 👈 [추가] '지난 달과 비교' 버튼
        compareMonthBtn = new JButton("지난 달과 비교"); 
        compareMonthBtn.addActionListener(this);
        
        exportExcelBtn = new JButton("월별 내역 Excel로 출력");
        exportExcelBtn.addActionListener(this);

        southPanel.add(compareMonthBtn); // 👈 [추가]
        southPanel.add(exportExcelBtn);
        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
    
    /**
     * 👈 [신규] 월별 분석 탭 UI를 생성합니다. (기존 생성자 코드를 분리)
     */
    private JPanel createMonthlyTabPanel() {
        // 1. 데이터 로드
        Map<String, Double> monthlySummary = transactionDao.getMonthlySummary(currentUser.getUserId(), year, month);
        Map<String, Double> expenseCategorySummary = transactionDao.getCategorySummary(currentUser.getUserId(), year, month, "지출");
        
        // 2. 상단 요약 패널
        JPanel summaryPanel = createSummaryPanel(monthlySummary);
        
        // 3. 차트 패널 (Pie, Bar)
        JSplitPane monthlyChartPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new PieChartPanel("지출 카테고리 분석 (원그래프)", expenseCategorySummary, monthlySummary.getOrDefault("지출", 0.0)),
            new BarChartPanel("수입/지출 비교 (막대 차트)", monthlySummary)
        );
        monthlyChartPane.setDividerLocation(480);
        monthlyChartPane.setOneTouchExpandable(true);
        monthlyChartPane.setResizeWeight(0.6); 

        // 4. 월별 탭 구성 (BorderLayout)
        JPanel monthlyTabPanel = new JPanel(new BorderLayout());
        monthlyTabPanel.add(summaryPanel, BorderLayout.NORTH);
        monthlyTabPanel.add(monthlyChartPane, BorderLayout.CENTER);
        
        return monthlyTabPanel;
    }
    
    /**
     * 👈 [신규] 연간 분석 탭 UI를 생성합니다.
     */
    private JPanel createYearlyTabPanel() {
        // 1. 연간 데이터 로드 (새 DAO 메서드)
        Map<String, double[]> yearlyData = transactionDao.getYearlySummary(currentUser.getUserId(), year);
        
        // 2. 연간 차트 패널 (Line) (새 클래스)
        // (주의: YearlyLineChartPanel.java 파일이 프로젝트에 있어야 함)
        YearlyLineChartPanel yearlyChartPanel = new YearlyLineChartPanel(
            String.format("%d년 수입/지출 추이 (선 그래프)", year), yearlyData
        );
        
        return yearlyChartPanel;
    }

    /**
     * 월별 총액 요약을 표시하는 패널을 생성합니다. (수정 없음)
     */
    private JPanel createSummaryPanel(Map<String, Double> summary) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        panel.setBackground(new Color(240, 240, 255));
        panel.setPreferredSize(new Dimension(800, 40)); 

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        
        double income = summary.getOrDefault("수입", 0.0);
        double expense = summary.getOrDefault("지출", 0.0);
        double net = income - expense;
        
        JLabel incomeLabel = new JLabel("총 수입: " + nf.format(income) + "원");
        JLabel expenseLabel = new JLabel("총 지출: " + nf.format(expense) + "원");
        JLabel netLabel = new JLabel("순자산: " + nf.format(net) + "원");

        incomeLabel.setForeground(Color.BLUE);
        expenseLabel.setForeground(Color.RED);
        netLabel.setForeground(net >= 0 ? new Color(0, 150, 0) : Color.RED);
        
        panel.add(incomeLabel);
        panel.add(expenseLabel);
        panel.add(netLabel);
        
        return panel;
    }

    /**
     * 👈 [수정] 버튼 클릭 이벤트 처리
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exportExcelBtn) {
            handleExportExcel();
        } else if (e.getSource() == compareMonthBtn) { // 👈 [추가]
            handleCompareMonth();
        }
    }
    
    /**
     * 👈 [신규] 지난 달과 이번 달의 요약을 비교하는 다이얼로그를 엽니다.
     */
    private void handleCompareMonth() {
        // 1. 현재 월 데이터
        Map<String, Double> currentSummary = transactionDao.getMonthlySummary(currentUser.getUserId(), year, month);

        // 2. 지난 달 계산
        int prevYear = year;
        int prevMonth = month - 1;
        if (prevMonth == 0) {
            prevMonth = 12;
            prevYear--;
        }

        // 3. 지난 달 데이터 로드
        Map<String, Double> prevSummary = transactionDao.getMonthlySummary(currentUser.getUserId(), prevYear, prevMonth);
        
        double currentIncome = currentSummary.getOrDefault("수입", 0.0);
        double currentExpense = currentSummary.getOrDefault("지출", 0.0);
        double prevIncome = prevSummary.getOrDefault("수입", 0.0);
        double prevExpense = prevSummary.getOrDefault("지출", 0.0);

        // 4. 비교 결과를 HTML로 포맷팅
        String message = String.format(
            "<html><h3>%d년 %d월 (지난달) vs %d년 %d월 (이번달)</h3>" +
            "<hr>" +
            "<b>총 수입:</b><br>" +
            " - 이번 달: %,.0f 원<br>" +
            " - 지난 달: %,.0f 원<br>" +
            " - 차이: <font color='%s'>%,.0f 원</font><br>" +
            "<hr>" +
            "<b>총 지출:</b><br>" +
            " - 이번 달: %,.0f 원<br>" +
            " - 지난 달: %,.0f 원<br>" +
            " - 차이: <font color='%s'>%,.0f 원</font><br>" +
            "<hr>" +
            "<b>순자산 (수입-지출):</b><br>" +
            " - 이번 달: %,.0f 원<br>" +
            " - 지난 달: %,.0f 원</html>",
            prevYear, prevMonth, year, month,
            currentIncome, prevIncome,
            (currentIncome >= prevIncome ? "blue" : "red"), (currentIncome - prevIncome),
            currentExpense, prevExpense,
            // 지출은 (이번달 - 지난달)이 음수(줄어든 것)일 때 파란색
            (currentExpense <= prevExpense ? "blue" : "red"), (currentExpense - prevExpense), 
            (currentIncome - currentExpense),
            (prevIncome - prevExpense)
        );

        JOptionPane.showMessageDialog(this, message, "월별 비교", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 현재 월의 거래 내역을 조회하고 ExcelExporter를 호출합니다. (수정 없음)
     */
    private void handleExportExcel() {
        // 1. 현재 월의 모든 상세 거래 내역 조회
        java.util.List<Transaction> transactions = transactionDao.findByMonthAndUser(
            currentUser.getUserId(), this.year, this.month
        );
        
        if (transactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "출력할 거래 내역이 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. 파일 저장 경로 다이얼로그 설정
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(String.format("%d년 %d월 내역 저장", year, month));
        fileChooser.setSelectedFile(new File(String.format("가계부_%d년_%d월_내역.xlsx", year, month)));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            
            // 3. Excel 출력 실행 (ExcelExporter 클래스가 프로젝트에 존재해야 함)
            ExcelExporter exporter = new ExcelExporter();
            
            if (exporter.exportMonth(transactions, this.year, this.month, filePath)) {
                JOptionPane.showMessageDialog(this, "Excel 파일이 성공적으로 저장되었습니다.", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 오류는 ExcelExporter 내부에서 출력됨
            }
        }
    }
}

// ---------------------- PieChartPanel 클래스 정의 (수정 없음) ----------------------

class PieChartPanel extends JPanel {
    private final String title;
    private final Map<String, Double> data;
    private final double total;
    private final String[] colors = {"#FF6347", "#4682B4", "#3CB371", "#FFD700", "#9370DB", "#FFA07A", "#6A5ACD", "#8FBC8F"};

    public PieChartPanel(String title, Map<String, Double> data, double total) {
        this.title = title;
        this.data = data;
        this.total = total;
        // TitledBorder 사용 시 import javax.swing.border.TitledBorder 필요
        setBorder(BorderFactory.createTitledBorder(title)); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (total <= 0 || data.isEmpty()) {
            g.drawString("지출 내역이 없습니다.", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
        
        // 🚨 차트 영역 설정: 범례 공간 확보
        // 이 로직은 JSplitPane 덕분에 부모로부터 충분한 너비를 받으므로 잘 동작합니다.
        int legendWidth = (int)(getWidth() * 0.45); // 범례 공간 45% 확보
        int chartAreaWidth = getWidth() - legendWidth;
        int size = Math.min(chartAreaWidth, getHeight()) - 40; 
        int x = (chartAreaWidth - size) / 2;
        int y = getHeight() / 2 - size / 2;

        double currentAngle = 0;
        int colorIndex = 0;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        
        // 범례 시작 위치
        int legendX = chartAreaWidth + 10;
        int legendY = 30; 

        // 원그래프 그리기 및 범례 표시
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double value = entry.getValue();
            double percent = value / total;
            int angle = (int) Math.round(percent * 360);

            // 원그래프
            Color color = Color.decode(colors[colorIndex % colors.length]);
            g2d.setColor(color);
            g2d.fillArc(x, y, size, size, (int) currentAngle, angle);
            
            // 범례 표시
            int currentLegendY = legendY + colorIndex * 20; 
            
            // 범례 사각형
            g2d.fillRect(legendX, currentLegendY, 10, 10);
            g2d.setColor(Color.BLACK);
            
            // 범례 텍스트
            g2d.drawString(entry.getKey() + ": " + nf.format(value) + String.format("원 (%.1f%%)", percent * 100), legendX + 15, currentLegendY + 10);

            currentAngle += angle;
            colorIndex++;
        }
    }
}

// ---------------------- BarChartPanel 클래스 정의 (수정 없음) ----------------------

class BarChartPanel extends JPanel {
    private final String title;
    private final Map<String, Double> data;

    public BarChartPanel(String title, Map<String, Double> data) {
        this.title = title;
        this.data = data;
        setBorder(BorderFactory.createTitledBorder(title));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Padding 증가 (글씨 잘림 해결)
        int padding = 60; 
        int barWidth = 40;
        int chartHeight = getHeight() - 2 * padding;
        int chartWidth = getWidth() - 2 * padding;

        double income = data.getOrDefault("수입", 0.0);
        double expense = data.getOrDefault("지출", 0.0);
        double max = Math.max(income, expense);

        if (max <= 0) {
            g.drawString("내역이 없습니다.", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
        
        // 기준선 (X축) 그리기
        g2d.drawLine(padding, padding + chartHeight, padding + chartWidth, padding + chartHeight);
        
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        
        // 수입 막대
        double incomeRatio = (income / max);
        int incomeBarHeight = (int) (incomeRatio * chartHeight);
        g2d.setColor(Color.BLUE);
        int incomeX = padding + chartWidth / 4 - barWidth / 2;
        g2d.fillRect(incomeX, padding + chartHeight - incomeBarHeight, barWidth, incomeBarHeight);
        g2d.setColor(Color.BLACK);
        
        // 금액 레이블 위치 조정 (상단에 명확히 표시)
        String incomeStr = nf.format(income);
        g2d.drawString(incomeStr, incomeX + barWidth/2 - g2d.getFontMetrics().stringWidth(incomeStr)/2, padding + chartHeight - incomeBarHeight - 5);
        g2d.drawString("수입", incomeX + barWidth/2 - g2d.getFontMetrics().stringWidth("수입")/2, padding + chartHeight + 15);

        // 지출 막대
        double expenseRatio = (expense / max);
        int expenseBarHeight = (int) (expenseRatio * chartHeight);
        g2d.setColor(Color.RED);
        int expenseX = padding + chartWidth * 3 / 4 - barWidth / 2;
        g2d.fillRect(expenseX, padding + chartHeight - expenseBarHeight, barWidth, expenseBarHeight);
        g2d.setColor(Color.BLACK);
        
        // 금액 레이블 위치 조정 (상단에 명확히 표시)
        String expenseStr = nf.format(expense);
        g2d.drawString(expenseStr, expenseX + barWidth/2 - g2d.getFontMetrics().stringWidth(expenseStr)/2, padding + chartHeight - expenseBarHeight - 5);
        g2d.drawString("지출", expenseX + barWidth/2 - g2d.getFontMetrics().stringWidth("지출")/2, padding + chartHeight + 15);
    }
}