package com.kc.filesync;

import java.io.File;

public class FileSync {
	
	public static final int PORT = 12345;
	
	public static final int READ_METADATA = 0;
	public static final int WRITE_FILECONTENT = 1;
	public static final int COMMIT_FILE = 2;
	
	private Sender sender = new Sender();
	private Receiver receiver = new Receiver();
	
	public void doSync(){
		receiver.receive();
		sendAllFiles();
//		while(true){
//			System.out.println("Enter file path: ");
//			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//			try {
//				String filePath = br.readLine();
//				if (filePath.isEmpty()){
//					throw new Exception("File path is empty");
//				}
//				sender.send(filePath);
//			} catch (Exception e) {
//				e.printStackTrace();
//				break;
//			}
//		}
	}
	
	private void sendAllFiles(){
		File folder = new File("C:\\Users\\Kaustav\\Pictures\\Saved Pictures");
		File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	  sender.send(listOfFiles[i].getPath());
	        System.out.println("File " + listOfFiles[i].getName());
	      } else if (listOfFiles[i].isDirectory()) {
	        System.out.println("Directory " + listOfFiles[i].getName());
	      }
	    }
	    String ipaddress = "10.10.10.10";
	    sender.setDestinationIPAddress(ipaddress);
	    sender.runDispatcher();
	}

}
