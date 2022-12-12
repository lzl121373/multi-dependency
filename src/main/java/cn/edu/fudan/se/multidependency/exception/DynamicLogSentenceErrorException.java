package cn.edu.fudan.se.multidependency.exception;

import lombok.Getter;

public class DynamicLogSentenceErrorException extends Exception {

	private static final long serialVersionUID = 1332624603812414534L;
	
	@Getter
	private String sentence;

	public DynamicLogSentenceErrorException(String sentence) {
		this.sentence = sentence;
	}
}
