package com.kc.filesync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class Handshaking {
	
	private boolean isThreadRunning;
	
	public boolean isThreadRunning() {
		return isThreadRunning;
	}

	public void setThreadRunning(boolean isThreadRunning) {
		this.isThreadRunning = isThreadRunning;
	}

	public void send(){
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Socket socket = new Socket("localhost",6666);  
					DataOutputStream dout=new DataOutputStream(socket.getOutputStream());  
					dout.writeUTF("Hello Server");  
					dout.flush();  
					dout.close();  
					socket.close();  
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		thread.start();
	}
	
	public void receive(){
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ServerSocket servsock = new ServerSocket(6666);
					while(isThreadRunning){
						Socket s = servsock.accept(); 
						DataInputStream dis = new DataInputStream(s.getInputStream());  
						String  str = (String)dis.readUTF();
						JsonReader reader = Json.createReader(dis);
						System.out.println("message= " + str);  
						JsonObject jsonObject = reader.readObject();
						JsonObject ports = jsonObject.getJsonObject("ports");
						if (!ports.getString("ps").isEmpty() && !ports.getString("pl").isEmpty()){
							break;
						}
						servsock.close();  
					}
					servsock.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		thread.start();
	}

}
