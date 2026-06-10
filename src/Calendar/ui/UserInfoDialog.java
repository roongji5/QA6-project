package Calendar.ui;

import Calendar.model.User;
import Calendar.repository.UserRepository;
import Calendar.session.UserSession;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

public class UserInfoDialog extends JDialog {

    private static final String[] JOB_OPTIONS = {"admin", "학생", "회사원", "자영업자", "기타"};

    private final UserRepository userRepository;
    private final User currentUser;
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField nameField = new JTextField(20);
    private final JComboBox<String> jobComboBox = new JComboBox<>(JOB_OPTIONS);

    public UserInfoDialog(JFrame owner, UserRepository userRepository) {
        super(owner, "내 정보", true);
        this.userRepository = userRepository;
        this.currentUser = UserSession.getCurrentUser();

        setSize(new Dimension(560, 380));
        setMinimumSize(new Dimension(460, 320));
        setLocationRelativeTo(owner);
        setContentPane(createContent());
        DialogShortcuts.installCloseShortcut(this);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel titleLabel = new JLabel("내 정보");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        root.add(titleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);

        if (currentUser == null) {
            addLabelRow(infoPanel, 0, "상태", "로그인 정보가 없습니다.");
        } else {
            fillCurrentUserValues();
            addLabelRow(infoPanel, 0, "Login ID", currentUser.getLoginID());
            addInputRow(infoPanel, 1, "Password", passwordField);
            addInputRow(infoPanel, 2, "Name", nameField);
            addInputRow(infoPanel, 3, "Job", jobComboBox);
        }

        root.add(infoPanel, BorderLayout.CENTER);
        root.add(createButtonPanel(), BorderLayout.SOUTH);
        return root;
    }

    private void fillCurrentUserValues() {
        passwordField.setText(currentUser.getPassword());
        nameField.setText(currentUser.getName());
        jobComboBox.setSelectedItem(currentUser.getJob() == null ? "" : currentUser.getJob());
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dispose());

        JButton updateButton = new JButton("Update");
        updateButton.setEnabled(currentUser != null);
        updateButton.addActionListener(event -> updateUserInfo());
        getRootPane().setDefaultButton(updateButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        buttonPanel.add(cancelButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(updateButton, gbc);

        return buttonPanel;
    }

    private void updateUserInfo() {
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        Arrays.fill(passwordChars, '\0');
        String name = nameField.getText().trim();
        Object selectedJob = jobComboBox.getSelectedItem();
        String job = selectedJob == null ? "" : selectedJob.toString().trim();

        if (password.isBlank() || name.isBlank()) {
            JOptionPane.showMessageDialog(this, "Password와 Name은 필수입니다.", "Update Failed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        User updatedUser = new User(
                currentUser.getUserID(),
                currentUser.getLoginID(),
                password,
                name,
                job.isBlank() ? null : job
        );

        // DB 수정 후 세션도 갱신해야 이후 UI가 최신 내 정보를 보여준다.
        userRepository.update(updatedUser);
        UserSession.login(updatedUser);

        JOptionPane.showMessageDialog(this, "내 정보가 수정되었습니다.", "Update",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void addLabelRow(JPanel panel, int row, String label, String value) {
        addRow(panel, row, label, new JLabel(value));
    }

    private void addInputRow(JPanel panel, int row, String label, java.awt.Component input) {
        addRow(panel, row, label, input);
    }

    private void addRow(JPanel panel, int row, String label, java.awt.Component valueComponent) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(8, 0, 8, 20);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(valueComponent, gbc);
    }
}
