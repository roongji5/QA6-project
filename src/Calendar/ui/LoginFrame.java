package Calendar.ui;

import Calendar.model.User;
import Calendar.repository.UserRepository;
import Calendar.session.UserSession;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.function.Supplier;

public class LoginFrame extends JFrame {

    private static final double MAIN_WINDOW_RATIO = 0.3;
    private static final Dimension MIN_LOGIN_SIZE = new Dimension(420, 320);

    private final UserRepository userRepository;
    private final Supplier<JFrame> calendarFrameFactory;
    private final JLabel titleLabel = new JLabel("Calendar Login");
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JTextField loginIdField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JTextField signUpLoginIdField = new JTextField(18);
    private final JPasswordField signUpPasswordField = new JPasswordField(18);
    private final JTextField signUpNameField = new JTextField(18);
    private final JTextField signUpJobField = new JTextField(18);
    private final JButton loginButton = createLoginButton();
    private final JButton signUpSubmitButton = createSignUpSubmitButton();

    public LoginFrame(UserRepository userRepository, Supplier<JFrame> calendarFrameFactory) {
        this.userRepository = userRepository;
        this.calendarFrameFactory = calendarFrameFactory;

        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(calculateLoginWindowSize());
        setMinimumSize(MIN_LOGIN_SIZE);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(createContent());
        getRootPane().setDefaultButton(loginButton);
    }

    private Dimension calculateLoginWindowSize() {
        Dimension mainWindowSize = CalendarFrame.calculateMainWindowSize();
        return new Dimension(
                Math.max((int) (mainWindowSize.width * MAIN_WINDOW_RATIO), MIN_LOGIN_SIZE.width),
                Math.max((int) (mainWindowSize.height * MAIN_WINDOW_RATIO), MIN_LOGIN_SIZE.height)
        );
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        root.add(titleLabel, BorderLayout.NORTH);

        cardPanel.setOpaque(false);
        cardPanel.add(createLoginPanel(), "login");
        cardPanel.add(createSignUpPanel(), "signUp");
        root.add(cardPanel, BorderLayout.CENTER);

        return root;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("ID"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 12, 8, 0);
        formPanel.add(loginIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(new JLabel("Password"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 12, 8, 0);
        formPanel.add(passwordField, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        JButton openSignUpButton = new JButton("Sign Up");
        openSignUpButton.addActionListener(event -> showSignUpPanel());

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 0;
        buttonGbc.insets = new Insets(0, 0, 0, 8);
        buttonPanel.add(openSignUpButton, buttonGbc);

        buttonGbc.gridx = 1;
        buttonGbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(loginButton, buttonGbc);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSignUpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        addFormRow(formPanel, 0, "Login ID", signUpLoginIdField);
        addFormRow(formPanel, 1, "Password", signUpPasswordField);
        addFormRow(formPanel, 2, "Name", signUpNameField);
        addFormRow(formPanel, 3, "Job", signUpJobField);

        panel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> showLoginPanel());

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 0;
        buttonGbc.insets = new Insets(0, 0, 0, 8);
        buttonPanel.add(backButton, buttonGbc);

        buttonGbc.gridx = 1;
        buttonGbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(signUpSubmitButton, buttonGbc);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void addFormRow(JPanel formPanel, int row, String label, JTextField field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 12, 8, 0);
        formPanel.add(field, gbc);
    }

    private JButton createLoginButton() {
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(event -> login());
        return loginButton;
    }

    private JButton createSignUpSubmitButton() {
        JButton button = new JButton("Create");
        button.addActionListener(event -> signUp());
        return button;
    }

    private void showLoginPanel() {
        titleLabel.setText("Calendar Login");
        cardLayout.show(cardPanel, "login");
        getRootPane().setDefaultButton(loginButton);
        clearSignUpFields();
    }

    private void showSignUpPanel() {
        titleLabel.setText("Create Account");
        cardLayout.show(cardPanel, "signUp");
        getRootPane().setDefaultButton(signUpSubmitButton);
        clearLoginFields();
    }

    private void login() {
        String loginId = loginIdField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        Arrays.fill(passwordChars, '\0');

        if (loginId.isBlank() || password.isBlank()) {
            showLoginError("ID와 Password를 입력하세요.");
            return;
        }

        User user = userRepository.findByLoginId(loginId);
        if (user == null || !password.equals(user.getPassword())) {
            showLoginError("ID 또는 Password가 올바르지 않습니다.");
            return;
        }

        UserSession.login(user);
        openCalendarApp();
    }

    private void signUp() {
        String loginId = signUpLoginIdField.getText().trim();
        char[] passwordChars = signUpPasswordField.getPassword();
        String password = new String(passwordChars);
        Arrays.fill(passwordChars, '\0');
        String name = signUpNameField.getText().trim();
        String job = signUpJobField.getText().trim();

        if (loginId.isBlank() || password.isBlank() || name.isBlank()) {
            showSignUpError("Login ID, Password, Name은 필수입니다.");
            return;
        }

        if (isDuplicatedLoginId(loginId)) {
            showSignUpError("이미 존재하는 ID 입니다.");
            return;
        }

        userRepository.addUser(new User(loginId, password, name, job.isBlank() ? null : job));
        JOptionPane.showMessageDialog(this, "회원가입 완료", "Sign Up", JOptionPane.INFORMATION_MESSAGE);
        clearSignUpFields();
        showLoginPanel();
    }

    private boolean isDuplicatedLoginId(String loginId) {
        return userRepository.findByLoginId(loginId) != null;
    }

    private void showLoginError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Failed", JOptionPane.WARNING_MESSAGE);
    }

    private void showSignUpError(String message) {
        JOptionPane.showMessageDialog(this, message, "Sign Up Failed", JOptionPane.WARNING_MESSAGE);
    }

    private void clearLoginFields() {
        loginIdField.setText("");
        passwordField.setText("");
    }

    private void clearSignUpFields() {
        signUpLoginIdField.setText("");
        signUpPasswordField.setText("");
        signUpNameField.setText("");
        signUpJobField.setText("");
    }

    private void openCalendarApp() {
        JFrame calendarFrame = calendarFrameFactory.get();
        calendarFrame.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            dispose();
            calendarFrame.toFront();
        });
    }
}
