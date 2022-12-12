package cn.edu.fudan.se.multidependency.exception;

import lombok.Getter;

public class LanguageErrorException extends Exception {

	private static final long serialVersionUID = -4627420836983503710L;
	@Getter
	private String errorLanguage;

	public LanguageErrorException(String errorLanguage) {
		this.errorLanguage = errorLanguage;
	}
}
