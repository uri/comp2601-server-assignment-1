package edu.carleton.COMP2601;

import java.util.HashMap;

public interface Reactor {
	
	
	public void registerHandler(String s, EventHandler e);
	
	public void waitForEvents();
	
	public void handleEvents();
	
	
	
	
	public void stop();
	
	public HashMap<String, EventHandler> getHandlers();
}
