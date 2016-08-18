package com.kc.filesync;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Sender {
	
	private ArrayList<Thread> threads;
	private String destIPAddress;
	
	public Sender(){
		threads = new ArrayList<Thread>();
	}
	
	public void send(String filePath){
		
		Thread thread = new Thread(new Runnable() {
			
			private String fPath = filePath;
			
			@Override
			public void run() {
				try {
					File file = new File(fPath);
					manageFileMetadata(file);
					sendFileContent(file);
					readEOF();
				    System.out.println("File sent as " + filePath);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		threads.add(thread);
	}
	
	public void runDispatcher(){
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					
					for (Thread th : threads){
						th.start();
						th.join();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		thread.start();
	}
	
	private boolean manageFileMetadata(File file){
		boolean bRet = false;
		try{
			Socket sock = new Socket(destIPAddress, FileSync.PORT);
			String fileName = file.getName();
			String fileSize = String.valueOf(file.length());
			DataOutputStream dout=new DataOutputStream(sock.getOutputStream());  
			String buffer = fileName + "," + fileSize;
			dout.writeUTF(buffer);  
			dout.flush();  
			dout.close();
			sock.close();
			bRet = true;
		}catch(Exception ex){
			ex.printStackTrace();
			
		}
		return bRet;
	}
	
	private boolean sendFileContent(File file){
		boolean bRet = false;
		try{
			Socket sock = new Socket(destIPAddress, FileSync.PORT);
		 	byte[] mybytearray = new byte[(int) file.length()];
		    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		    bis.read(mybytearray, 0, mybytearray.length);
		    OutputStream os = sock.getOutputStream();
		    os.write(mybytearray, 0, mybytearray.length);
		    bis.close();
		    os.flush();	
		    sock.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return bRet;
	}
	
	private boolean readEOF(){
		boolean bRet = false;
		DataInputStream dis;
		try {
			Socket sock = new Socket(destIPAddress, FileSync.PORT);
			dis = new DataInputStream(sock.getInputStream());
			String  eof = (String)dis.readUTF();
			System.out.println(eof);
			bRet = true;
			sock.close();
		} catch (IOException e) {
			bRet = false;
			e.printStackTrace();
		}
		return bRet;
	}

	public void setDestinationIPAddress(String ipaddress) {
		this.destIPAddress = ipaddress;
		
	}
}
