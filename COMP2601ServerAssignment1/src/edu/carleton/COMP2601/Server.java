package edu.carleton.COMP2601;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Server implements Runnable, Reactor {

	private boolean serverRunning;
	private HashMap<String, EventHandler> handlers;
	
	private JsonReader dis;
	private JsonWriter dos;
	
	private Gson gson;
	
	private Socket s;

	public static void main(String[] args) {
		Server ns = new Server();

		System.out.println("Launching Server.");
		ns.run();
	}

	public Server() {
		serverRunning = false;
		handlers = new HashMap<String, EventHandler>();
		gson = new GsonBuilder().create();

		// REQ_LOGIN
		registerHandler(Message.REQ_LOGIN, new EventHandler() {
			public void handleEvent(Event e) {
				Message reply = new Message(Message.REPLY_LOGIN);
				sendMessage(reply);
				
			}
		});

		// REQ_LIST_FILES
		registerHandler(Message.REQ_LIST_FILES, new EventHandler() {
			public void handleEvent(Event e) {
				// The user has requested the list of files...
				File fileDirectory = new File("files");
				
				String directories[] = fileDirectory.list();
				ArrayList<String> files = new ArrayList<String>();
				
				
				
				// Read the directories
				if (directories != null) {
					for (String s : fileDirectory.list()) {
						System.out.println("This is the name of the file: " + s);
						files.add(s);
					}
					
					// Send the message
					Message reply = new Message(Message.REPLY_LIST_FILES);
					reply.getBody().put(Message.KEY_FILE_LIST, files);
					
					// TODO body puts Message.Key_Image
					// Send the response
					sendMessage(reply);
					
				} else {
					System.out.println("Error file directory not found.");
				}
				
			}
		});

		// REQ_FILE
		registerHandler(Message.REQ_FILE, new EventHandler() {
			public void handleEvent(Event e) {
				// The user has requested the list of files...
				String fileName = (String)e.getMap().get(Message.KEY_FILE);
				String content = "";
				
				File file = new File("files" + File.separator + fileName);
				if (file.exists()) {
					try {
						System.out.println("Found the file:" + file.getName());
						FileReader reader = new FileReader(file);
						BufferedReader br = new BufferedReader(reader);
						
						String currentLine = null;
						currentLine = br.readLine();
						
						while (currentLine != null) {
							content += currentLine + "\n";
							currentLine = br.readLine();
						}
						
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				// We now have the content.
				System.out.println("This is the content of the file: "+ content);

				// Send the content back
				Message reply = new Message(Message.REPLY_FILE);
				reply.getBody().put(Message.KEY_CONTENT, content);

				sendMessage(reply);
			}
		});
	}

	/**
	 * Runnable Interface
	 */
	public void run() {
		ServerSocket listener;

		try {

			// Listen on a port
			listener = new ServerSocket(Common.PORT);
			// Connect on a socket
			
			System.out.println("Waiting for socket connection...");
			s = listener.accept();
			System.out.println("Found client.");

			// Set serverRunning to true if we have connected
			if (s.isConnected()) {
				serverRunning = true;
				
				System.out.println("Connection established.");
			} else {
				System.out.println("Not connected.");
			}

			while (serverRunning) {

				// Running Service...
				System.out.println("Runing Service...");
				handleEvents();
			}

			// Close the connection once we are done
			// TODO will this throw an error if we are not connected
			s.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * The Service to be performed while the server is mainting a connection
	 * 
	 * @param is
	 * @param os
	 * @throws HeadlessException
	 * @throws ClassNotFoundException
	 */
	public void handleEvents()
			throws HeadlessException {

		// Read
		Message m = readMessage();
		
		System.out.println("Found message::"+m);

		// Dispatch the method
		EventHandler h = handlers.get(m.getHeader().getType());

		// If we have an event handler for the message, call it
		if (h != null) {
			System.out.println("Event is not null.");
			h.handleEvent(new Event(m));
		}

	}
	
	
	public void sendMessages(ArrayList<Message> messageList) {
		
	}
	
	public void sendMessage(Message m) {
		
		
		
		try {
			OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
			dos = new JsonWriter(new BufferedWriter(os));
			
			dos.beginArray();
			gson.toJson(m, Message.class, dos);
			dos.endArray();
			
			// For the buffered reader - send what we have.
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readMessages() {
		
	}
	
	
	/** 
	 * Read a single message
	 */
	public Message readMessage() {
		Message received = null;
		
		try {
			InputStreamReader is = new InputStreamReader(s.getInputStream());
			dis = new JsonReader(new BufferedReader(is));
			
			dis.beginArray();
			received = gson.fromJson(dis, Message.class);
			dis.endArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return received;
	
	}

	/**
	 * Add a handler
	 */
	public void registerHandler(String s, EventHandler ev) {
		handlers.put(s, ev);
	}

	/**
	 * Stop the server
	 */
	public void stop() {
		serverRunning = false;
	}

	public HashMap<String, EventHandler> getHandlers() {
		return handlers;
	}

	@Override
	public void waitForEvents() {
		// TODO Auto-generated method stub

	}



}
