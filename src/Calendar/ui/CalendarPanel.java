package Calendar.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CalendarPanel extends JPanel {

    private static final int CALENDAR_ROWS = 6;
    private static final int CALENDAR_COLUMNS = 7;
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 251);
    private static final Color BORDER_COLOR = new Color(218, 223, 230);
    private static final Color MUTED_TEXT_COLOR = new Color(145, 153, 164);
    private static final Color TODAY_MARK_COLOR = new Color(166, 255, 242, 120);
    private static final Color SELECTED_MARK_COLOR = new Color(0, 0, 0, 70);
    private static final Color SUNDAY_COLOR = new Color(210, 52, 52);
    private static final Color SATURDAY_COLOR = new Color(35, 96, 200);
    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy 년 M 월", Locale.KOREAN);

    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel dayGridPanel = new JPanel(new GridLayout(CALENDAR_ROWS, CALENDAR_COLUMNS, 0, 0));
    private YearMonth currentMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();

    public CalendarPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCalendarBody(), BorderLayout.CENTER);
        refreshCalendar();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JButton previousButton = new JButton("<");
        previousButton.addActionListener(event -> changeMonth(-1));

        JButton todayButton = new JButton("Today");
        todayButton.addActionListener(event -> {
            currentMonth = YearMonth.now();
            selectedDate = LocalDate.now();
            refreshCalendar();
        });

        JButton nextButton = new JButton(">");
        nextButton.addActionListener(event -> changeMonth(1));

        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 24f));
        monthLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        monthLabel.setToolTipText("년월 선택");
        monthLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                showMonthPickerDialog();
            }
        });

        JPanel leftButtonPanel = new JPanel(new GridBagLayout());
        leftButtonPanel.setOpaque(false);
        leftButtonPanel.add(previousButton);
        leftButtonPanel.add(todayButton);

        JPanel rightButtonPanel = new JPanel(new GridBagLayout());
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.add(nextButton);

        Dimension sideSize = getBalancedSideSize(leftButtonPanel, rightButtonPanel);
        leftButtonPanel.setPreferredSize(sideSize);
        rightButtonPanel.setPreferredSize(sideSize);

        headerPanel.add(leftButtonPanel, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(rightButtonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private Dimension getBalancedSideSize(JPanel leftButtonPanel, JPanel rightButtonPanel) {
        Dimension leftSize = leftButtonPanel.getPreferredSize();
        Dimension rightSize = rightButtonPanel.getPreferredSize();
        return new Dimension(
                Math.max(leftSize.width, rightSize.width),
                Math.max(leftSize.height, rightSize.height)
        );
    }

    private JPanel createCalendarBody() {
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JPanel weekHeaderPanel = new JPanel(new GridLayout(1, CALENDAR_COLUMNS, 0, 0));
        weekHeaderPanel.setBackground(new Color(241, 244, 248));

        String[] weekDays = {"일", "월", "화", "수", "목", "금", "토"};
        for (int i = 0; i < weekDays.length; i++) {
            String weekDay = weekDays[i];
            JLabel label = new JLabel(weekDay, SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
            label.setForeground(getWeekHeaderColor(i));
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_COLOR));
            weekHeaderPanel.add(label);
        }

        dayGridPanel.setBackground(Color.WHITE);
        bodyPanel.add(weekHeaderPanel, BorderLayout.NORTH);
        bodyPanel.add(dayGridPanel, BorderLayout.CENTER);
        return bodyPanel;
    }

    private void changeMonth(int amount) {
        setCurrentMonth(currentMonth.plusMonths(amount));
    }

    private void setCurrentMonth(YearMonth yearMonth) {
        currentMonth = yearMonth;
        int selectedDay = Math.min(selectedDate.getDayOfMonth(), currentMonth.lengthOfMonth());
        selectedDate = currentMonth.atDay(selectedDay);
        refreshCalendar();
    }

    private void refreshCalendar() {
        monthLabel.setText(currentMonth.format(MONTH_FORMATTER));
        dayGridPanel.removeAll();

        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        LocalDate firstCellDate = firstDayOfMonth.minusDays(getSundayBasedDayIndex(firstDayOfMonth.getDayOfWeek()));

        for (int i = 0; i < CALENDAR_ROWS * CALENDAR_COLUMNS; i++) {
            LocalDate cellDate = firstCellDate.plusDays(i);
            dayGridPanel.add(createDayCell(cellDate));
        }

        dayGridPanel.revalidate();
        dayGridPanel.repaint();
    }

    private JPanel createDayCell(LocalDate date) {
        JPanel cellPanel = new DayCellPanel(
                date,
                getCellDateTextColor(date)
        );
        cellPanel.setBackground(Color.WHITE);
        cellPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_COLOR));
        cellPanel.setPreferredSize(new Dimension(100, 78));

        MouseAdapter selectDateListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                selectDate(date);
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() >= 2) {
                    showEmptyScheduleDialog(date);
                }
            }
        };

        cellPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cellPanel.addMouseListener(selectDateListener);
        return cellPanel;
    }

    private void selectDate(LocalDate date) {
        selectedDate = date;
        YearMonth selectedMonth = YearMonth.from(date);
        if (selectedMonth.equals(currentMonth)) {
            dayGridPanel.repaint();
            return;
        }

        currentMonth = selectedMonth;
        refreshCalendar();
    }

    private void showEmptyScheduleDialog(LocalDate date) {
        Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "일정 추가 - " + date, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showMonthPickerDialog() {
        Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "년월 선택", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 16));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));

        JComboBox<Integer> yearComboBox = new JComboBox<>(createYearOptions());
        yearComboBox.setSelectedItem(currentMonth.getYear());
        yearComboBox.setMaximumRowCount(10);

        JComboBox<Integer> monthComboBox = new JComboBox<>(createMonthOptions());
        monthComboBox.setSelectedItem(currentMonth.getMonthValue());
        monthComboBox.setMaximumRowCount(12);

        addPickerRow(formPanel, 0, "Year", yearComboBox);
        addPickerRow(formPanel, 1, "Month", monthComboBox);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dialog.dispose());

        JButton applyButton = new JButton("Go");
        applyButton.addActionListener(event -> {
            int selectedYear = (Integer) yearComboBox.getSelectedItem();
            int selectedMonth = (Integer) monthComboBox.getSelectedItem();
            setCurrentMonth(YearMonth.of(selectedYear, selectedMonth));
            dialog.dispose();
        });

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 0;
        buttonGbc.insets = new Insets(0, 0, 0, 8);
        buttonPanel.add(cancelButton, buttonGbc);

        buttonGbc.gridx = 1;
        buttonGbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(applyButton, buttonGbc);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(applyButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(260, 180));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addPickerRow(JPanel formPanel, int row, String labelText, JComponent input) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(8, 0, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        formPanel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(input, gbc);
    }

    private Integer[] createYearOptions() {
        Integer[] years = new Integer[201];
        for (int i = 0; i < years.length; i++) {
            years[i] = 1900 + i;
        }
        return years;
    }

    private Integer[] createMonthOptions() {
        Integer[] months = new Integer[12];
        for (int i = 0; i < months.length; i++) {
            months[i] = i + 1;
        }
        return months;
    }

    private Color getCellDateTextColor(LocalDate date) {
        if (!YearMonth.from(date).equals(currentMonth)) {
            return MUTED_TEXT_COLOR;
        }
        return getDateTextColor(date);
    }

    private Color getDateTextColor(LocalDate date) {
        int dayIndex = getSundayBasedDayIndex(date.getDayOfWeek());
        Color weekendColor = getWeekendColor(dayIndex);
        return weekendColor != null ? weekendColor : Color.BLACK;
    }

    private Color getWeekHeaderColor(int sundayBasedDayIndex) {
        Color weekendColor = getWeekendColor(sundayBasedDayIndex);
        return weekendColor != null ? weekendColor : Color.BLACK;
    }

    private Color getWeekendColor(int sundayBasedDayIndex) {
        if (sundayBasedDayIndex == 0) {
            return SUNDAY_COLOR;
        }
        if (sundayBasedDayIndex == 6) {
            return SATURDAY_COLOR;
        }
        return null;
    }

    private int getSundayBasedDayIndex(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue() % CALENDAR_COLUMNS;
    }

    private class DayCellPanel extends JPanel {

        private static final int BADGE_SIZE = 32;
        private static final int BADGE_X = 8;
        private static final int BADGE_Y = 6;

        private final LocalDate date;
        private final Color textColor;

        DayCellPanel(LocalDate date, Color textColor) {
            super(new BorderLayout());
            this.date = date;
            this.textColor = textColor;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (LocalDate.now().equals(date)) {
                g2.setColor(TODAY_MARK_COLOR);
                g2.fillOval(BADGE_X, BADGE_Y, BADGE_SIZE, BADGE_SIZE);
            }
            if (selectedDate.equals(date)) {
                g2.setColor(SELECTED_MARK_COLOR);
                g2.fillOval(BADGE_X, BADGE_Y, BADGE_SIZE, BADGE_SIZE);
            }

            String dayText = String.valueOf(date.getDayOfMonth());
            FontMetrics metrics = g2.getFontMetrics();
            int textX = BADGE_X + (BADGE_SIZE - metrics.stringWidth(dayText)) / 2;
            int textY = BADGE_Y + ((BADGE_SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
            g2.setColor(textColor);
            g2.drawString(dayText, textX, textY);

            g2.dispose();
        }
    }
}
