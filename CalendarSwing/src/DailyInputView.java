import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Insets;

public class DailyInputView extends JDialog implements ActionListener {

    private final CalendarSwing parent; 
    private String selectedDate; 
    private final User currentUser; 
    
    // UI 컴포넌트
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JButton categorySelectBtn; 
    private JTextField contentField; 
    private JButton saveBtn;

    private final TransactionDao transactionDao = new TransactionDao(); 

    // 카테고리 데이터 정의
    private final String[] EXPENSE_CATEGORIES = {"식비", "교통", "생활/쇼핑", "문화/여가", "건강/의료", "경조사/모임", "교육/자기개발", "기타"};
    private final String[] INCOME_CATEGORIES = {"근로 소득", "부가 소득", "금융 소득", "기타 소득"};

    public DailyInputView(CalendarSwing owner, String date, User user) {
        super(owner, user.getUsername() + "님의 " + date + " 입력", true); 
        this.parent = owner;
        this.selectedDate = date;
        this.currentUser = user; 
        
        // 1. 창 기본 설정
        setSize(400, 300);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner); 

        // 2. 입력 폼 패널 생성
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 3. 컴포넌트 초기화 및 리스너 연결
        typeCombo = new JComboBox<>(new String[]{"지출", "수입"}); 
        amountField = new JTextField(15);
        categorySelectBtn = new JButton("카테고리 선택 ▼");
        contentField = new JTextField(15); 
        saveBtn = new JButton("저장");
        
        saveBtn.addActionListener(this); 
        categorySelectBtn.addActionListener(this);
        
        typeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    categorySelectBtn.setText("카테고리 선택 ▼"); 
                }
            }
        });

        // 4. 입력 폼 패널에 컴포넌트 추가
        inputPanel.add(new JLabel("날짜:"));
        inputPanel.add(new JLabel(date)); 
        inputPanel.add(new JLabel("유형:"));
        inputPanel.add(typeCombo);
        inputPanel.add(new JLabel("금액:"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("카테고리:"));
        inputPanel.add(categorySelectBtn); 
        inputPanel.add(new JLabel("내용 (메모):")); 
        inputPanel.add(contentField); 

        // 5. 프레임에 패널 추가
        add(inputPanel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        setVisible(true);
    }
    
    /**
     * 카테고리 팝업 메뉴를 생성하고 표시합니다.
     */
    private void showCategoryPopup(JButton sourceButton) {
        JPopupMenu popupMenu = new JPopupMenu();
        String selectedType = (String) typeCombo.getSelectedItem();
        String[] categories = selectedType.equals("수입") ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
        
        for (String category : categories) {
            JMenuItem item = new JMenuItem(category);
            item.setBackground(Color.WHITE); 
            item.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            item.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { item.setBackground(Color.YELLOW); }
                @Override public void mouseExited(MouseEvent e) { item.setBackground(Color.WHITE); }
            });

            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sourceButton.setText(category); 
                    popupMenu.setVisible(false);
                }
            });
            popupMenu.add(item);
        }
        
        popupMenu.show(sourceButton, 0, sourceButton.getHeight());
    }

    // 버튼 클릭 시 처리
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveBtn) {
            handleSaveTransaction(); 
        } else if (e.getSource() == categorySelectBtn) {
            showCategoryPopup((JButton) e.getSource());
        }
    }
    
    /**
     * 입력된 데이터를 검증하고 Transaction 객체로 만들어 저장 DAO를 호출합니다.
     */
    private void handleSaveTransaction() {
        String type = (String) typeCombo.getSelectedItem();
        String amountText = amountField.getText();
        String category = categorySelectBtn.getText();
        String content = contentField.getText();

        if (amountText.isEmpty() || category.equals("카테고리 선택 ▼")) {
            JOptionPane.showMessageDialog(this, "금액과 카테고리는 필수 입력 항목입니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "금액은 유효한 숫자 형식이어야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // (가정) Transaction.java에 6개 인자(id 제외)를 받는 생성자가 있다고 가정
        Transaction newTransaction = new Transaction(
            currentUser.getUserId(),    
            selectedDate,               
            type,                       
            amount,                     
            category,                   
            content                     
        );
        
        //  [수정] save() -> addTransaction()
        if (transactionDao.addTransaction(newTransaction)) {
            JOptionPane.showMessageDialog(this, "거래 내역이 성공적으로 저장되었습니다.", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
            // [수정] reloadMonthData() -> loadMonthData()
            parent.loadMonthData(); 
        } else {
            JOptionPane.showMessageDialog(this, "DB 저장 중 오류가 발생했습니다.", "저장 실패", JOptionPane.ERROR_MESSAGE);
        }
        
        this.dispose(); // 창 닫기
    }
}