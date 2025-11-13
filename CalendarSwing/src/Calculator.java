import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Calculator extends JFrame implements ActionListener {

    private JTextField display;
    private String[] buttons = {
        "7", "8", "9", "/",
        "4", "5", "6", "*",
        "1", "2", "3", "-",
        "C", "0", "=", "+"
    };

    private String currentNumber = "";
    private double result = 0;
    private String operator = "";
    private boolean isStart = true;

    public Calculator() {
        setTitle("간단 계산기");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        display = new JTextField("0");
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 4, 5, 5));
        
        for (String label : buttons) {
            JButton button = new JButton(label);
            button.setFont(new Font("SansSerif", Font.BOLD, 18));
            button.addActionListener(this);
            buttonPanel.add(button);
        }
        
        add(buttonPanel, BorderLayout.CENTER);
        
        setSize(300, 400);
        setLocationRelativeTo(null); 
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.charAt(0) >= '0' && command.charAt(0) <= '9') {
            if (isStart) {
                currentNumber = command;
                isStart = false;
            } else {
                currentNumber += command;
            }
            display.setText(currentNumber);
        } else if (command.equals("C")) {
            currentNumber = "";
            operator = "";
            result = 0;
            isStart = true;
            display.setText("0");
        } else if (command.equals("=")) {
            if (!operator.isEmpty()) {
                calculate(Double.parseDouble(currentNumber));
                operator = "";
                currentNumber = String.valueOf(result);
                isStart = true;
                display.setText(currentNumber);
            }
        } else {
            if (!currentNumber.isEmpty()) {
                if (operator.isEmpty()) {
                    result = Double.parseDouble(currentNumber);
                } else {
                    calculate(Double.parseDouble(currentNumber));
                }
            }
            operator = command;
            currentNumber = "";
            isStart = true;
        }
    }

    private void calculate(double num) {
        switch (operator) {
            case "+":
                result += num;
                break;
            case "-":
                result -= num;
                break;
            case "*":
                result *= num;
                break;
            case "/":
                if (num != 0) {
                    result /= num;
                } else {
                    display.setText("Error");
                    result = 0;
                    currentNumber = "";
                    operator = "";
                    isStart = true;
                    return;
                }
                break;
        }
        if (result == (long) result) {
            display.setText(String.format("%d", (long) result));
        } else {
            display.setText(String.valueOf(result));
        }
    }
}