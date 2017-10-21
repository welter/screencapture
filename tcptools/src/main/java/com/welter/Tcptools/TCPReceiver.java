package com.welter.Tcptools;

/**
 * Created by welter on 17年10月17日.
 */

import android.util.Log;

//import com.example.messagec.observer.ObserverHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 接收
 */
public class TCPReceiver {

    public static final String TAG = "TCPReceiver";

    /**
     * 接收数据的服务端Socket
     */
    private ServerSocket serverSocket;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * @param serverPort 服务器注册的端口号
     */
    public TCPReceiver(int serverPort) {
        initSocket(serverPort);
        initReceiverMessage();
    }

    private void initSocket(int serverPort) {
        try {
            // 创建一个ServerSocket对象，并设置监听端口
            serverSocket = new ServerSocket(serverPort);
            Log.i(TAG, "isBound=" + serverSocket.isBound() + "  isClosed=" + serverSocket.isClosed());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initReceiverMessage() {
        executorService.execute(runnableReceiverMsg);
    }

    private Runnable runnableReceiverMsg = new Runnable() {
        @Override
        public void run() {
            Socket socket = null;
            try {
                // 调用ServerSocket的accept()方法，接受客户端所发送的请求，
                socket = serverSocket.accept();
                // 从Socket当中得到InputStream对象
                InputStream inputStream = socket.getInputStream();
                byte buffer[] = new byte[10 * 1024];
                int temp = 0;
                // 从InputStream当中读取客户端所发送的数据
                while ((temp = inputStream.read(buffer)) != -1) {
                    Log.i(TAG, new String(buffer, 0, temp));  //打印接收到的信息

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

}