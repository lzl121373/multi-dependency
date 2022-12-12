package cn.edu.fudan.se.multidependency.exception;

public class ScenarioIdErrorException extends Exception {

	private static final long serialVersionUID = 6633012278645401925L;
	
	public ScenarioIdErrorException() {
		super("wrong scenario id");
	}
	
	public ScenarioIdErrorException(Integer errorScenarioId) {
		super("wrong scenario id: " + errorScenarioId);
	}
}
