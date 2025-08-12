package com.campuseventhub.gui.organizer;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Calendar;

/**
 * A user-friendly date and time selector component combining calendar picker and time dropdowns.
 */
public class DateTimeSelector extends JPanel {
    private JComboBox<Integer> yearCombo;
    private JComboBox<Integer> monthCombo;
    private JComboBox<Integer> dayCombo;
    private JComboBox<Integer> hourCombo;
    private JComboBox<Integer> minuteCombo;
    
    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };
    
    public DateTimeSelector() {
        initializeComponents();
        setDefaultToCurrentTime();
    }
    
    private void initializeComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        // Year dropdown (current year to 5 years ahead)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Integer[] years = new Integer[6];
        for (int i = 0; i < 6; i++) {
            years[i] = currentYear + i;
        }
        yearCombo = new JComboBox<>(years);
        
        // Month dropdown
        Integer[] months = new Integer[12];
        for (int i = 0; i < 12; i++) {
            months[i] = i + 1;
        }
        monthCombo = new JComboBox<>(months);
        monthCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Integer) {
                    setText(MONTH_NAMES[((Integer) value) - 1]);
                }
                return this;
            }
        });
        
        // Day dropdown
        Integer[] days = new Integer[31];
        for (int i = 0; i < 31; i++) {
            days[i] = i + 1;
        }
        dayCombo = new JComboBox<>(days);
        
        // Hour dropdown (0-23)
        Integer[] hours = new Integer[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = i;
        }
        hourCombo = new JComboBox<>(hours);
        hourCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Integer) {
                    int hour = (Integer) value;
                    setText(String.format("%02d:00", hour));
                }
                return this;
            }
        });
        
        // Minute dropdown (every 5 minutes: 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)
        Integer[] minutes = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55};
        minuteCombo = new JComboBox<>(minutes);
        minuteCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Integer) {
                    setText(String.format(":%02d", (Integer) value));
                }
                return this;
            }
        });
        
        // Add month/day change listener to update available days
        monthCombo.addActionListener(e -> updateAvailableDays());
        yearCombo.addActionListener(e -> updateAvailableDays());
        
        // Add components
        add(new JLabel("Date:"));
        add(monthCombo);
        add(dayCombo);
        add(yearCombo);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(new JLabel("Time:"));
        add(hourCombo);
        add(minuteCombo);
    }
    
    private void setDefaultToCurrentTime() {
        LocalDateTime now = LocalDateTime.now().plusDays(1); // Default to tomorrow
        
        yearCombo.setSelectedItem(now.getYear());
        monthCombo.setSelectedItem(now.getMonthValue());
        dayCombo.setSelectedItem(now.getDayOfMonth());
        
        // Round up to next 5-minute interval
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();
        
        // Round up to next 5-minute interval
        currentMinute = ((currentMinute / 5) + 1) * 5;
        if (currentMinute >= 60) {
            currentHour++;
            currentMinute = 0;
        }
        
        hourCombo.setSelectedItem(currentHour);
        minuteCombo.setSelectedItem(currentMinute);
    }
    
    private void updateAvailableDays() {
        Integer selectedYear = (Integer) yearCombo.getSelectedItem();
        Integer selectedMonth = (Integer) monthCombo.getSelectedItem();
        
        if (selectedYear != null && selectedMonth != null) {
            // Calculate max days in the selected month/year
            LocalDate date = LocalDate.of(selectedYear, selectedMonth, 1);
            int maxDays = date.lengthOfMonth();
            
            // Update day combo
            Integer currentDay = (Integer) dayCombo.getSelectedItem();
            dayCombo.removeAllItems();
            
            for (int i = 1; i <= maxDays; i++) {
                dayCombo.addItem(i);
            }
            
            // Restore selection if still valid
            if (currentDay != null && currentDay <= maxDays) {
                dayCombo.setSelectedItem(currentDay);
            }
        }
    }
    
    /**
     * Gets the selected date and time as LocalDateTime
     */
    public LocalDateTime getDateTime() {
        Integer year = (Integer) yearCombo.getSelectedItem();
        Integer month = (Integer) monthCombo.getSelectedItem();
        Integer day = (Integer) dayCombo.getSelectedItem();
        Integer hour = (Integer) hourCombo.getSelectedItem();
        Integer minute = (Integer) minuteCombo.getSelectedItem();
        
        if (year == null || month == null || day == null || hour == null || minute == null) {
            return null;
        }
        
        try {
            return LocalDateTime.of(year, month, day, hour, minute);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Sets the date and time
     */
    public void setDateTime(LocalDateTime dateTime) {
        if (dateTime != null) {
            yearCombo.setSelectedItem(dateTime.getYear());
            monthCombo.setSelectedItem(dateTime.getMonthValue());
            dayCombo.setSelectedItem(dateTime.getDayOfMonth());
            hourCombo.setSelectedItem(dateTime.getHour());
            
            // Round to nearest 5-minute interval
            int minute = dateTime.getMinute();
            minute = (minute / 5) * 5; // Round down to nearest 5-minute interval
            
            minuteCombo.setSelectedItem(minute);
        }
    }
    
    /**
     * Clears the selection
     */
    public void clear() {
        setDefaultToCurrentTime();
    }
}