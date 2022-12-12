package cn.edu.fudan.se.multidependency.exception;

public class TestCaseIdErrorException extends Exception {

	private static final long serialVersionUID = 6633012278645401925L;
	
	public TestCaseIdErrorException() {
		super("wrong testcase id");
	}
	
	public TestCaseIdErrorException(Integer errorTestCaseId) {
		super("wrong testcase id: " + errorTestCaseId);
	}
}
