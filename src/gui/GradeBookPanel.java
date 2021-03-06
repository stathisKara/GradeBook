package gui;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TabbedPaneUI;

import model.Category;
import model.Course;
import model.Summary;
import model.Student;
import model.GradeableCategory;
import model.Statistics;

public class GradeBookPanel extends JPanel {
	private Course course;
	private JTabbedPane gradeBookTabs;
	
	public GradeBookPanel(Course course) {
		super(new GridLayout(1, 1));
		this.course = course;
		
		setUpGUI();
	}

	private void setUpGUI() {
		gradeBookTabs = new JTabbedPane();
		Font font = gradeBookTabs.getFont();
		Font newFont = new Font(font.getName(), font.getStyle(), 18);
		gradeBookTabs.setFont(newFont);
		
		//Add the Summary
		gradeBookTabs.addTab(Summary.SUMMARY, new GradeBookTablePanel(this, course.getSummary()));
		
		//Add the other Categories
		for(Category category : course.getAllCategories()) {
			addCategoryTab(category);
		}
		
		gradeBookTabs.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) 
		    {
		    	if(SwingUtilities.isRightMouseButton(e)){
		    		TabbedPaneUI tabUI = gradeBookTabs.getUI();
		    		int tabIndex = tabUI.tabForCoordinate(gradeBookTabs, e.getX(), e.getY());
		    		if(tabIndex >= 0) {
						String tabName = gradeBookTabs.getTitleAt(tabIndex);
						
						//Can't modify Summary tab
						if(!tabName.equals(Summary.SUMMARY)) {
							Category category = course.getCategory(tabName);
			    			JPopupMenu popupMenu = new JPopupMenu();
			    			
			    			//Add menu items
			    			if(category.isGradeable()) {
			    				JMenuItem statisticItem = new JMenuItem("Compute Statistics");
			    				statisticItem.addActionListener(new ActionListener() {
			    					@Override
			    					public void actionPerformed(ActionEvent e) {
			    						//Set the valid data in the model in the format it is in now
			    						GradeBookTablePanel gBookTablePanel = (GradeBookTablePanel) gradeBookTabs.getComponentAt(tabIndex);
			    						gBookTablePanel.syncModelAndGUI(true);
			    						boolean result = gBookTablePanel.updateOverallGrades(course);
			    		    			
			    						JFrame topFrame = (JFrame) SwingUtilities
		    									.getWindowAncestor(GradeBookPanel.this);
			    						if(result) {
			    							Statistics stats = course.categoryStatistics(category);		    							
			    							
			    							StatisticsPanel statisticsPanel = new StatisticsPanel(stats.getMedian(), 
			    		    						stats.getMean(), stats.getStandardDev());

			    							JOptionPane.showMessageDialog(topFrame, statisticsPanel, 
			    									"Statistics for " + category.getName(), JOptionPane.PLAIN_MESSAGE);
			    						}
			    						else {
			    							JOptionPane.showMessageDialog(topFrame, "Error when computing the statistics for " + category.getName(), 
			    									"Error", JOptionPane.ERROR_MESSAGE);
			    						}
			    					}
			    				});
			    				popupMenu.add(statisticItem);
			    			}
			    			
			    			JMenuItem editItem = new JMenuItem("Edit");
			    			editItem.addActionListener(new ActionListener() {
			    	            @Override
			    	            public void actionPerformed(ActionEvent e) {
			    	            	handleEditCategory(category);
			    	            	
			    	            	//Update the GUI
			    	            	gradeBookTabs.setTitleAt(tabIndex, category.getName());
			    	            	if(category.isGradeable()) {
			    	            		double weight = ((GradeableCategory) category).getWeight();
				    					String weightToolTip = getWeightToolTipString(weight);
				    	            	gradeBookTabs.setToolTipTextAt(tabIndex, weightToolTip);
			    	            	}
			    	            }
			    	        });
			    	        popupMenu.add(editItem);
			    	        
			    	        JMenuItem deleteItem = new JMenuItem("Delete");
			    	        deleteItem.addActionListener(new ActionListener() {
			    	            @Override
			    	            public void actionPerformed(ActionEvent e) {
			    	            	JFrame topFrame = (JFrame) SwingUtilities
			    	            			.getWindowAncestor(GradeBookPanel.this);
			    	            	int result = JOptionPane.showConfirmDialog(topFrame, 
			    	            			"Are you sure you want to delete category " + tabName + "?", 
			    	            			"Delete " + tabName,
			    	            			JOptionPane.OK_CANCEL_OPTION);
			    	            	
			    	            	if(result == JOptionPane.OK_OPTION) {
			    	            		course.deleteCategory(tabName);
			    	            		gradeBookTabs.remove(tabIndex);
			    	            	}
			    	            }
			    	        });
			    	        popupMenu.add(deleteItem);
			    			
			    	        popupMenu.show(gradeBookTabs, e.getX(), e.getY());
						}
		    		}
		        }
		    }
		});
		
		add(gradeBookTabs);
	}
	
	public void addCategoryTab(Category category) {
		if(category.isGradeable()) {
			double weight = ((GradeableCategory) category).getWeight();
			String weightToolTip = getWeightToolTipString(weight);
			gradeBookTabs.addTab(category.getName(), null, new GradeBookTablePanel(this, category), weightToolTip);
		}
		else {
			gradeBookTabs.addTab(category.getName(), new GradeBookTablePanel(this, category));
		}
	}
	
	public static String getWeightToolTipString(double weight) {
		return "Weight: " + weight*100 + "%";
	}
	
	private void handleEditCategory(Category category) {
    	JFrame topFrame = (JFrame) SwingUtilities
    			.getWindowAncestor(this);

    	NamePanel categoryPanel;
    	if(category.isGradeable()) {
    		GradeableCategory gradeableCategory = (GradeableCategory) category;
    		categoryPanel = new GradeableCategoryPanel(gradeableCategory.getName(), gradeableCategory.getWeight() * 100);
    	}
    	else {
    		categoryPanel = new NamePanel(category.getName());
    	}

    	int result = JOptionPane.showConfirmDialog(topFrame, 
    			categoryPanel, 
    			"Edit Category " + category.getName(),
    			JOptionPane.OK_CANCEL_OPTION);

    	if(result == JOptionPane.OK_OPTION) {
    		if(!categoryPanel.hasProperData()) {
    			JOptionPane.showMessageDialog(null, 
    					"Invalid data entered.");
    			return;
    		}

    		String newName = categoryPanel.getName();
    		if(!category.getName().equals(newName) && course.getCategory(newName) != null) {
    			JOptionPane.showMessageDialog(null, 
    					"Already have a category the name " + newName + ".");
    			return;
    		}

    		//Update to the new component data
    		category.setName(newName);
    		if(category.isGradeable()) {
    			GradeableCategory gradeableCategory = (GradeableCategory) category;
    			GradeableCategoryPanel gradeableCategoryPanel = (GradeableCategoryPanel) categoryPanel;
    			
    			double percentWeight = gradeableCategoryPanel.getPercentWeight();
    			gradeableCategory.setWeight(percentWeight / 100);
    		}
    	}
	}
	
	public void handleDeleteStudent(Student studentToDelete) {
		setAllData(true);
		course.deleteStudent(studentToDelete);
		setAllData(false);
	}
	
	public String updateOverallGrades() {
		setAllData(true);
		
		String errorString = null;
		//Update the Summary table last
		for(int tabIndex=gradeBookTabs.getTabCount()-1; tabIndex>=0; tabIndex--) {
			GradeBookTablePanel gBookTablePanel = (GradeBookTablePanel) gradeBookTabs.getComponentAt(tabIndex);
			boolean result = gBookTablePanel.updateOverallGrades(course);
			
			String categoryName = gradeBookTabs.getTitleAt(tabIndex);
			if(!result) {
				if(!categoryName.equals(Summary.SUMMARY)) {
					if(errorString == null) {
						errorString = "Assignment weights do not sum to 100% in the categories: " + categoryName;
					}
					else {
						errorString = errorString + ", " + categoryName;
					}
				}
				else {
					if(errorString == null) {
						errorString = "Categories for the course do not sum to 100%";
					}
				}
			}
		}
		
		return errorString;
	}
	
	public void setAllData(boolean doSetData) {
		for(int tabIndex=0; tabIndex<gradeBookTabs.getTabCount(); tabIndex++) {
			GradeBookTablePanel gBookTablePanel = (GradeBookTablePanel) gradeBookTabs.getComponentAt(tabIndex);
			gBookTablePanel.syncModelAndGUI(doSetData);
		}
	}
}
