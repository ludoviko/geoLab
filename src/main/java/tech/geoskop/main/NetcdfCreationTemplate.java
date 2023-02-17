package tech.geoskop.main;

import java.util.List;

import ucar.ma2.DataType;

public class NetcdfCreationTemplate {

	private FilePathName filePathName;

	private List<Attribute> fileMetadata;

	private List<Variable> variables;

	public FilePathName getFilePathName() {
		return filePathName;
	}

	public void setFilePathName(FilePathName filePathName) {
		this.filePathName = filePathName;
	}

	public List<Attribute> getFileMetadata() {
		return fileMetadata;
	}

	public void setFileMetadata(List<Attribute> fileMetadata) {
		this.fileMetadata = fileMetadata;
	}

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}

	public NetcdfCreationTemplate() {
	}

	@Override
	public String toString() {
		return "NetcdfCreationTemplate [filePathName=" + filePathName + ", fileMetadata=" + fileMetadata
				+ ", variables=" + variables + "]";
	}
}

class FilePathName {
	private String dir;
	private String name;

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "FilePathName [dir=" + dir + ", name=" + name + "]";
	}

}

class Attribute {
	private String name;
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Attribute [name=" + name + ", value=" + value + "]";
	}

}

class Dim {
	private String name;
	private int size;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "Dimension [name=" + name + ", size=" + size + "]";
	}

}

class Variable {
	private String name;
	private DataType type;
	private List<Dim> dimensions;
	private List<Attribute> attributes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public List<Dim> getDimensions() {
		return dimensions;
	}

	public void setDimensions(List<Dim> dimensions) {
		this.dimensions = dimensions;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return "Variable [name=" + name + ", type=" + type + ", dimensions=" + dimensions + ", attributes=" + attributes
				+ "]";
	}

}