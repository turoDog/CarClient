package turo.carclient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Socket_send {
	
	public void send (String str){
		new MyThread(str).start();  
	}
	
	class MyThread extends Thread{
		public String txt1;  
		  
        public MyThread(String str) {  
            txt1 = str;  
        }  
        @Override  
        public void run() {  
        	//创建一个Socket对象，指定服务器端的IP地址和端口号
    		Socket CarSocket;
    		try {
    			CarSocket = new Socket("192.168.1.1",2001);
    			//从Socket当中得到OutputStream
    			OutputStream outputStream = CarSocket.getOutputStream();
    			
    			//向服务器发送信息  
                outputStream.write(txt1.getBytes());  
                outputStream.flush();  
                
    			//关闭输出流
    			outputStream.close();
    			//关闭Socket
    			CarSocket.close();
    		} catch (UnknownHostException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }  
    }  
}
