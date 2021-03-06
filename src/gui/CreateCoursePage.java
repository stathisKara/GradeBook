package gui;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import data.DatabaseAPI;
import model.Course;
import model.Semester;

import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class CreateCoursePage {

	private JFrame frame;
	private JTextField nameTextField;
	private JTextField codeTextField;
	private JTextField yearTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CreateCoursePage window = new CreateCoursePage();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public CreateCoursePage() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setVisible(true);
		frame.setBounds(100, 100, 300, 350);
		//frame.setLocationRelativeTo(null);
		// set the window in the middle of screen
		int windowWidth = frame.getWidth(); 
		int windowHeight = frame.getHeight(); 
		Toolkit kit = Toolkit.getDefaultToolkit(); 
		Dimension screenSize = kit.getScreenSize(); 
		int screenWidth = screenSize.width; 
		int screenHeight = screenSize.height; 
		frame.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);
				
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Create New Course");
		
		JLabel createNewCourseLabel = new JLabel("Create your new course");
		createNewCourseLabel.setHorizontalAlignment(SwingConstants.CENTER);
		createNewCourseLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		createNewCourseLabel.setBounds(50, 10, 200, 60);
		frame.getContentPane().add(createNewCourseLabel);
		
		JLabel nameLabel = new JLabel("Name (eg. Java Development)");
		nameLabel.setBounds(50, 60, 190, 20);
		frame.getContentPane().add(nameLabel);
		
		nameTextField = new JTextField();
		nameTextField.setToolTipText("");
		nameTextField.setBounds(50, 80, 175, 30);
		frame.getContentPane().add(nameTextField);
		nameTextField.setColumns(10);
		
		JLabel codeLabel = new JLabel("Code (eg. 591)");
		codeLabel.setBounds(50, 115, 190, 20);
		frame.getContentPane().add(codeLabel);
		
		codeTextField = new JTextField();
		codeTextField.setBounds(50, 135, 175, 30);
		frame.getContentPane().add(codeTextField);
		codeTextField.setColumns(10);
		
		JLabel yearLabel = new JLabel("Year (eg. 2019)");
		yearLabel.setBounds(50, 170, 180, 20);
		frame.getContentPane().add(yearLabel);
		
		yearTextField = new JTextField();
		yearTextField.setBounds(50, 190, 175, 30);
		frame.getContentPane().add(yearTextField);
		yearTextField.setColumns(10);
		
		JLabel semesterLabel = new JLabel("Semester");
		semesterLabel.setBounds(50, 225, 80, 20);
		frame.getContentPane().add(semesterLabel);
		
		JRadioButton fallRadioButton = new JRadioButton("Fall");
		fallRadioButton.setBounds(40, 245, 70, 23);
		frame.getContentPane().add(fallRadioButton);
		
		JRadioButton springRadioButton = new JRadioButton("Spring");
		springRadioButton.setBounds(105, 245, 80, 23);
		frame.getContentPane().add(springRadioButton);
		
		JRadioButton summerRadioButton = new JRadioButton("Summer");
		summerRadioButton.setBounds(180, 245, 90, 23);
		frame.getContentPane().add(summerRadioButton);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(fallRadioButton);
		bg.add(springRadioButton);
		bg.add(summerRadioButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				new CourseCollectionPage();
			}
		});
		cancelButton.setBounds(30, 280, 120, 30);
		frame.getContentPane().add(cancelButton);
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = nameTextField.getText();
				String code = codeTextField.getText();
				String year = yearTextField.getText();
				String semester = "";
				if(fallRadioButton.isSelected()) {
					semester = fallRadioButton.getText();
				}
				if(springRadioButton.isSelected()) {
					semester = springRadioButton.getText();
				}
				if(summerRadioButton.isSelected()) {
					semester = summerRadioButton.getText();
				}
				Object[] courseRow = {name, code, year, semester, "Active", "View", "Edit", "Delete"};
				DefaultTableModel tableModel = (DefaultTableModel) CourseCollectionPage.courseTable.getModel();
				if(name.equals("") || code.equals("") || year.equals("") || semester.equals("")) {
					frame.dispose();
					String str = "You input course data is missing some values.";
					new HandleCourseInputErrorPopup(str);
				}else if(!name.equals("") && !code.equals("") && !year.equals("") && !semester.equals("")){
					char[] numOrNot = year.toCharArray();
					boolean flag = true;
					for (int i = 0; i < numOrNot.length; i++) {
						if(numOrNot[i] >= '0' && numOrNot[i] <= '9') {
							continue;
						}else {
							flag = false;
							break;
						}
					}
					
					//check for repeat courses
					boolean flag2 = true;
					if(flag) {
						ArrayList<Course> courseList = DatabaseAPI.getCourseList();
						Course checkCourse = new Course("", code, Integer.parseInt(year), Semester.valueOf(semester));
						for (int i = 0; i < courseList.size(); i++) {
							if(checkCourse.equals(courseList.get(i))) {
								flag2 = false;
								break;
							}
						}
					}
					
					if(flag && flag2 == false) {
						frame.dispose();
						String str = "You already have this course.";
						new HandleCourseInputErrorPopup(str);
					}else if(flag && flag2) {
						Course newCourse = new Course(name, code, Integer.parseInt(year), Semester.valueOf(semester));
						DatabaseAPI.saveCourse(newCourse);
						tableModel.addRow(courseRow);
						frame.dispose();
						new CourseCollectionPage();
					}else {
						frame.dispose();
						String str = "You input year is not a number value!";
						new HandleCourseInputErrorPopup(str);
					}
				}else {
					frame.dispose();
					String str = "You input contains error and needs to be fixed.";
					new HandleCourseInputErrorPopup(str);
				}
				
				
			}
		});
		saveButton.setBounds(150, 280, 120, 30);
		frame.getContentPane().add(saveButton);
		frame.setResizable(false);
	}
}
