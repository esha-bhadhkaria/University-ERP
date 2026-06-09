package edu.univ.erp.ui.admin;

import edu.univ.erp.data.CourseDao;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AdminService.CreateCourseResult;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Window for Admin to create/edit Courses, Sections, and manage Drop Deadlines.
public class ManageCoursesWindow extends JDialog {

    private final AdminService adminService;
    private final CourseDao courseDao;
    private final CourseTableModel courseTableModel;
    private final JTable courseTable;
    private final SectionTableModel sectionTableModel;
    private final JTable sectionTable;
    private static final int CURRENT_SEMESTER = 1;
    private static final int CURRENT_YEAR = 2024;

    public ManageCoursesWindow(Frame owner) {
        super(owner, "Manage Courses, Sections, and Deadlines", true);
        this.adminService = new AdminService();
        this.courseDao = new CourseDao();
        this.courseTableModel = new CourseTableModel(Collections.emptyList());
        this.sectionTableModel = new SectionTableModel(Collections.emptyList());

        setSize(1200, 800);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        //Manage Courses (Code, Title, Credits)
        this.courseTable = new JTable(courseTableModel);
        this.courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabbedPane.addTab("1. Manage Courses", createManageCoursesPanel());

        // Manage Sections
        this.sectionTable = new JTable(sectionTableModel);
        this.sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabbedPane.addTab("2. Manage Sections", createManageSectionsPanel());

        // Edit Drop Deadline
        tabbedPane.addTab("3. Edit Drop Deadline", new DropDeadlinePanel());

        add(tabbedPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadCourses();
        loadSections();
    }

    private void loadCourses() {
        List<Course> courses = courseDao.getAllCourses();
        courseTableModel.setCourses(courses);
    }

    private void loadSections() {
        List<Section> sections = adminService.getAllSections(CURRENT_SEMESTER, CURRENT_YEAR);
        sectionTableModel.setSections(sections);
    }

    private JPanel createManageCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(courseTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add New Course");
        addButton.addActionListener(e -> addNewCourse());

        JButton editButton = new JButton("Edit Selected Course");
        editButton.addActionListener(e -> editSelectedCourse());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(Box.createHorizontalStrut(20)); // Spacer
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
//Handles adding new course
    private void addNewCourse() {
        JPanel formPanel = new JPanel(new MigLayout("wrap 2, insets 15", "[right]",""));
        JTextField codeField = new JTextField(15);
        JTextField titleField = new JTextField(25);
        JTextField creditsField = new JTextField(5);
        formPanel.add(new JLabel("Course Code (e.g., CSE302):"));
        formPanel.add(codeField, "growx");
        formPanel.add(new JLabel("Course Title:"));
        formPanel.add(titleField, "growx");
        formPanel.add(new JLabel("Credits:"));
        formPanel.add(creditsField, "growx");

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Add New Course",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String creditsStr = creditsField.getText().trim();

            try {
                int credits = Integer.parseInt(creditsStr);
                CreateCourseResult serviceResult = adminService.createCourse(code, title, credits);

                if (serviceResult.success) {
                    JOptionPane.showMessageDialog(this, serviceResult.message, "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCourses(); // Refresh the table!
                } else {
                    JOptionPane.showMessageDialog(this, serviceResult.message, "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Credits must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
//Handles editing selected course
    private void editSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Course course = courseTableModel.getCourseAt(selectedRow);
        EditCourseDialog dialog = new EditCourseDialog(this, course);
        dialog.setVisible(true);
        loadCourses();
    }

    private JPanel createManageSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(sectionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Create New Section");
        createButton.addActionListener(e -> createNewSection());

        JButton editButton = new JButton("Edit Selected Section");
        editButton.addActionListener(e -> editSelectedSection());

        JButton assignButton = new JButton("Assign Instructor");
        assignButton.addActionListener(e -> assignInstructor());

        JButton deleteButton = new JButton("Delete Selected Section");
        deleteButton.addActionListener(e -> deleteSelectedSection());
        buttonPanel.add(deleteButton);
        buttonPanel.add(createButton);
        buttonPanel.add(editButton);
        buttonPanel.add(assignButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
//Creates new section when user ask's
    private void createNewSection() {
        List<Course> allCourses = courseDao.getAllCourses();
        if (allCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please create a course first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JComboBox<Course> courseComboBox = new JComboBox<>(allCourses.toArray(new Course[0]));
        JComboBox<String> dayComboBox = new JComboBox<>(new String[]{"1 (Mon)", "2 (Tue)", "3 (Wed)", "4 (Thu)", "5 (Fri)"});
        JTextField startTimeField = new JTextField("09:00:00");
        JTextField endTimeField = new JTextField("10:30:00");
        JTextField roomField = new JTextField("A101");
        JTextField capacityField = new JTextField("50");
        JTextField semesterField = new JTextField(String.valueOf(CURRENT_SEMESTER));
        JTextField yearField = new JTextField(String.valueOf(CURRENT_YEAR));

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        formPanel.add(new JLabel("Course:")); formPanel.add(courseComboBox);
        formPanel.add(new JLabel("Day of Week:")); formPanel.add(dayComboBox);
        formPanel.add(new JLabel("Start Time (HH:MM:SS):")); formPanel.add(startTimeField);
        formPanel.add(new JLabel("End Time (HH:MM:SS):")); formPanel.add(endTimeField);
        formPanel.add(new JLabel("Room:")); formPanel.add(roomField);
        formPanel.add(new JLabel("Capacity:")); formPanel.add(capacityField);
        formPanel.add(new JLabel("Semester:")); formPanel.add(semesterField);
        formPanel.add(new JLabel("Year:")); formPanel.add(yearField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Create New Section",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Section newSection = new Section();
                newSection.setCourseid(((Course) courseComboBox.getSelectedItem()).getCourseid());
                newSection.setDayofweek(Integer.parseInt(((String) dayComboBox.getSelectedItem()).substring(0, 1)));
                newSection.setStarttime(LocalTime.parse(startTimeField.getText().trim()));
                newSection.setEndtime(LocalTime.parse(endTimeField.getText().trim()));
                newSection.setRoom(roomField.getText().trim());
                newSection.setCapacity(Integer.parseInt(capacityField.getText().trim()));
                newSection.setSemester(Integer.parseInt(semesterField.getText().trim()));
                newSection.setYear(Integer.parseInt(yearField.getText().trim()));

                if (adminService.createSection(newSection)) {
                    JOptionPane.showMessageDialog(this, "Section created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSections();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create section.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Check times (HH:MM:SS) and numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    //Delete section if no one enrolled
    private void deleteSelectedSection() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Section sectionToDelete = sectionTableModel.getSectionAt(selectedRow);
        int confirmation = JOptionPane.showConfirmDialog(this,
                "WARNING: Are you sure you want to permanently delete section " + sectionToDelete.getCourseCode() + " (" + sectionToDelete.getSectionid() + ")? ",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            AdminService.DeleteSectionResult result = adminService.deleteSection(sectionToDelete.getSectionid());

            if (result.success) {
                JOptionPane.showMessageDialog(this, result.message, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSections();
            } else {
                JOptionPane.showMessageDialog(this, result.message, "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void editSelectedSection() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to edit.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Section section = sectionTableModel.getSectionAt(selectedRow);

        EditSectionDialog dialog = new EditSectionDialog(this, section);
        dialog.setVisible(true);
        loadSections();
    }

    private void assignInstructor() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section from the table first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Section selectedSection = sectionTableModel.getSectionAt(selectedRow);

        List<Instructor> allInstructors = adminService.getAllInstructors();
        if (allInstructors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No instructors found. Please create an instructor first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<Instructor> instructorComboBox = new JComboBox<>(allInstructors.toArray(new Instructor[0]));
        Instructor currentInstructor = allInstructors.stream()
                .filter(i -> i.getUserid() == selectedSection.getInstructorid())
                .findFirst().orElse(null);
        if (currentInstructor != null) {
            instructorComboBox.setSelectedItem(currentInstructor);
        } else {
            instructorComboBox.setSelectedIndex(-1);
        }
        int result = JOptionPane.showConfirmDialog(this, instructorComboBox,
                "Assign Instructor to " + selectedSection.getCourseCode(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Instructor selectedInstructor = (Instructor) instructorComboBox.getSelectedItem();
            if (selectedInstructor == null) {
                JOptionPane.showMessageDialog(this, "No instructor selected.", "Assignment Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (adminService.assignInstructor(selectedSection.getSectionid(), selectedInstructor.getUserid())) {
                JOptionPane.showMessageDialog(this, "Instructor assigned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSections(); // Refresh table to show new instructor
            } else {
                JOptionPane.showMessageDialog(this, "Failed to assign instructor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class CourseTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID", "Code", "Title", "Credits"};
        private List<Course> courses;

        public CourseTableModel(List<Course> courses) {
            this.courses = new ArrayList<>(courses);
        }

        public void setCourses(List<Course> courses) {
            this.courses = new ArrayList<>(courses);
            fireTableDataChanged();
        }

        public Course getCourseAt(int row) {
            return courses.get(row);
        }

        @Override
        public int getRowCount() { return courses.size(); }
        @Override
        public int getColumnCount() { return columnNames.length; }
        @Override
        public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Course c = courses.get(row);
            return switch (col) {
                case 0 -> c.getCourseid();
                case 1 -> c.getCode();
                case 2 -> c.getTitle();
                case 3 -> c.getCredits();
                default -> null;
            };
        }
    }

    private static class SectionTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID", "Code", "Title", "Credits", "Schedule", "Room", "Cap", "Left", "Instructor"};
        private List<Section> sections;

        public SectionTableModel(List<Section> sections) {
            this.sections = new ArrayList<>(sections);
        }

        public void setSections(List<Section> sections) {
            this.sections = new ArrayList<>(sections);
            fireTableDataChanged();
        }

        public Section getSectionAt(int row) {
            return sections.get(row);
        }

        @Override
        public int getRowCount() { return sections.size(); }
        @Override
        public int getColumnCount() { return columnNames.length; }
        @Override
        public String getColumnName(int col) { return columnNames[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Section s = sections.get(row);
            return switch (col) {
                case 0 -> s.getSectionid();
                case 1 -> s.getCourseCode();
                case 2 -> s.getCourseTitle();
                case 3 -> s.getCredits();
                case 4 -> s.getDayName() + " " + s.getStarttime();
                case 5 -> s.getRoom();
                case 6 -> s.getCapacity();
                case 7 -> s.getSeatsLeft();
                case 8 -> s.getInstructorName();
                default -> null;
            };
        }
    }
}