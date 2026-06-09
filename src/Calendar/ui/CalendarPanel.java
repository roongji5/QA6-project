package Calendar.ui;

import Calendar.model.RecommendedSchedule;
import Calendar.model.Schedule;
import Calendar.model.User;
import Calendar.repository.ScheduleRepository;
import Calendar.repository.UserRepository;
import Calendar.session.UserSession;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    private static final ScheduleColorOption[] SCHEDULE_COLOR_OPTIONS = {
            new ScheduleColorOption("Blue", "#5470E0"),
            new ScheduleColorOption("Sky", "#0EA5E9"),
            new ScheduleColorOption("Teal", "#14B8A6"),
            new ScheduleColorOption("Green", "#22C55E"),
            new ScheduleColorOption("Yellow", "#EAB308"),
            new ScheduleColorOption("Orange", "#F97316"),
            new ScheduleColorOption("Red", "#EF4444"),
            new ScheduleColorOption("Pink", "#EC4899"),
            new ScheduleColorOption("Purple", "#8B5CF6"),
            new ScheduleColorOption("Gray", "#64748B")
    };
    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy 년 M 월", Locale.KOREAN);
    private static final DateTimeFormatter DETAIL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel dayGridPanel = new JPanel(new GridLayout(CALENDAR_ROWS, CALENDAR_COLUMNS, 0, 0));
    private List<Schedule> currentUserSchedules = Collections.emptyList();
    private YearMonth currentMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();
    private boolean recommendationChecked;

    public CalendarPanel(UserRepository userRepository, ScheduleRepository scheduleRepository) {
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;

        setLayout(new BorderLayout(0, 16));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCalendarBody(), BorderLayout.CENTER);
        refreshCalendar();
        SwingUtilities.invokeLater(this::showRecommendedScheduleIfAvailable);
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

        JButton userInfoButton = new JButton("내 정보");
        userInfoButton.addActionListener(event -> {
            Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (owner instanceof javax.swing.JFrame frame) {
                new UserInfoDialog(frame, userRepository).setVisible(true);
            }
        });

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
        addHeaderButton(leftButtonPanel, previousButton, 0, 8);
        addHeaderButton(leftButtonPanel, todayButton, 1, 0);

        JPanel rightButtonPanel = new JPanel(new GridBagLayout());
        rightButtonPanel.setOpaque(false);
        addHeaderButton(rightButtonPanel, userInfoButton, 0, 8);
        addHeaderButton(rightButtonPanel, nextButton, 1, 0);

        Dimension sideSize = getBalancedSideSize(leftButtonPanel, rightButtonPanel);
        leftButtonPanel.setPreferredSize(sideSize);
        rightButtonPanel.setPreferredSize(sideSize);

        headerPanel.add(leftButtonPanel, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(rightButtonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void addHeaderButton(JPanel panel, JButton button, int column, int rightInset) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.insets = new Insets(0, 0, 0, rightInset);
        panel.add(button, gbc);
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

        JPanel weekHeaderPanel = new JPanel(new GridLayout(1, CALENDAR_COLUMNS, 0, 0));
        weekHeaderPanel.setBackground(new Color(241, 244, 248));
        weekHeaderPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, BORDER_COLOR));

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
        dayGridPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));
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
        currentUserSchedules = getCurrentUserSchedules();

        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        LocalDate firstCellDate = firstDayOfMonth.minusDays(getSundayBasedDayIndex(firstDayOfMonth.getDayOfWeek()));

        for (int i = 0; i < CALENDAR_ROWS * CALENDAR_COLUMNS; i++) {
            LocalDate cellDate = firstCellDate.plusDays(i);
            dayGridPanel.add(createDayCell(cellDate, getSchedulesForDate(cellDate)));
        }

        dayGridPanel.revalidate();
        dayGridPanel.repaint();
    }

    private JPanel createDayCell(LocalDate date, List<Schedule> schedules) {
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
        cellPanel.add(createScheduleListPanel(schedules), BorderLayout.CENTER);
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
        showScheduleCreateDialog(date, null);
    }

    private void showScheduleCreateDialog(LocalDate date, RecommendedSchedule recommendedSchedule) {
        Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "일정 추가 - " + date, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 16));
        dialog.getContentPane().setBackground(Color.WHITE);

        JTextField titleField = new JTextField(24);
        JComboBox<LocalDate> startDateComboBox = new JComboBox<>(createScheduleDateOptions());
        JComboBox<String> startTimeComboBox = new JComboBox<>(createTimeOptions());
        JComboBox<LocalDate> endDateComboBox = new JComboBox<>(createScheduleDateOptions());
        JComboBox<String> endTimeComboBox = new JComboBox<>(createTimeOptions());
        JComboBox<ScheduleColorOption> colorComboBox = createColorComboBox();
        JTextArea descriptionArea = new JTextArea(5, 24);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        configureScheduleComboBoxes(startDateComboBox, startTimeComboBox, endDateComboBox, endTimeComboBox);

        if (recommendedSchedule == null) {
            startDateComboBox.setSelectedItem(date);
            startTimeComboBox.setSelectedItem("09:00");
            endDateComboBox.setSelectedItem(date);
            endTimeComboBox.setSelectedItem("10:00");
        } else {
            titleField.setText(recommendedSchedule.title());
            startDateComboBox.setSelectedItem(recommendedSchedule.startDateTime().toLocalDate());
            startTimeComboBox.setSelectedItem(recommendedSchedule.startDateTime().toLocalTime().toString());
            endDateComboBox.setSelectedItem(recommendedSchedule.endDateTime().toLocalDate());
            endTimeComboBox.setSelectedItem(recommendedSchedule.endDateTime().toLocalTime().toString());
            descriptionArea.setText(recommendedSchedule.description());
        }

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));
        addScheduleFormRow(formPanel, 0, "일정 이름", titleField);
        addScheduleFormRow(formPanel, 1, "시작 시간", createDateTimePickerPanel(startDateComboBox, startTimeComboBox));
        addScheduleFormRow(formPanel, 2, "종료 시간", createDateTimePickerPanel(endDateComboBox, endTimeComboBox));
        addScheduleFormRow(formPanel, 3, "색상", colorComboBox);
        addScheduleFormRow(formPanel, 4, "상세 설명", new JScrollPane(descriptionArea));

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dialog.dispose());

        JButton createButton = new JButton("일정 생성");
        createButton.addActionListener(event -> saveSchedule(
                dialog,
                null,
                titleField,
                startDateComboBox,
                startTimeComboBox,
                endDateComboBox,
                endTimeComboBox,
                colorComboBox,
                descriptionArea
        ));

        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 0;
        buttonGbc.insets = new Insets(0, 0, 0, 8);
        buttonPanel.add(cancelButton, buttonGbc);

        buttonGbc.gridx = 1;
        buttonGbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(createButton, buttonGbc);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(createButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(480, 360));
        dialog.setLocationRelativeTo(this);
        DialogShortcuts.installCloseShortcut(dialog);
        dialog.setVisible(true);
    }

    private JPanel createDateTimePickerPanel(JComboBox<LocalDate> dateComboBox, JComboBox<String> timeComboBox) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);
        panel.add(dateComboBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(timeComboBox, gbc);

        return panel;
    }

    private JComboBox<ScheduleColorOption> createColorComboBox() {
        JComboBox<ScheduleColorOption> comboBox = new JComboBox<>(SCHEDULE_COLOR_OPTIONS);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ScheduleColorOption option) {
                    label.setText(option.name());
                    label.setIcon(new ColorSwatchIcon(parseScheduleColor(option.hex())));
                }
                return label;
            }
        });
        return comboBox;
    }

    private void configureScheduleComboBoxes(JComboBox<LocalDate> startDateComboBox,
                                             JComboBox<String> startTimeComboBox,
                                             JComboBox<LocalDate> endDateComboBox,
                                             JComboBox<String> endTimeComboBox) {
        startDateComboBox.setMaximumRowCount(12);
        startTimeComboBox.setMaximumRowCount(12);
        endDateComboBox.setMaximumRowCount(12);
        endTimeComboBox.setMaximumRowCount(12);
    }

    private void addScheduleFormRow(JPanel formPanel, int row, String labelText, Component input) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(8, 0, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        formPanel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(input, gbc);
    }

    private void saveSchedule(JDialog dialog, Schedule originalSchedule, JTextField titleField,
                              JComboBox<LocalDate> startDateComboBox, JComboBox<String> startTimeComboBox,
                              JComboBox<LocalDate> endDateComboBox, JComboBox<String> endTimeComboBox,
                              JComboBox<ScheduleColorOption> colorComboBox, JTextArea descriptionArea) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(dialog, "로그인 정보가 없습니다.", "일정 저장 실패",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = titleField.getText().trim();
        if (title.isBlank()) {
            JOptionPane.showMessageDialog(dialog, "일정 이름을 입력하세요.", "일정 저장 실패",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(
                (LocalDate) startDateComboBox.getSelectedItem(),
                LocalTime.parse((String) startTimeComboBox.getSelectedItem())
        );
        LocalDateTime endDateTime = LocalDateTime.of(
                (LocalDate) endDateComboBox.getSelectedItem(),
                LocalTime.parse((String) endTimeComboBox.getSelectedItem())
        );

        if (!startDateTime.isBefore(endDateTime)) {
            JOptionPane.showMessageDialog(dialog, "종료 시간은 시작 시간보다 늦어야 합니다.", "일정 저장 실패",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String description = descriptionArea.getText().trim();
        ScheduleColorOption selectedColor = (ScheduleColorOption) colorComboBox.getSelectedItem();
        Schedule schedule = new Schedule(
                originalSchedule == null ? 0 : originalSchedule.getId(),
                currentUser.getUserID(),
                title,
                startDateTime,
                endDateTime,
                description.isBlank() ? null : description,
                selectedColor == null ? Schedule.DEFAULT_COLOR_HEX : selectedColor.hex()
        );
        if (originalSchedule != null) {
            schedule.setCompleted(originalSchedule.isCompleted());
        }

        try {
            if (originalSchedule == null) {
                scheduleRepository.addSchedule(schedule);
            } else {
                scheduleRepository.update(schedule);
            }
            JOptionPane.showMessageDialog(dialog, originalSchedule == null ? "일정이 생성되었습니다." : "일정이 수정되었습니다.",
                    originalSchedule == null ? "일정 생성" : "일정 수정",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            refreshCalendar();
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(dialog, exception.getMessage(), "일정 저장 실패",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate[] createScheduleDateOptions() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusYears(1);
        LocalDate[] dates = new LocalDate[731];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = startDate.plusDays(i);
        }
        return dates;
    }

    private String[] createTimeOptions() {
        String[] times = new String[288];
        for (int i = 0; i < times.length; i++) {
            LocalTime time = LocalTime.MIDNIGHT.plusMinutes(i * 5L);
            times[i] = time.toString();
        }
        return times;
    }

    private List<Schedule> getSchedulesForDate(LocalDate date) {
        return currentUserSchedules.stream()
                .filter(schedule -> isScheduleVisibleOnDate(schedule, date))
                .sorted(Comparator.comparing(Schedule::getStartDateTime))
                .collect(Collectors.toList());
    }

    private boolean isScheduleVisibleOnDate(Schedule schedule, LocalDate date) {
        LocalDate startDate = schedule.getStartDateTime().toLocalDate();
        LocalDate endDate = schedule.getEndDateTime().toLocalDate();
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    private List<Schedule> getCurrentUserSchedules() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return Collections.emptyList();
        }

        try {
            return scheduleRepository.findByUserId(currentUser.getUserID());
        } catch (RuntimeException exception) {
            return Collections.emptyList();
        }
    }

    private void showRecommendedScheduleIfAvailable() {
        if (recommendationChecked) {
            return;
        }
        recommendationChecked = true;

        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        RecommendedSchedule recommendedSchedule = RecommendedSchedule.forJob(currentUser.getJob(), LocalDate.now());
        if (recommendedSchedule == null || hasOverlappingSchedule(recommendedSchedule)) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "오늘 " + recommendedSchedule.startDateTime().toLocalTime() + " ~ "
                        + recommendedSchedule.endDateTime().toLocalTime() + "에 \""
                        + recommendedSchedule.title() + "\" 일정을 추가해볼까요?",
                "추천 일정",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION) {
            showScheduleCreateDialog(recommendedSchedule.startDateTime().toLocalDate(), recommendedSchedule);
        }
    }

    private boolean hasOverlappingSchedule(RecommendedSchedule recommendedSchedule) {
        LocalDateTime recommendedStart = recommendedSchedule.startDateTime();
        LocalDateTime recommendedEnd = recommendedSchedule.endDateTime();
        return currentUserSchedules.stream()
                .anyMatch(schedule -> schedule.getStartDateTime().isBefore(recommendedEnd)
                        && schedule.getEndDateTime().isAfter(recommendedStart));
    }

    private JPanel createScheduleListPanel(List<Schedule> schedules) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(42, 4, 4, 4));

        int visibleCount = Math.min(schedules.size(), 3);
        for (int i = 0; i < visibleCount; i++) {
            Schedule schedule = schedules.get(i);
            JLabel scheduleLabel = new JLabel(schedule.getTitle());
            scheduleLabel.setOpaque(true);
            scheduleLabel.setBackground(parseScheduleColor(schedule.getColorHex()));
            scheduleLabel.setForeground(Color.WHITE);
            scheduleLabel.setFont(scheduleLabel.getFont().deriveFont(Font.BOLD, 13f));
            scheduleLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            scheduleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            scheduleLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    event.consume();
                }

                @Override
                public void mouseClicked(MouseEvent event) {
                    event.consume();
                    showScheduleDetailDialog(schedule);
                }
            });

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 2, 0);
            panel.add(scheduleLabel, gbc);
        }

        if (schedules.size() > visibleCount) {
            JLabel moreLabel = new JLabel("+" + (schedules.size() - visibleCount));
            moreLabel.setForeground(MUTED_TEXT_COLOR);
            moreLabel.setFont(moreLabel.getFont().deriveFont(Font.BOLD, 11f));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = visibleCount;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(moreLabel, gbc);
        }

        GridBagConstraints spacerGbc = new GridBagConstraints();
        spacerGbc.gridx = 0;
        spacerGbc.gridy = visibleCount + 1;
        spacerGbc.weighty = 1;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        panel.add(spacer, spacerGbc);

        return panel;
    }

    private void showScheduleDetailDialog(Schedule schedule) {
        Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "일정 정보", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 16));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));

        addDetailRow(infoPanel, 0, "일정 이름", schedule.getTitle());
        addDetailRow(infoPanel, 1, "시작 시간", schedule.getStartDateTime().format(DETAIL_FORMATTER));
        addDetailRow(infoPanel, 2, "종료 시간", schedule.getEndDateTime().format(DETAIL_FORMATTER));
        addDetailRow(infoPanel, 3, "상세 설명", schedule.getDescription() == null ? "" : schedule.getDescription());

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));

        JButton closeButton = new JButton("창 닫기");
        closeButton.addActionListener(event -> dialog.dispose());

        JButton editButton = new JButton("일정 수정");
        editButton.addActionListener(event -> {
            dialog.dispose();
            showEditScheduleDialog(schedule);
        });

        JButton deleteButton = new JButton("일정 삭제");
        deleteButton.addActionListener(event -> deleteSchedule(dialog, schedule));

        addDialogButton(buttonPanel, closeButton, 0, 8);
        addDialogButton(buttonPanel, editButton, 1, 8);
        addDialogButton(buttonPanel, deleteButton, 2, 0);

        dialog.add(infoPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(closeButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 260));
        dialog.setLocationRelativeTo(this);
        DialogShortcuts.installCloseShortcut(dialog);
        dialog.setVisible(true);
    }

    private void showEditScheduleDialog(Schedule schedule) {
        Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "일정 수정", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 16));
        dialog.getContentPane().setBackground(Color.WHITE);

        JTextField titleField = new JTextField(schedule.getTitle(), 24);
        JComboBox<LocalDate> startDateComboBox = new JComboBox<>(createScheduleDateOptions());
        JComboBox<String> startTimeComboBox = new JComboBox<>(createTimeOptions());
        JComboBox<LocalDate> endDateComboBox = new JComboBox<>(createScheduleDateOptions());
        JComboBox<String> endTimeComboBox = new JComboBox<>(createTimeOptions());
        JComboBox<ScheduleColorOption> colorComboBox = createColorComboBox();
        JTextArea descriptionArea = new JTextArea(schedule.getDescription() == null ? "" : schedule.getDescription(), 5, 24);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        configureScheduleComboBoxes(startDateComboBox, startTimeComboBox, endDateComboBox, endTimeComboBox);
        startDateComboBox.setSelectedItem(schedule.getStartDateTime().toLocalDate());
        startTimeComboBox.setSelectedItem(schedule.getStartDateTime().toLocalTime().toString());
        endDateComboBox.setSelectedItem(schedule.getEndDateTime().toLocalDate());
        endTimeComboBox.setSelectedItem(schedule.getEndDateTime().toLocalTime().toString());
        colorComboBox.setSelectedItem(findColorOption(schedule.getColorHex()));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));
        addScheduleFormRow(formPanel, 0, "일정 이름", titleField);
        addScheduleFormRow(formPanel, 1, "시작 시간", createDateTimePickerPanel(startDateComboBox, startTimeComboBox));
        addScheduleFormRow(formPanel, 2, "종료 시간", createDateTimePickerPanel(endDateComboBox, endTimeComboBox));
        addScheduleFormRow(formPanel, 3, "색상", colorComboBox);
        addScheduleFormRow(formPanel, 4, "상세 설명", new JScrollPane(descriptionArea));

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dialog.dispose());

        JButton updateButton = new JButton("일정 수정");
        updateButton.addActionListener(event -> saveSchedule(
                dialog,
                schedule,
                titleField,
                startDateComboBox,
                startTimeComboBox,
                endDateComboBox,
                endTimeComboBox,
                colorComboBox,
                descriptionArea
        ));

        addDialogButton(buttonPanel, cancelButton, 0, 8);
        addDialogButton(buttonPanel, updateButton, 1, 0);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(updateButton);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(480, 360));
        dialog.setLocationRelativeTo(this);
        DialogShortcuts.installCloseShortcut(dialog);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, int row, String labelText, String valueText) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(8, 0, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(new JLabel(valueText), gbc);
    }

    private void addDialogButton(JPanel panel, JButton button, int column, int rightInset) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.insets = new Insets(0, 0, 0, rightInset);
        panel.add(button, gbc);
    }

    private void deleteSchedule(JDialog dialog, Schedule schedule) {
        int result = JOptionPane.showConfirmDialog(
                dialog,
                "이 일정을 삭제할까요?",
                "일정 삭제",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            scheduleRepository.delete(schedule.getId());
            dialog.dispose();
            refreshCalendar();
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(dialog, exception.getMessage(), "일정 삭제 실패",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ScheduleColorOption findColorOption(String colorHex) {
        for (ScheduleColorOption option : SCHEDULE_COLOR_OPTIONS) {
            if (option.hex().equalsIgnoreCase(colorHex)) {
                return option;
            }
        }
        return SCHEDULE_COLOR_OPTIONS[0];
    }

    private Color parseScheduleColor(String colorHex) {
        try {
            return Color.decode(colorHex);
        } catch (NumberFormatException exception) {
            return Color.decode(Schedule.DEFAULT_COLOR_HEX);
        }
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
        DialogShortcuts.installCloseShortcut(dialog);
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

    private record ScheduleColorOption(String name, String hex) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static class ColorSwatchIcon implements Icon {
        private static final int SIZE = 12;
        private final Color color;

        ColorSwatchIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(x, y + 2, SIZE, SIZE, 4, 4);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(x, y + 2, SIZE, SIZE, 4, 4);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE + 6;
        }

        @Override
        public int getIconHeight() {
            return SIZE + 4;
        }
    }
}
