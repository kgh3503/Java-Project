import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView extends JDialog implements ActionListener {

    private final UserDao userDao = new UserDao();
    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JButton signupBtn;
    
    private User loggedInUser = null; 

    public LoginView(JFrame owner) {
        super(owner, "사용자 로그인", true); 
        
        setSize(300, 200);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 중앙 입력 패널
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        
        idField = new JTextField(15);
        passwordField = new JPasswordField(15);

        inputPanel.add(new JLabel(" 아이디:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel(" 비밀번호:"));
        inputPanel.add(passwordField);

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        loginBtn = new JButton("로그인");
        signupBtn = new JButton("회원가입");
        
        loginBtn.addActionListener(this);
        signupBtn.addActionListener(this);
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(signupBtn);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public User showDialog() {
        // ✅ DB 테이블 생성 및 초기화 로직 (오류 방지)
        DatabaseManager.createTables(); 
        this.setVisible(true);
        return loggedInUser;
    }
    
    private void handleLogin() {
        String id = idField.getText();
        String pw = new String(passwordField.getPassword());
        
        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDao.authenticate(id, pw);
        
        if (user != null) {
            loggedInUser = user;
            JOptionPane.showMessageDialog(this, user.getUsername() + "님, 환영합니다!", "로그인 성공", JOptionPane.INFORMATION_MESSAGE);
            this.dispose(); 
        } else {
            JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleSignup() {
        String id = idField.getText();
        String pw = new String(passwordField.getPassword());

        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (userDao.registerUser(id, pw)) {
            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!\n이제 로그인해주세요.", "회원가입 성공", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "회원가입 실패: 아이디가 이미 존재하거나 DB 오류입니다.", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            handleLogin();
        } else if (e.getSource() == signupBtn) {
            handleSignup();
        }
    }
}