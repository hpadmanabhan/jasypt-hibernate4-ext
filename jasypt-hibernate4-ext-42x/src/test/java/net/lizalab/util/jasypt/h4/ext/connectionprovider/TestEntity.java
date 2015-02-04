package net.lizalab.util.jasypt.h4.ext.connectionprovider;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="TEST_ENTITY")
@SuppressWarnings("serial")
public class TestEntity implements Serializable {

	@Id
	@Column(name="TEST_ID")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer testId;
	
	@Column(name="TEST_STRING", nullable=false)
	private String testString;

	public TestEntity() {}

	public TestEntity(String testString) {
		this.testString = testString;
	}

	public String getTestString() {
		return testString;
	}

	public void setTestString(String testString) {
		this.testString = testString;
	}

	public Integer getTestId() {
		return testId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testId == null) ? 0 : testId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TestEntity)) {
			return false;
		}
		TestEntity other = (TestEntity) obj;
		if (testId == null) {
			if (other.testId != null) {
				return false;
			}
		} else if (!testId.equals(other.testId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestEntity [testId=");
		builder.append(testId);
		builder.append(", testString=");
		builder.append(testString);
		builder.append("]");
		return builder.toString();
	}
	
	
}
