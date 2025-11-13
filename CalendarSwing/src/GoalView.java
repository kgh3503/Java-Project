import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;     
import java.util.HashMap;  

public class GoalView extends JDialog implements ActionListener {

    private final User currentUser;
    private final GoalManager goalManager;
    private final int currentYear;
    private final int currentMonth;
    private final CalendarSwing parent;

    // UI 컴포넌트
    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;
    private JTextField amountField;
    private JButton saveBtn;
    private JPanel listPanel; // 목표 목록을 표시할 패널

  
    private final Map<String, String[]> categories = new HashMap<>();

    /**
     * 수입/지출별 카테고리 목록을 초기화합니다.
     */
    private void initCategories() {
        categories.put("지출", new String[]{"식비", "교통", "생활/쇼핑", "문화/여가", "건강/의료", "경조사/모임", "교육/자기개발", "기타"});
        categories.put("수입", new String[]{"근로 소득", "부가 소득", "금융 소득", "기타 소득"});
    }

    /**
     * '유형' 콤보박스 선택에 따라 '카테고리' 콤보박스 내용을 업데이트합니다.
     */
    private void updateCategoryCombo() {
        String selectedType = (String) typeCombo.getSelectedItem();
        categoryCombo.removeAllItems();
        
        // "전체" 항목은 목표 설정에 항상 필요
        categoryCombo.addItem("전체"); 
        
        if (selectedType != null) {
            String[] cats = categories.get(selectedType);
            if (cats != null) {
                for (String cat : cats) {
                    categoryCombo.addItem(cat);
                }
            }
        }
    }

    public GoalView(CalendarSwing owner, User user, GoalManager manager, int year, int month) {
        super(owner, String.format("%d년 %d월 목표 관리", year, month), true);
        this.parent = owner;
        this.currentUser = user;
        this.goalManager = manager;
        this.currentYear = year;
        this.currentMonth = month;
        
        initCategories(); //  생성자에서 카테고리 맵 초기화

        setSize(650, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);
        
        // 1. 목표 설정 입력부
        add(createInputPanel(), BorderLayout.NORTH);
        
        // 2. 목표 목록 및 현황 표시부
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createTitledBorder("목표 달성 현황"));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadGoalData(); // 기존 목표 데이터 로드 및 표시
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("신규 목표 설정"));
        
        typeCombo = new JComboBox<>(new String[]{"지출", "수입"});
        
        // 👈 [수정] new String[]... 부분을 삭제하고 빈 콤보박스로 생성
        categoryCombo = new JComboBox<>(); 
        
        amountField = new JTextField(10);
        saveBtn = new JButton("목표 저장");
        saveBtn.addActionListener(this);

        // -----------------
        // typeCombo에 리스너 추가 (유형 변경 시 카테고리 변경)
        // -----------------
        typeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCategoryCombo(); 
            }
        });
        updateCategoryCombo(); //  초기 카테고리 목록 설정 ("지출" 기준)
     

        inputPanel.add(new JLabel("유형:"));
        inputPanel.add(typeCombo);
        inputPanel.add(new JLabel("카테고리:"));
        inputPanel.add(categoryCombo);
        inputPanel.add(new JLabel("금액:"));
        inputPanel.add(amountField);
        inputPanel.add(saveBtn);
        
        return inputPanel;
    }

    private void loadGoalData() {
        listPanel.removeAll(); // 기존 목록 삭제
        
        List<Goal> goals = goalManager.getGoalsByMonth(currentUser.getUserId(), currentYear, currentMonth);
        
        if (goals.isEmpty()) {
            listPanel.add(new JLabel("현재 설정된 목표가 없습니다."));
        } else {
            for (Goal goal : goals) {
                // 목표별 현황 표시 컴포넌트 생성
                double progress = goalManager.checkProgress(goal);
                double rate = goalManager.getAchievementRate(goal);
                listPanel.add(createGoalProgressComponent(goal, progress, rate));
            }
        }
        
        listPanel.revalidate();
        listPanel.repaint();
    }
    
    // 개별 목표의 현황을 표시하는 UI 컴포넌트 생성 메서드
    private JPanel createGoalProgressComponent(Goal goal, double progress, double rate) {
        JPanel goalPane = new JPanel(new BorderLayout());
        goalPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        
        // 상단 정보
        String catInfo = (goal.getCategory() == null) ? "전체" : goal.getCategory();
        JLabel infoLabel = new JLabel(String.format(" %s - %s 목표: %,.0f원", goal.getType(), catInfo, goal.getTargetAmount()));
        goalPane.add(infoLabel, BorderLayout.NORTH);
        
        // 진행 상황 표시 (프로그레스 바)
        JProgressBar progressBar = new JProgressBar(0, 100);
        
        //  수입/지출 상관없이 달성률(rate)을 정수로 변환하여 설정
        // (시각적 표시는 100%로 제한하되, 텍스트는 실제 rate 표시)
        int percentage = (int) Math.min(100.0, rate); 
        
        progressBar.setValue(percentage);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("진행: %,.0f원 / 목표: %,.0f원 (%.1f%%)", progress, goal.getTargetAmount(), rate));

        // 지출 목표 초과 시 색상 변경
        if (goal.getType().equals("지출") && rate > 100.0) {
            progressBar.setForeground(Color.RED); // 지출 초과 (나쁨)
        } else if (goal.getType().equals("지출")) {
            progressBar.setForeground(Color.ORANGE); // 지출 진행 (주의)
        } else if (goal.getType().equals("수입") && rate >= 100.0) {
            progressBar.setForeground(Color.BLUE); // 수입 달성 (좋음)
        } else if (goal.getType().equals("수입")) {
             progressBar.setForeground(new Color(0, 150, 255)); // 수입 진행
        }
        
        goalPane.add(progressBar, BorderLayout.CENTER);
        return goalPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveBtn) {
            handleSaveGoal();
        }
    }
    
    private void handleSaveGoal() {
        String type = (String) typeCombo.getSelectedItem();
        String category = (String) categoryCombo.getSelectedItem();
        String amountText = amountField.getText();

        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "목표 금액을 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountText.replaceAll(",", ""));
            
            // "전체" 또는 null/빈 문자열일 경우 모두 null로 처리
            String finalCategory = (category == null || "전체".equals(category) || category.trim().isEmpty()) ? null : category;
            
            if (goalManager.setGoal(currentUser.getUserId(), type, finalCategory, amount, currentYear, currentMonth)) {
                JOptionPane.showMessageDialog(this, "목표가 성공적으로 저장되었습니다!", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
                amountField.setText("");
                loadGoalData(); // 목록 새로고침
            } else {
                JOptionPane.showMessageDialog(this, "목표 저장 실패: 동일한 목표가 이미 존재하거나 DB 오류입니다.", "저장 실패", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "금액은 유효한 숫자 형식이어야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}