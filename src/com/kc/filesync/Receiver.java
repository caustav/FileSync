package com.kc.filesync;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
	
	private boolean isThreadRunning;
	
	private long totalSizeFile = 0;

	public boolean isThreadRunning() {
		return isThreadRunning;
	}

	public void setThreadRunning(boolean isThreadRunning) {
		this.isThreadRunning = isThreadRunning;
	}

	public void receive(){
		
		isThreadRunning = true; 
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				receiveInternal();
//				try {
//					ServerSocket servsock = new ServerSocket(FileSync.PORT);
//					System.out.println("Server listening at " + String.valueOf(FileSync.PORT));
//					int status = FileSync.READ_METADATA;
//					FileMetadata fileMetadata = null;
//					while(isThreadRunning){
//						Socket sock = servsock.accept();
//						switch(status){
//							case FileSync.READ_METADATA:{
//								fileMetadata = new FileMetadata();
//								if (!manageFileMetadata(sock, fileMetadata)){
//									throw new Exception("Problem in processing metadata");
//								}
//								status = FileSync.WRITE_FILECONTENT;
//								break;
//							}
//							case FileSync.WRITE_FILECONTENT:{
//								InputStream is = sock.getInputStream();
//								if (is.available() > 0){
//                                    if (!manageFileContent(sock, fileMetadata)){
//                                        throw new Exception("Problem in processing content");
//                                    }
//								}else{
//    								if (!manageCommitFile(sock)){
//    									throw new Exception("Problem in commiting content");
//    								}
//    								status = FileSync.READ_METADATA;
//    								fileMetadata = null;
//                                }
//								break;
//							}
//						}
//						sock.close();
//					}
//					
//					servsock.close();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		});
		
		thread.start();
	}
	
	private void receiveInternal(){
		boolean isThreadRunning = true;
		FileMetadata fileMetadata = null;
		try{
			ServerSocket servsock = new ServerSocket(FileSync.PORT);
			System.out.println("Server listening at " + String.valueOf(FileSync.PORT));
			while(isThreadRunning){
				Socket sock = servsock.accept();
				InputStream is = sock.getInputStream();
				byte[] flag = new byte[1];
				is.read(flag, 0, flag.length);
				if (flag[0] == 1){
					totalSizeFile = 0;
					fileMetadata = new FileMetadata();
					readFileMetadata(is, fileMetadata);
//					System.out.println("End metadata");
				}else if (flag[0] == 2){
					readFileContent(is, fileMetadata);
//					System.out.println("End content");
				}
				sock.close();
			}
			servsock.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void readFileMetadata(InputStream is, FileMetadata fileMetadata){
		byte[] arrayMetadata = new byte[1024];
		try {
			is.read(arrayMetadata, 0, arrayMetadata.length);
			String str = new String(arrayMetadata, "UTF-8");
			String [] fileInfo = str.split(",");
			if (fileMetadata != null){
				String fName = fileInfo[0].replace("\"", "");
				String fSize = fileInfo[1].replace("\"", "").trim();
				fileMetadata.setFileName(fName);
				System.out.println(fSize.length());
				fileMetadata.setFileSize(Integer.parseInt(fSize));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readFileContent(InputStream is, FileMetadata fileMetadata){
		System.out.println("start readFileContent");
		byte[] fileContent = new byte[1024*1024*10];
		try{
			FileOutputStream fos = new FileOutputStream("E:\\Workspace\\Misc\\Temp\\Mars\\" + fileMetadata.getFileName(), true);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
//		    int bytesRead = is.read(fileContent, 0, fileContent.length);
//		    if (bytesRead == -1){
//		    	bytesRead = 0;
//		    }
		    int bytesRead = 0;
		    int byteOffset;
		    while(bytesRead < fileContent.length){
		    	byteOffset = is.read(fileContent, bytesRead, fileContent.length - bytesRead);
		    	if (byteOffset == -1){
		    		break;	
		    	}else{
		    		bytesRead += byteOffset;
		    	}
		    }
		    System.out.println(String.valueOf(bytesRead + 1));
		    bos.write(fileContent, 0, fileContent.length);
		    totalSizeFile += bytesRead;
		    bos.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("end readFileContent : " + String.valueOf(totalSizeFile));
	}
	
	private boolean manageFileMetadata(Socket s, FileMetadata fileMetadata){
		boolean bRet = false;
		DataInputStream dis;
		try {
			dis = new DataInputStream(s.getInputStream());
//			JsonReader reader = Json.createReader(dis);
//			JsonObject jsonObject = reader.readObject();
//			String fileName = jsonObject.getString("fileName");
//			String fileSize = jsonObject.getString("fileSize");
			String  buffer = (String)dis.readUTF();
			String [] fileInfo = buffer.split(",");
			if (fileMetadata != null){
				fileMetadata.setFileName(fileInfo[0]);
				fileMetadata.setFileSize(Integer.parseInt(fileInfo[1]));
			}
			bRet = true;
		} catch (IOException e) {
			bRet = false;
			e.printStackTrace();
		}
		return bRet;
	}
	
	private boolean manageFileContent(Socket sock, FileMetadata fileMetadata){
		boolean bRet = false;
		try{
		    byte[] mybytearray = new byte[1024*1024*10];
		    InputStream is = sock.getInputStream();
		    FileOutputStream fos = new FileOutputStream("E:\\Workspace\\Misc\\Temp\\Neon\\" + fileMetadata.getFileName(), true);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    int bytesRead = is.read(mybytearray, 0, mybytearray.length);
		    if (bytesRead == -1){
		    	bytesRead = 0;
		    }
		    int byteOffset = -1;
		    while(bytesRead < mybytearray.length){
		    	byteOffset = is.read(mybytearray, bytesRead, mybytearray.length - bytesRead);
		    	if (byteOffset == -1){
		    		break;	
		    	}else{
		    		bytesRead += byteOffset;
		    	}
		    }
		    System.out.println(String.valueOf(bytesRead));
		    bos.write(mybytearray, 0, bytesRead);
		    bos.close();
		    bRet = true;
		}catch(Exception ex){
			ex.printStackTrace();
			bRet = false;
		}
		return bRet;
	}
	
	private boolean manageCommitFile(Socket sock){
		boolean bRet = false;
		try{
			DataOutputStream dout=new DataOutputStream(sock.getOutputStream());  
			dout.writeUTF("FILE_COMMIT");  
			dout.flush();  
			dout.close();  
			bRet = true;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return bRet;
	}
}
