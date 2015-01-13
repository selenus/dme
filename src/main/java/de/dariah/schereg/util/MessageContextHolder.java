package de.dariah.schereg.util;

import java.util.ArrayList;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class MessageContextHolder {

	public static ArrayList<DisplayMessage> currentMessages() {
        return (ArrayList<DisplayMessage>)RequestContextHolder.currentRequestAttributes().getAttribute("messages", RequestAttributes.SCOPE_SESSION);
    }
	
	public static void addMessage(String code, String message) {
		addMessage(code, message, null);
	}
		
	public static void addMessage(String code, String message, String[] arguments) {
		ArrayList<DisplayMessage> messages = currentMessages();
		if (messages == null) {
			messages = new ArrayList<DisplayMessage>();
		}
				
		messages.add(new DisplayMessage(code, message, arguments));
				
		RequestContextHolder.currentRequestAttributes().setAttribute("messages", messages, RequestAttributes.SCOPE_SESSION);
	}
}
