package gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import data.DatabaseAPI;
import model.Category;
import model.CategoryComponent;
import model.Course;
import model.GradeableCategory;
import model.Statistics;
import model.Student;
import model.Student.StudentType;
import model.TextCategory;
import java.awt.Font;

public class CourseGradebookButtonPage {

	private JFrame frame;
	private GradeBookPanel gBookPanel;
	private Course course;
	

	/**
	 * Create the application.
	 */
	public CourseGradebookButtonPage() {
	}
	
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public CourseGradebookButtonPage(Course course) {
		this.course = course;
		
		initialize(course);
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(Course course) {
		//course = DatabaseAPI.loadCourse(inputCourse.getName(), inputCourse.getCode(), inputCourse.getYear());
		//this.course = course;
		//initialize GradebookPanel 
		//new GradeBookPanel(currentCourse);
		//JFrame alexFrame = new JFrame("Gradebook");
		gBookPanel = new GradeBookPanel(course);
		gBookPanel.setAllData(true);
		
		//frame.setSize(1000, 800);
		//frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//frame.setVisible(true); 
		
		
		frame = new JFrame();
		frame.setVisible(true);
		Toolkit kit = Toolkit.getDefaultToolkit(); 
		Dimension screenSize = kit.getScreenSize(); 
		int screenWidth = screenSize.width; 
		//System.out.println(screenWidth);
		int screenHeight = screenSize.height;
		//System.out.println(screenHeight);
		frame.setBounds(100, 100, 1200, 850);
		//frame.setLocationRelativeTo(null);
		// set the window in the middle of screen
		int windowWidth = frame.getWidth(); 
		int windowHeight = frame.getHeight();  
		frame.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);
				
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		
		JButton backButton = new JButton("Back");
		backButton.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				new CourseCollectionPage();
			}
		});
		backButton.setBounds(30, 20, 120, 40);
		frame.getContentPane().add(backButton);
		
		JLabel courseTitleLabel = new JLabel("");
		courseTitleLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 25));
		courseTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		courseTitleLabel.setBounds(200, 60, 800, 60);
		courseTitleLabel.setText(course.toString() + " Gradebook");
		frame.getContentPane().add(courseTitleLabel);
		
		JButton addStudentButton = new JButton("Add Student");
		addStudentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gBookPanel.setAllData(true);
				new CreateStudentPage(course);
				frame.dispose();
				gBookPanel.setAllData(false);
			}
		});
		addStudentButton.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		addStudentButton.setBounds(30, 140, 200, 40);
		frame.getContentPane().add(addStudentButton);
		
		JButton importStudentsButton = new JButton("Import Students");
		importStudentsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//wait for method in Course -> bulkLoadStudents
				// after that could use addStudent() method repeatedly
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
				//jfc.showDialog(new JLabel(), "Select");
				int result = jfc.showOpenDialog(null);
				if(result != JFileChooser.CANCEL_OPTION) {
				File file = jfc.getSelectedFile();
				if(file.isDirectory()){
					System.out.println("Selected file folder is: " + file.getAbsolutePath());
				}else if(file.isFile()){
					System.out.println("Selected file is: " + file.getAbsolutePath());
				}
				System.out.println(jfc.getSelectedFile().getName());
				
			
				gBookPanel.setAllData(true);
				ArrayList<Student> studentsArray = DatabaseAPI.importStudents(file.getAbsolutePath());
				for (int i = 0; i < studentsArray.size(); i++) {
					String sId = studentsArray.get(i).getSid();
					String fName = studentsArray.get(i).getFirstName();
					String mName = studentsArray.get(i).getMiddleName();
					String lName = studentsArray.get(i).getLastName();
					course.addStudent(sId, fName, mName, lName, true, StudentType.UNDERGRADUATE);
				}
				gBookPanel.setAllData(false);
			}
				
			}
		});
		importStudentsButton.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		importStudentsButton.setBounds(740, 140, 200, 40);
		frame.getContentPane().add(importStudentsButton);
		
		JButton gradingSchemeButton = new JButton("Import Grading Scheme");
		gradingSchemeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gBookPanel.setAllData(true);
				
				if(!course.getAllCategories().isEmpty()) {
					JOptionPane.showMessageDialog(null, 
			    			"Cannot import into a course that already has categories.");
			    	return;
				}
				
				ArrayList<Course> allCourses = DatabaseAPI.getCourseList();
				allCourses.remove(course);
				if(allCourses.isEmpty()) {
					JOptionPane.showMessageDialog(null, 
							"No other courses to clone.");
					return;
				}
				
				CourseSelectionPanel coursesPanel = new CourseSelectionPanel(allCourses);
				int result = JOptionPane.showConfirmDialog(frame, 
						coursesPanel, 
						"Import Grading Scheme",
						JOptionPane.OK_CANCEL_OPTION);
		        
		        if(result == JOptionPane.OK_OPTION) {
		        	Course courseToCopy = coursesPanel.getSelectedCourse();

		        	//Add the Categories
					for(Category newCategory : courseToCopy.getAllCategories()) {
						newCategory.clearStudentEntries();
						course.addCategory(newCategory);
						gBookPanel.addCategoryTab(newCategory);
					}

		        	//Add to Summary tab
					//First clear out anything we may have added to it automatically
					course.getSummary().getComponents().clear();
					for(CategoryComponent cc : courseToCopy.getSummary().getComponents()) {
						if(cc.isGradeable()) {
							continue;
						}
						
						course.getSummary().addComponent(cc);
					}
					
					gBookPanel.setAllData(true);
		        }	        
			}
		});
		gradingSchemeButton.setFont(new Font("Lucida Grande", Font.PLAIN, 17));
		gradingSchemeButton.setBounds(965, 140, 210, 40);
		frame.getContentPane().add(gradingSchemeButton);
		
		JButton addGradeCategoryButton = new JButton("Add Grade Category");
		addGradeCategoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAddNewCategory(true);
			}
		});
		addGradeCategoryButton.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		addGradeCategoryButton.setBounds(260, 140, 200, 40);
		frame.getContentPane().add(addGradeCategoryButton);
		
		JButton addTextCategoryButton = new JButton("Add Text Category");
		addTextCategoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAddNewCategory(false);
			}
		});
		addTextCategoryButton.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		addTextCategoryButton.setBounds(500, 140, 200, 40);
		frame.getContentPane().add(addTextCategoryButton);
		
		
		gBookPanel.setBounds(50, 200, 1100, 500);
		frame.getContentPane().add(gBookPanel);
		
		
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Course resetCourse = DatabaseAPI.loadCourse(course.getName(), course.getCode(), course.getYear(), course.getSemester());
				frame.dispose();
				new CourseGradebookButtonPage(resetCourse);
				//new CourseCollectionPage();
			}
		});
		resetButton.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		resetButton.setBounds(40, 720, 200, 40);
		frame.getContentPane().add(resetButton);
		
		JButton recalculateButton = new JButton("Recalculate");
		recalculateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String status = gBookPanel.updateOverallGrades();
    			
				if(status == null) {
					Statistics stats = course.courseStatistics();		    							
					
					StatisticsPanel statisticsPanel = new StatisticsPanel(stats.getMedian(), 
    						stats.getMean(), stats.getStandardDev());

					JOptionPane.showMessageDialog(frame, statisticsPanel, 
							"Statistics for Course: " + course.getName(), JOptionPane.PLAIN_MESSAGE);
				}
				else {
					JOptionPane.showMessageDialog(frame, status, 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		recalculateButton.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		recalculateButton.setBounds(700, 720, 200, 40);
		frame.getContentPane().add(recalculateButton);
		
		JButton saveChangesButton = new JButton("Save changes");
		saveChangesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				frame.dispose();
				gBookPanel.setAllData(true);
				new SaveCourseConfirmationPage(course);
				//DatabaseAPI.saveCourse(course);
				//Course updateCourse = DatabaseAPI.loadCourse(course.getName(), course.getCode(), course.getYear());
				//new CourseCollectionPage();
			}
		});
		saveChangesButton.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
		saveChangesButton.setBounds(950, 720, 200, 40);
		frame.getContentPane().add(saveChangesButton);
		frame.setTitle("Course Gradebook");
		frame.setResizable(false);
	}
	
	public void handleAddNewCategory(boolean isGradeable) {
		JFrame topFrame = (JFrame) SwingUtilities
				.getWindowAncestor(frame);
		
		NamePanel componentPanel;
		String type;
		if(isGradeable) {
			componentPanel = new GradeableCategoryPanel();
			type = "Gradeable Category";
		}
		else {
			componentPanel = new NamePanel();
			type = "Text Category";
		}
		
        int result = JOptionPane.showConfirmDialog(topFrame, 
        		componentPanel, 
				"Add New " + type,
				JOptionPane.OK_CANCEL_OPTION);
        
        if(result == JOptionPane.OK_OPTION) {
        	if(!componentPanel.hasProperData()) {
		    	JOptionPane.showMessageDialog(null, 
		    			"Invalid data entered.");
		    	return;
        	}
        	
        	String name = componentPanel.getName();
        	if(course.getCategory(name) != null) {
        		JOptionPane.showMessageDialog(null, 
		    			"Already have a " + type + " with the name " 
        				+ name + ".");
        		return;
        	}
        	        	
        	ArrayList<Student> students = course.getAllStudents();
        	Category newCategory;
        	if(isGradeable) {
        		GradeableCategoryPanel gradeablePanel = (GradeableCategoryPanel) componentPanel;
        		double percentWeight = gradeablePanel.getPercentWeight();
        		double weight = percentWeight / 100;
        		newCategory = new GradeableCategory(weight, name, students);
        	}
    		else {
    			newCategory = new TextCategory(name, students);
    		}
        	
			course.addCategory(newCategory);
			
			//Refresh Summary table GUI
			gBookPanel.setAllData(true);
			
			gBookPanel.addCategoryTab(newCategory);
        }
        
        gBookPanel.setAllData(false);
	}
}
