package model;

public abstract class CategoryComponent {
	private String name;
	private boolean isEditable;
	
	public CategoryComponent(String n, boolean isEditable) {
		name = n;
		this.isEditable = isEditable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEditable() {
		return isEditable;
	}
	
	public abstract DataEntry<?> createEntry();
	
	public boolean isGradeable() {
		return this instanceof GradeableComponent;
	}
}