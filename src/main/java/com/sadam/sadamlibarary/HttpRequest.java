package com.sadam.sadamlibarary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

class Reader implements Runnable{
    /**
     * 连接超时时间
     */
    private final static int CONNECTTIMEOUT = 5000;
    /**
     * 读取超时时间
     */
    private final static int READTIMEOUT = 5000;

    private String url_web_address;
    private InputStreamHadler inputStreamHadler;



    public Reader(String url,InputStreamHadler inputStreamHadler) {
        this.inputStreamHadler = inputStreamHadler;
        this.url_web_address = url;
    }

    @Override
    public void run() {
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(url_web_address);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(CONNECTTIMEOUT);
            httpURLConnection.setReadTimeout(READTIMEOUT);
            httpURLConnection.setUseCaches(false);//不缓存
            InputStream inputStream = httpURLConnection.getInputStream();
            inputStreamHadler.handle(inputStream);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (httpURLConnection != null){
                httpURLConnection.disconnect();
            }
        }
    }




}

abstract class InputStreamHadler{
    /**
     * 是用来提交结果，收到的请求的response  传给 这个Handler处理 ，所以最终怎么处理要看这个handler的内置方法handleMessage
     *为什么必须用Handler ：因为  其他线程不能直接接触view  目前能间接接触的也只有这Handler 机制
     */
    private Handler handler;
    public InputStreamHadler(Handler handler) {
        this.handler = handler;
    }
    public abstract void handle(InputStream inputStream);
}




public class HttpRequest{

    public void send(final Handler handler, String url) {
        new Thread(new Reader(url, new InputStreamHadler(handler) {
            @Override
            public void handle(InputStream inputStream) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line + "\n");
                    }
                    Message message = new Message();
                    message.obj = new String(result);
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        })).start();
    }


    public static void getUrlImageBitmap(String url, final Handler handler){
        new Thread(new Reader(url, new InputStreamHadler(handler) {
            @Override
            public void handle(InputStream inputStream) {
                Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                Message message = new Message();
                message.obj = bmp;
                handler.sendMessage(message);
            }
        })).start();
    }
}

