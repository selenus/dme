package de.dariah.schereg.util;

public class DisplayMessage {
	private String code = null;
	private String message = null;
	private String[] arguments = null;
	
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public String[] getArguments() { return arguments; }
	public void setArguments(String[] arguments) { this.arguments = arguments; }

	public DisplayMessage(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public DisplayMessage(String code, String message, String[] arguments) {
		this.code = code;
		this.message = message;
		this.arguments = arguments;
	}
	
	public String getJspTag(String springTagsPrefix) {
		if (code==null) {
			return message;
		}
		
		StringBuilder strBldr = new StringBuilder();

		
		// <s:message code="auth.message.loggedin" arguments="${user}" />
		
		return message;
	}
}
