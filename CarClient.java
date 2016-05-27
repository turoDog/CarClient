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
		
		
		/*用于强制主线程访问网络：这里由于下载的图片较小，所以强制访问；
		 * 若要下载的数据较大时，请新建一个子线程用于访问网络*/
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
		
		/*WebView控件：用于访问服务器端口获取车子的摄像头画面*/
		carView = (WebView)findViewById(R.id.carView);
		carView.loadUrl("http://192.168.1.1:8080/?action=stream");
		
        /*保存文件按妞监听事件：按下时，启动下载图片按线程并保存
         * 松开时，结束线程，不再下载，保存数据*/
        mBtnSave.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
           
                //按键计数标志
                ++  btn_count;
                if (btn_count %2 == 1){
                	Toast.makeText(CarClient.this, "Start to save data...", Toast.LENGTH_SHORT).show();
                	//启动线程
                	new Thread(connectNet).start();
                }else{
                	//撤销线程
                	connecthandler.removeCallbacks(connectNet);
                }
        }
        });
	}
    
	/*网络访问函数，用于从网络上下载图片，并返回inputstream*/
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
     * 连接网络
     * 由于在4.0中不允许在主线程中访问网络，所以需要在子线程中访问
     */
    private Handler connecthandler = new Handler();
    
    private Runnable connectNet = new Runnable(){
        @SuppressLint("ShowToast")
		@Override
        public void run() {

        		
            try {

                String filePath = "http://192.168.1.1:8080/?action=snapshot";
        
                //取得的是InputStream，直接从InputStream生成bitmap
                mBitmap = BitmapFactory.decodeStream(getImageStream(filePath));
                
                //调用保存文件方法
                Save save = new Save();
            	save.saveImage(mBitmap);
            	save.savePWM();
            	
            } catch (Exception e) {
                Toast.makeText(CarClient.this,"无法链接网络！", 1).show();
                e.printStackTrace();
                System.out.println(e);
            }
            
            //线程每隔2秒运行一次
            connecthandler.postDelayed(connectNet, 500);
        }
    };
    
    /*内部类，用于保存文件（图片和PWM）*/
    public class Save {

    	public void saveImage(Bitmap bm){
        	try {
        		if (pwmA||pwmW||pwmS||pwmD == true){
        		//文件名递增
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
         * 保存文件
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
        		
        		//文件名递增
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
        
      //保存PWM数据
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
            writer.close();//记得关闭

            outStream.close();
        }
    	
    }

    /**
     * 5个按键监听事件，
     * ForWard，BackWard，TurnLeft，TurnRight，Stop
     * 分别对应于控制车子的前进，后退，左转，右转以及刹车
     **/
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.forward:
			if (event.getAction()==MotionEvent.ACTION_DOWN){
				   pwmW = true;
					//创建一个Socket对象，指定服务器端的IP地址和端口号
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
					//创建一个Socket对象，指定服务器端的IP地址和端口号
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
					//创建一个Socket对象，指定服务器端的IP地址和端口号
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
					//创建一个Socket对象，指定服务器端的IP地址和端口号
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
					//创建一个Socket对象，指定服务器端的IP地址和端口号
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
