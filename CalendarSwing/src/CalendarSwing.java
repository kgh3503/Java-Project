import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class CalendarSwing extends JFrame implements ItemListener, ActionListener {

    Font fnt = new Font("굴림체", Font.BOLD, 18);

    // 상단 선택 패널
    JPanel selectPane = new JPanel();
    JButton prevBtn = new JButton("◀");
    JButton nextBtn = new JButton("▶");
    JComboBox<Integer> yearCombo = new JComboBox<Integer>();
    JComboBox<Integer> monthCombo = new JComboBox<Integer>();
    JLabel yearLBl = new JLabel("년");
    JLabel monthLBl = new JLabel("월");
    JButton analysisBtn = new JButton("분석");
    JButton calculatorBtn = new JButton("계산기");
    JButton goalBtn = new JButton("목표 관리");	//Goal

    // 중앙 캘린더 패널
    JPanel centerPane = new JPanel(new BorderLayout());
    JPanel titlePane = new JPanel(new GridLayout(1, 7));
    String[] title = {"일", "월", "화", "수", "목", "금", "토"};
    JPanel dayPane = new JPanel(new GridLayout(0, 7));

    // 달력 데이터
    Calendar date;
    int year;
    int month;

    // 사용자 및 DAO
    private final User currentUser;
    private final TransactionDao transactionDao = new TransactionDao();
    private final GoalDao goalDao = new GoalDao();		//Goal
    private final GoalManager goalManager;				//Goal
    private List<Transaction> currentMonthTransactions;

    // --- [새로 추가된 오른쪽 상세 패널 컴포넌트] ---
    private JPanel detailsPanel;
    private JLabel selectedDateLabel;
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;
    private JTextField contentField;
    private JTextField amountField;
    private JButton addButton;
    private JButton deleteButton;

    // 선택 상태 관리
    private JButton previouslySelectedDayButton = null;
    private String currentSelectedDate = null;
    
    // 카테고리 데이터
    private final Map<String, String[]> categories = new HashMap<>();

    /**
     * 생성자: User 객체를 받아 UI를 초기화합니다.
     */
    public CalendarSwing(User user) {
        super("가계부 달력 - " + user.getUsername() + "님");
        this.currentUser = user;
        
        this.goalManager = new GoalManager(transactionDao, goalDao);	//Goal

        date = Calendar.getInstance();
        year = date.get(Calendar.YEAR);
        month = date.get(Calendar.MONTH) + 1;

        initCategories(); // 카테고리 맵 초기화

        // --- 1. 상단 패널 (NORTH) ---
        selectPane.setBackground(new Color(150, 200, 200));
        prevBtn.setFont(fnt); selectPane.add(prevBtn);
        yearCombo.setFont(fnt); selectPane.add(yearCombo);
        yearLBl.setFont(fnt); selectPane.add(yearLBl);
        monthCombo.setFont(fnt); selectPane.add(monthCombo);
        monthLBl.setFont(fnt); selectPane.add(monthLBl);
        nextBtn.setFont(fnt); selectPane.add(nextBtn);
        analysisBtn.setFont(fnt); selectPane.add(analysisBtn);
        calculatorBtn.setFont(fnt); selectPane.add(calculatorBtn);
        
        //Goal
        goalBtn.setFont(fnt);
        goalBtn.addActionListener(this);
        selectPane.add(goalBtn);
        
        add(BorderLayout.NORTH, selectPane);

        // --- 2. 캘린더 패널 (CENTER) ---
        setYear();
        setMonth();
        setCalendarTitle();
        centerPane.add(BorderLayout.NORTH, titlePane);
        centerPane.add(dayPane);
        add(centerPane, BorderLayout.CENTER); // 캘린더를 중앙에 배치

        // --- 3. 상세 정보 패널 (EAST) ---
        this.detailsPanel = createDetailsPanel();
        add(detailsPanel, BorderLayout.EAST); // 상세 패널을 오른쪽에 배치

        // --- 4. 이벤트 리스너 ---
        prevBtn.addActionListener(this);
        nextBtn.addActionListener(this);
        yearCombo.addItemListener(this);
        monthCombo.addItemListener(this);
        analysisBtn.addActionListener(this);
        calculatorBtn.addActionListener(this);
        
        // --- 5. JFrame 설정 ---
        setExtendedState(JFrame.MAXIMIZED_BOTH); // 전체 화면
        // 창이 너무 작아지지 않도록 최소 크기 설정
        setMinimumSize(new Dimension(1024, 768)); 
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 초기 데이터 로드
        loadMonthData();
    }

    /**
     * 수입/지출별 카테고리 목록을 초기화합니다.
     */
    private void initCategories() {
        categories.put("지출", new String[]{"식비", "교통", "생활/쇼핑", "문화/여가", "건강/의료", "경조사/모임", "교육/자기개발", "기타"});
        categories.put("수입", new String[]{"근로 소득", "부가 소득", "금융 소득", "기타 소득"});
    }
    /**
     * 오른쪽 상세 정보 패널 UI를 생성합니다.
     */
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // 세로 정렬
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 여백
        
        // 요청하신대로, 창을 줄여도 패널이 사라지지 않게 최소 너비 설정
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setMinimumSize(new Dimension(350, 0)); 

        // 1. 날짜 라벨
        selectedDateLabel = new JLabel("날짜를 선택하세요");
        selectedDateLabel.setFont(fnt);
        panel.add(selectedDateLabel);

        // 2. 거래 내역 테이블
        String[] columnNames = {"유형", "카테고리", "내용", "금액", "ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 테이블 수정 불가
            }
        };
        transactionsTable = new JTable(tableModel);
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 'ID' 컬럼은 데이터 관리를 위해 필요하므로, 숨김 처리
        hideIdColumn(transactionsTable);

        JScrollPane tableScrollPane = new JScrollPane(transactionsTable);
        panel.add(tableScrollPane);

        // 3. 입력 필드 패널
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // (행, 2열, 가로갭, 세로갭)
        inputPanel.setBorder(BorderFactory.createTitledBorder("내역 입력"));

        typeCombo = new JComboBox<>(new String[]{"지출", "수입"});
        categoryCombo = new JComboBox<>();
        
        // '유형' 콤보박스를 변경할 때 '카테고리' 목록이 바뀌도록 리스너 추가
        typeCombo.addActionListener(e -> updateCategoryCombo());
        updateCategoryCombo(); // 초기 카테고리 목록 설정

        contentField = new JTextField();
        amountField = new JTextField();

        inputPanel.add(new JLabel("유형:"));
        inputPanel.add(typeCombo);
        inputPanel.add(new JLabel("카테고리:"));
        inputPanel.add(categoryCombo);
        inputPanel.add(new JLabel("상세 내용:"));
        inputPanel.add(contentField);
        inputPanel.add(new JLabel("금액:"));
        inputPanel.add(amountField);
        
        // inputPanel이 세로로 늘어나지 않도록 최대 크기 고정
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                                    inputPanel.getPreferredSize().height));
        panel.add(inputPanel);

        // 4. 버튼
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // (1행, 2열)
        addButton = new JButton("입력 추가");
        deleteButton = new JButton("내용 삭제");
        
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                                     buttonPanel.getPreferredSize().height));
        panel.add(buttonPanel);

        return panel;
    }
    
    /**
     * JTable에서 'ID' 컬럼을 보이지 않게 숨깁니다.
     */
    private void hideIdColumn(JTable table) {
        table.getColumn("ID").setMinWidth(0);
        table.getColumn("ID").setMaxWidth(0);
        table.getColumn("ID").setWidth(0);
    }
    
    /**
     * '유형' 콤보박스 선택에 따라 '카테고리' 콤보박스 내용을 업데이트합니다.
     */
    private void updateCategoryCombo() {
        String selectedType = (String) typeCombo.getSelectedItem();
        categoryCombo.removeAllItems();
        if (selectedType != null) {
            String[] cats = categories.get(selectedType);
            if (cats != null) {
                for (String cat : cats) {
                    categoryCombo.addItem(cat);
                }
            }
        }
    }

    /**
     * DB에서 로드된 데이터를 반영하여 달력 UI를 갱신
     */
    public void updateCalendarUI() {
        dayPane.removeAll();

        date.set(year, month - 1, 1);
        int week = date.get(Calendar.DAY_OF_WEEK);
        int lastDay = date.getActualMaximum(Calendar.DATE);

        Map<Integer, Map<String, Double>> dailyIncomeMaps = new HashMap<>();
        Map<Integer, Map<String, Double>> dailyExpenseMaps = new HashMap<>();

        if (currentMonthTransactions != null) {
            for (Transaction t : currentMonthTransactions) {
                int dayOfMonth = Integer.parseInt(t.getDate().substring(8));
                if (t.getType().equals("수입")) {
                    dailyIncomeMaps.computeIfAbsent(dayOfMonth, k -> new HashMap<>())
                                 .merge(t.getCategory(), t.getAmount(), Double::sum);
                } else {
                    dailyExpenseMaps.computeIfAbsent(dayOfMonth, k -> new HashMap<>())
                                  .merge(t.getCategory(), t.getAmount(), Double::sum);
                }
            }
        }

        for (int s = 1; s < week; s++) {
            dayPane.add(new JLabel(" "));
        }

        for (int day = 1; day <= lastDay; day++) {
            JPanel dayCell = new JPanel(new BorderLayout());
            dayCell.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(new Font("굴림체", Font.BOLD, 14));
            dayBtn.setPreferredSize(new Dimension(50, 20));
            dayBtn.setMargin(new Insets(0, 0, 0, 0));

            String dateString = String.format("%d-%02d-%02d", year, month, day);
            dayBtn.setActionCommand(dateString);
            dayBtn.addActionListener(this);
            
            // [수정] 현재 선택된 날짜와 일치하면 노란색 배경 유지
            if (dateString.equals(currentSelectedDate)) {
                dayBtn.setBackground(Color.YELLOW);
                previouslySelectedDayButton = dayBtn;
            }

            date.set(Calendar.DATE, day);
            int w = date.get(Calendar.DAY_OF_WEEK);
            if (w == Calendar.SUNDAY) dayBtn.setForeground(Color.red);
            if (w == Calendar.SATURDAY) dayBtn.setForeground(Color.blue);

            dayCell.add(dayBtn, BorderLayout.NORTH);

            // ... (기존 요약 HTML 코드 - 생략) ...
            Map<String, Double> incomes = dailyIncomeMaps.get(day);
            Map<String, Double> expenses = dailyExpenseMaps.get(day);
            StringBuilder incomeStr = new StringBuilder();
            StringBuilder expenseStr = new StringBuilder();
            if (incomes != null && !incomes.isEmpty()) {
                for (Map.Entry<String, Double> entry : incomes.entrySet()) {
                    incomeStr.append(String.format(Locale.KOREA, "%s: +%,.0f<br>", 
                                     entry.getKey(), entry.getValue()));
                }
            }
            if (expenses != null && !expenses.isEmpty()) {
                for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                    expenseStr.append(String.format(Locale.KOREA, "%s: -%,.0f<br>", 
                                      entry.getKey(), entry.getValue()));
                }
            }
            JLabel summary = new JLabel("", SwingConstants.CENTER); 
            summary.setFont(new Font("맑은 고딕", Font.PLAIN, 12)); 
            if (incomeStr.length() > 0 || expenseStr.length() > 0) {
                summary.setText("<html><font color='blue'>" + incomeStr.toString() + "</font>" + 
                                "<font color='red'>" + expenseStr.toString() + "</font></html>");
            }
            dayCell.add(summary, BorderLayout.CENTER);
            
            dayPane.add(dayCell);
        }

        dayPane.revalidate();
        dayPane.repaint();
    }

    /**
     * DB에서 월별 데이터 로드
     * 🚨 [수정] DailyInputView가 접근할 수 있도록 public으로 변경
     */
    public void loadMonthData() {
        this.currentMonthTransactions = transactionDao.findByMonthAndUser(
            currentUser.getUserId(),
            this.year,
            this.month
        );
        updateCalendarUI();
    }

    /**
     * 날짜 패널 리셋
     */
    private void setDayReset() {
        yearCombo.removeItemListener(this);
        monthCombo.removeItemListener(this);
        yearCombo.setSelectedItem(year);
        monthCombo.setSelectedItem(month);
        
        // [수정] 날짜 리셋 시 선택된 날짜 및 패널 초기화
        currentSelectedDate = null;
        previouslySelectedDayButton = null;
        updateDetailsPanel(null); // 오른쪽 패널 클리어
        
        // dayPane 갱신 (기존 코드)
        dayPane.setVisible(false);
        dayPane.removeAll();
        yearCombo.addItemListener(this);
        monthCombo.addItemListener(this);
        loadMonthData();
        dayPane.setVisible(true);
    }
    
    // --- [오른쪽 패널을 위한 새 헬퍼 메서드] ---

    /**
     * [신규] 날짜를 받아와 오른쪽 패널의 라벨과 테이블을 업데이트합니다.
     * @param dateString "YYYY-MM-DD" 형식의 날짜, null이면 패널 초기화
     */
    private void updateDetailsPanel(String dateString) {
        if (dateString == null) {
            selectedDateLabel.setText("날짜를 선택하세요");
            tableModel.setRowCount(0); // 테이블 비우기
            clearInputFields();
            return;
        }
        
        // 1. 날짜 라벨 변경 (예: "2025-10-31" -> "10월 31일 소비 내역")
        try {
            LocalDate date = LocalDate.parse(dateString);
            String formattedDate = String.format("%d월 %d일 소비 내역",
                date.getMonthValue(), date.getDayOfMonth());
            selectedDateLabel.setText(formattedDate);
        } catch (Exception e) {
            selectedDateLabel.setText(dateString);
        }

        // 2. 테이블 데이터 로드
        tableModel.setRowCount(0); // 테이블 비우기

        // (주의) TransactionDao에 getTransactionsByDate 메서드가 필요합니다.
        List<Transaction> txList = transactionDao.getTransactionsByDate(
            currentUser.getUserId(), dateString);

        if (txList != null) {
            for (Transaction t : txList) {
                Object[] row = {
                    t.getType(),
                    t.getCategory(),
                    t.getContent(),
                    String.format(Locale.KOREA, "%,.0f", t.getAmount()),
                    t.getTransactionId() 
                };
                tableModel.addRow(row);
            }
        }
        clearInputFields();
    }
    
    /**
     * [신규] '입력 추가' 버튼 클릭 시 실행됩니다.
     */
    private void addTransaction() {
        if (currentSelectedDate == null) {
            JOptionPane.showMessageDialog(this, "먼저 캘린더에서 날짜를 선택하세요.");
            return;
        }

        try {
            // 1. 입력 값 읽기
            String type = (String) typeCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            String content = contentField.getText();
            
            if (content == null || content.trim().isEmpty()) {
                 JOptionPane.showMessageDialog(this, "상세 내용을 입력하세요.");
                 return;
            }

            // 2. 금액 유효성 검사 추가
            String amountText = amountField.getText();
            if (amountText == null || amountText.trim().isEmpty()) {
                 JOptionPane.showMessageDialog(this, "금액을 입력하세요.");
                 return;
            }

            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                 JOptionPane.showMessageDialog(this, "금액은 0보다 커야 합니다.");
                 return;
            }

            // 2. Transaction 객체 생성 (ID 없는 생성자 사용)
            Transaction newTx = new Transaction(
                currentUser.getUserId(),
                currentSelectedDate,
                type,
                amount,
                category,
                content
            );

            // 3. DAO를 통해 DB에 저장 (TransactionDao에 addTransaction 메서드 필요)
            transactionDao.addTransaction(newTx);
            
            // 4. 화면 새로고침
            updateDetailsPanel(currentSelectedDate); // 오른쪽 테이블 새로고침
            loadMonthData(); // 캘린더 요약 새로고침

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "금액은 숫자로만 입력하세요.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "입력 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * [신규] '내용 삭제' 버튼 클릭 시 실행됩니다.
     */
    private void deleteTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 항목을 테이블에서 선택하세요.");
            return;
        }

        // 1. 숨겨진 ID 값 가져오기 (테이블 모델의 4번 인덱스가 ID)
        int modelRow = transactionsTable.convertRowIndexToModel(selectedRow);
        // 🚨 [수정] transactionId가 ID 컬럼의 실제 값이 되도록 수정
        int transactionId = (int) tableModel.getValueAt(modelRow, 4);

        // 2. 사용자 확인
        int confirm = JOptionPane.showConfirmDialog(this,
            "정말로 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 3. DAO를 통해 DB에서 삭제 (TransactionDao에 deleteTransactionById 메서드 필요)
                transactionDao.deleteTransactionById(transactionId);

                // 4. 화면 새로고침
                updateDetailsPanel(currentSelectedDate); // 오른쪽 테이블 새로고침
                loadMonthData(); // 캘린더 요약 새로고침

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "삭제 중 오류 발생: " + e.getMessage());
            }
        }
    }
    
    /**
     * [신규] 오른쪽 하단 입력 필드를 초기화합니다.
     */
    private void clearInputFields() {
        typeCombo.setSelectedIndex(0); // '지출'로 기본 설정
        updateCategoryCombo(); // 카테고리도 '지출' 목록으로 리셋
        contentField.setText("");
        amountField.setText("");
    }

    // --- [기존 메서드 (대부분 변경 없음)] ---

    // 달력 요일(일~토) 설정
    public void setCalendarTitle() {
        for (String s : title) {
            JLabel lbl = new JLabel(s, JLabel.CENTER);
            lbl.setFont(fnt);
            if (s.equals("일")) lbl.setForeground(Color.RED);
            if (s.equals("토")) lbl.setForeground(Color.BLUE);
            titlePane.add(lbl);
        }
    }
    // 년도 콤보박스 설정
    public void setYear() {
        Calendar current = Calendar.getInstance();
        int currentYear = current.get(Calendar.YEAR);
        for (int i = currentYear - 10; i <= currentYear + 10; i++) {
            yearCombo.addItem(i);
        }
        yearCombo.setSelectedItem(year);
    }
    // 월 콤보박스 설정
    public void setMonth() {
        for (int i = 1; i <= 12; i++) {
            monthCombo.addItem(i);
        }
        monthCombo.setSelectedItem(month);
    }
    // 이전 달 이동
    public void prevMonth() {
        if (month == 1) { year--; month = 12; } else { month--; }
    }
    // 다음 달 이동
    public void nextMonth() {
        if (month == 12) { year++; month = 1; } else { month++; }
    }

    // 콤보박스 이벤트 처리
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            year = (int) yearCombo.getSelectedItem();
            month = (int) monthCombo.getSelectedItem();
            
            // [수정] 월 변경 시 선택 상태 초기화
            currentSelectedDate = null;
            previouslySelectedDayButton = null;
            updateDetailsPanel(null); // 오른쪽 패널 클리어
            
            loadMonthData();
        }
    }

    /**
     * 버튼 클릭 및 날짜 클릭 이벤트 처리 (수정됨)
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        Object obj = ae.getSource();
        String command = ae.getActionCommand();

        if (obj == prevBtn) {
            prevMonth();
            setDayReset();
        } else if (obj == nextBtn) {
            nextMonth();
            setDayReset();
        } else if (obj == analysisBtn) {
            // 분석 버튼 클릭
            new AnalysisView(this, currentUser, year, month);
        } else if (obj == calculatorBtn) {
            // 계산기 버튼 클릭
            SwingUtilities.invokeLater(() -> new Calculator());
            
        // Goal
        } else if (obj == goalBtn) {
        	new GoalView(this, currentUser, goalManager, year, month).setVisible(true);
            
        // --- [ 여기가 핵심 수정 부분 ] ---
        } else if (command != null && command.matches("\\d{4}-\\d{2}-\\d{2}")) {
            // 날짜 버튼 클릭 시
            
            // 1. 선택된 날짜 저장
            this.currentSelectedDate = command;

            // 2. 버튼 하이라이트 (노란색)
            JButton clickedButton = (JButton) obj;
            if (previouslySelectedDayButton != null) {
                previouslySelectedDayButton.setBackground(null); // 이전 버튼 색상 복원
            }
            clickedButton.setBackground(Color.YELLOW);
            previouslySelectedDayButton = clickedButton;

            // 3. 오른쪽 패널 데이터 업데이트 (팝업 대신)
            updateDetailsPanel(command);
            
        } else if (obj == addButton) {
            // [새로 추가] '입력 추가' 버튼 로직
            addTransaction();
            
        } else if (obj == deleteButton) {
            // [새로 추가] '내용 삭제' 버튼 로직
            deleteTransaction();
        }
        // --- [ 수정 끝 ] ---
    }

    // 메인 메서드 (변경 없음)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame initialFrame = new JFrame();
            initialFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            initialFrame.setVisible(false);

            LoginView loginDialog = new LoginView(initialFrame);
            User loggedInUser = loginDialog.showDialog();

            if (loggedInUser != null) {
                initialFrame.dispose();
                new CalendarSwing(loggedInUser);
            } else {
                initialFrame.dispose();
                System.exit(0);
            }
        });
    }
}