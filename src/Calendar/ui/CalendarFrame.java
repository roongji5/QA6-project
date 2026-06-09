package Calendar.ui;

import Calendar.repository.DbScheduleRepository;
import Calendar.repository.DbUserRepository;
import Calendar.repository.ScheduleRepository;
import Calendar.repository.UserRepository;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

public class CalendarFrame extends JFrame {

    public static final double SCREEN_SIZE_RATIO = 0.7;
    private final UserRepository userRepository = new DbUserRepository();
    private final ScheduleRepository scheduleRepository = new DbScheduleRepository();

    public CalendarFrame() {
        setTitle("Calendar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(calculateMainWindowSize());
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        setContentPane(new CalendarPanel(userRepository, scheduleRepository));
    }

    public static Dimension calculateMainWindowSize() {
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        return new Dimension(
                (int) (screenBounds.width * SCREEN_SIZE_RATIO),
                (int) (screenBounds.height * SCREEN_SIZE_RATIO)
        );
    }

}
