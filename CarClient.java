package turo.carclient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class CarClient extends Activity implements OnTouchListener{

	private Button ForWard = null;
	private Button BackWard = null;
	private Button TurnRight = null;
	private Button TurnLeft = null;
	private Button Stop = null;
	private Button mBtnSave = null;
	private WebView carView = null;
    private Bitmap mBitmap;
    private int btn_count = 0;
    private String pwmFileName;
	private String mFileName;
	private int file_count = 0;
    private boolean pwmW = false;
    private boolean pwmA = false;
    private boolean pwmS = false;
    private boolean pwmD = false;
    private final static String ALBUM_PATH
    = Environment.getExternalStorageDirectory() + "/CarImage/";
    private final static String PWM_PATH
    = Environment.getExternalStorageDirectory() + "/CarPWM/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_car_client);
		
		
		/*����ǿ�����̷߳������磺�����������ص�ͼƬ��С������ǿ�Ʒ��ʣ�
		 * ��Ҫ���ص����ݽϴ�ʱ�����½�һ�����߳����ڷ�������*/
		if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);}
		
		ForWard = (Button)findViewById(R.id.forward);
		BackWard = (Button)findViewById(R.id.back);
		TurnRight = (Button)findViewById(R.id.right);
		TurnLeft = (Button)findViewById(R.id.left);
		Stop = (Button)findViewById(R.id.stop);
		mBtnSave = (Button)findViewById(R.id.btnSave);
		ForWard.setOnTouchListener(this);
		BackWard.setOnTouchListener(this);
		TurnRight.setOnTouchListener(this);
		TurnLeft.setOnTouchListener(this);
		Stop.setOnTouchListener(this);
		
		/*WebView�ؼ������ڷ��ʷ������˿ڻ�ȡ���ӵ�����ͷ����*/
		carView = (WebView)findViewById(R.id.carView);
		carView.loadUrl("http://192.168.1.1:8080/?action=stream");
		
        /*�����ļ���椼����¼�������ʱ����������ͼƬ���̲߳�����
         * �ɿ�ʱ�������̣߳��������أ���������*/
        mBtnSave.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
           
                //����������־
                ++  btn_count;
                if (btn_count %2 == 1){
                	Toast.makeText(CarClient.this, "Start to save data...", Toast.LENGTH_SHORT).show();
                	//�����߳�
                	new Thread(connectNet).start();
                }else{
                	//�����߳�
                	connecthandler.removeCallbacks(connectNet);
                }
        }
        });
	}
    
	/*������ʺ��������ڴ�����������ͼƬ��������inputstream*/
    public InputStream getImageStream(String path) throws Exception{
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
            return conn.getInputStream();
        }
        return null;
    }  
    
    /*
     * ��������
     * ������4.0�в����������߳��з������磬������Ҫ�����߳��з���
     */
    private Handler connecthandler = new Handler();
    
    private Runnable connectNet = new Runnable(){
        @SuppressLint("ShowToast")
		@Override
        public void run() {

        		
            try {

                String filePath = "http://192.168.1.1:8080/?action=snapshot";
        
                //ȡ�õ���InputStream��ֱ�Ӵ�InputStream����bitmap
                mBitmap = BitmapFactory.decodeStream(getImageStream(filePath));
                
                //���ñ����ļ�����
                Save save = new Save();
            	save.saveImage(mBitmap);
            	save.savePWM();
            	
            } catch (Exception e) {
                Toast.makeText(CarClient.this,"�޷��������磡", 1).show();
                e.printStackTrace();
                System.out.println(e);
            }
            
            //�߳�ÿ��2������һ��
            connecthandler.postDelayed(connectNet, 500);
        }
    };
    
    /*�ڲ��࣬���ڱ����ļ���ͼƬ��PWM��*/
    public class Save {

    	public void saveImage(Bitmap bm){
        	try {
        		if (pwmA||pwmW||pwmS||pwmD == true){
        		//�ļ�������
                String s =null;
            	++file_count;
            	s = Integer.toString(file_count);
            	mFileName = s + ".jpg";
            	
    			saveFile(bm, mFileName);
    			System.out.println(bm);
    			
    			}
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }

        /**
         * �����ļ�
         * @param bm
         * @param fileName
         * @throws IOException
         */
        public void saveFile(Bitmap bm, String fileName) throws IOException {
            File dirFile = new File(ALBUM_PATH);
            if(!dirFile.exists()){
                dirFile.mkdir();
            }
            File myCaptureFile = new File(ALBUM_PATH + fileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        }
        
        public void savePWM(){
        	try {
        		
        		//�ļ�������
            	pwmFileName = "PWM.txt";
            	
            	if(pwmW == true){
            		savePWM("80","80",pwmFileName);
            	}else if(pwmA == true){
            		savePWM("5","100",pwmFileName);
            	}else if (pwmS == true){
            		savePWM("-80","-80",pwmFileName);
            	}else if (pwmD == true){
            		savePWM("100","5",pwmFileName);
            	}
            	
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
        
      //����PWM����
        public void savePWM(String pwm1,String pwm2, String fileName) throws IOException {
            File dirFile = new File(PWM_PATH);
            if(!dirFile.exists()){
                dirFile.mkdir();
            }
            FileOutputStream outStream = new FileOutputStream(PWM_PATH + fileName,true);
            OutputStreamWriter writer = new OutputStreamWriter(outStream,"utf-8");
            writer.write("(" + pwm1 + "," + pwm2 + ")");
            writer.write("\r\n");
            writer.flush();
            writer.close();//�ǵùر�

            outStream.close();
        }
    	
    }

    /**
     * 5�����������¼���
     * ForWard��BackWard��TurnLeft��TurnRight��Stop
     * �ֱ��Ӧ�ڿ��Ƴ��ӵ�ǰ�������ˣ���ת����ת�Լ�ɲ��
     **/
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.forward:
			if (event.getAction()==MotionEvent.ACTION_DOWN){
				   pwmW = true;
					//����һ��Socket����ָ���������˵�IP��ַ�Ͷ˿ں�
				   Socket_send sck = new Socket_send();
				   sck.send("w");
				   
			   }
	            else if(event.getAction()==MotionEvent.ACTION_UP){
	            	pwmW = false;
	            	Socket_send sck = new Socket_send();
					sck.send("f");
					}
			   break;
			   
		case R.id.back:
			if (event.getAction()==MotionEvent.ACTION_DOWN){
				   pwmW = true;
					//����һ��Socket����ָ���������˵�IP��ַ�Ͷ˿ں�
				   Socket_send sck = new Socket_send();
				   sck.send("s");
				   
			   }
	            else if(event.getAction()==MotionEvent.ACTION_UP){
	            	pwmW = false;
	            	Socket_send sck = new Socket_send();
					sck.send("f");
					}
			   break;
			   
		case R.id.left:
			if (event.getAction()==MotionEvent.ACTION_DOWN){
				   pwmW = true;
					//����һ��Socket����ָ���������˵�IP��ַ�Ͷ˿ں�
				   Socket_send sck = new Socket_send();
				   sck.send("a");
				   
			   }
	            else if(event.getAction()==MotionEvent.ACTION_UP){
	            	pwmW = false;
	            	Socket_send sck = new Socket_send();
					sck.send("f");
					}
			   break;
			   
		case R.id.right:
			if (event.getAction()==MotionEvent.ACTION_DOWN){
				   pwmW = true;
					//����һ��Socket����ָ���������˵�IP��ַ�Ͷ˿ں�
				   Socket_send sck = new Socket_send();
				   sck.send("d");
				   
			   }
	            else if(event.getAction()==MotionEvent.ACTION_UP){
	            	pwmW = false;
	            	Socket_send sck = new Socket_send();
					sck.send("f");
					}
			   break;
			   
		case R.id.stop:
			if (event.getAction()==MotionEvent.ACTION_DOWN){
				   pwmW = true;
					//����һ��Socket����ָ���������˵�IP��ַ�Ͷ˿ں�
				   Socket_send sck = new Socket_send();
				   sck.send("f");
				   
			   }
	            else if(event.getAction()==MotionEvent.ACTION_UP){
	            	pwmW = false;
	            	Socket_send sck = new Socket_send();
					sck.send("f");
					}
			   break;
		default:
			break;
		}
		return true;
	}   
}
