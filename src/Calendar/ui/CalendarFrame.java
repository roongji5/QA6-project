package Calendar.ui;

import Calendar.repository.DbUserRepository;
import Calendar.repository.UserRepository;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class CalendarFrame extends JFrame {

    public static final double SCREEN_SIZE_RATIO = 0.7;
    private final UserRepository userRepository = new DbUserRepository();

    public CalendarFrame() {
        setTitle("Calendar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(calculateMainWindowSize());
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        setContentPane(createContent());
    }

    public static Dimension calculateMainWindowSize() {
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        return new Dimension(
                (int) (screenBounds.width * SCREEN_SIZE_RATIO),
                (int) (screenBounds.height * SCREEN_SIZE_RATIO)
        );
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(createTopBar(), BorderLayout.NORTH);
        root.add(new CalendarPanel(), BorderLayout.CENTER);
        return root;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        JButton userInfoButton = new JButton("내 정보");
        userInfoButton.addActionListener(event -> new UserInfoDialog(this, userRepository).setVisible(true));

        // 현재는 내 정보 버튼만 두고, 이후 로그아웃/설정 같은 전역 버튼을 이 영역에 추가한다.
        topBar.add(userInfoButton, BorderLayout.EAST);
        return topBar;
    }

}
