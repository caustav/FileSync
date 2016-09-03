package com.kc.filesync;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Sender {
	
	private ArrayList<Thread> threads;
	private String destIPAddress;

    private int fileSizeTemp = 0;

    public Sender(){
		threads = new ArrayList<>();
	}

	public void sendAsFile(final File f){

		Thread thread = new Thread(new Runnable() {

			private File file = f;

			@Override
			public void run() {
				try {
					manageFileMetadata(file);
					sendFileContent(file);
                    sendEOF();
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

	private boolean sendFileContent(File file){
		boolean bRet = false;
		fileSizeTemp = 0;
        int fileLength = (int) file.length();
		try{
            InputStream inputStream = new FileInputStream(file);
			if (null == inputStream){
				throw new FileNotFoundException("Selected file not found in the device.");
			}
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            
			int read;
			Socket sock;
			OutputStream outputStream;
			while(bis.available() > 0){
				byte buffer[] = new byte[1024*1024*10 + 1];
				buffer[0] = (byte)2;
				int bytesRead = 1;
				int byteOffset = 0;
			    while(bytesRead < buffer.length){
			    	byteOffset = bis.read(buffer, bytesRead, buffer.length - bytesRead);
			    	if (byteOffset == -1){
			    		break;	
			    	}else{
			    		bytesRead += byteOffset;
			    	}
			    }	
				sock = new Socket(destIPAddress, FileSync.PORTTEMP);
				outputStream = sock.getOutputStream();
				outputStream.write(buffer, 0, bytesRead);
				System.out.println(bytesRead);
				sock.close();
			}
			bRet = true;
			inputStream.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return bRet;
	}

	public void setDestinationIPAddress(String ipaddress) {
		this.destIPAddress = ipaddress;
	}

    private boolean sendEOF(){
        boolean bRet = false;
        try{
            Socket sock = new Socket(destIPAddress, FileSync.PORTTEMP);
            OutputStream os = sock.getOutputStream();
            byte[] buffer = new byte[1];
            buffer[0] = 3;
            os.write(buffer);
            sock.close();
            bRet = true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return bRet;
    }

    private boolean manageFileMetadata(File f){
        boolean bRet = false;
        try{
            Socket sock = new Socket(destIPAddress, FileSync.PORTTEMP);
			String fileName = f.getName();
			String fileSize = String.valueOf(f.length());
            OutputStream os = sock.getOutputStream();
            String str = fileName + "," + fileSize;
            byte[] buffer = new byte[str.getBytes().length + 1];
            buffer[0] = 1;
            byte [] b = str.getBytes();
            for (int i = 0; i < b.length; i ++){
                buffer[i+1] = b[i];
            }
            os.write(buffer);
            sock.close();
            bRet = true;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return bRet;
    }

	public void reset() {
		threads.clear();
	}
}
