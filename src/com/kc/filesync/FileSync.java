package com.kc.filesync;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class FileSync {
	
	public static final int PORT = 12345, PORTTEMP = 12345;
	
	public static final int READ_METADATA = 0;
	public static final int WRITE_FILECONTENT = 1;
	public static final int COMMIT_FILE = 2;
	
	private Sender sender = new Sender();
	private Receiver receiver = new Receiver();
	
	public void doSync(){
		receiver.receive();
	    sender.setDestinationIPAddress("192.168.43.1");
//		sendAllFiles("192.168.0.104");
		while(true){
			System.out.println("Enter file path: ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				String filePath = br.readLine();
				if (filePath.isEmpty()){
					throw new Exception("File path is empty");
				}
				sender.reset();
				sender.sendAsFile(new File(filePath));
				sender.runDispatcher();
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	private void sendAllFiles(String ipaddress){
		File folder = new File("C:\\Users\\Kaustav\\Pictures\\Saved Pictures");
		File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	  sender.sendAsFile(listOfFiles[i]);
	        System.out.println("File " + listOfFiles[i].getName());
	      } else if (listOfFiles[i].isDirectory()) {
	        System.out.println("Directory " + listOfFiles[i].getName());
	      }
	    }
	    sender.setDestinationIPAddress(ipaddress);
	    sender.runDispatcher();
	}

}
