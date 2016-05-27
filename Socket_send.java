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
        	//����һ��Socket����ָ���������˵�IP��ַ�Ͷ˿ں�
    		Socket CarSocket;
    		try {
    			CarSocket = new Socket("192.168.1.1",2001);
    			//��Socket���еõ�OutputStream
    			OutputStream outputStream = CarSocket.getOutputStream();
    			
    			//�������������Ϣ  
                outputStream.write(txt1.getBytes());  
                outputStream.flush();  
                
    			//�ر������
    			outputStream.close();
    			//�ر�Socket
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
