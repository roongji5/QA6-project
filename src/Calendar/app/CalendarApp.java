package Calendar.app;

import Calendar.repository.DatabaseManager;
import Calendar.repository.DbUserRepository;
import Calendar.ui.CalendarFrame;
import Calendar.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class CalendarApp {

    public static void main(String[] args) {
        DatabaseManager.initialize();

        SwingUtilities.invokeLater(() -> {
            DbUserRepository userRepository = new DbUserRepository();
            LoginFrame loginFrame = new LoginFrame(userRepository, CalendarFrame::new);
            loginFrame.setVisible(true);
        });
    }
}
