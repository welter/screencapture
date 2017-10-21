package com.welter.Tcptools;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by welter on 17年10月21日.
 */

public class TCPSendfile {
    private Socket socket;
    private File fSend;
    private InputStream fileOutStream;
    private OutputStream outputStream;
    public TCPSendfile(String ip, int port){
        try {
            socket = new Socket(ip, port);
            outputStream = socket.getOutputStream();
            Log.i("---", "isBound" + socket.isBound() + " isConnected" + socket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean SendFile(String Filename)  {
        try {
            fSend = new File(Filename);
            fileOutStream = new FileInputStream(fSend);
            long count = fileOutStream.available();
            while (count == 0) {
                count = fileOutStream.available();
            }
            byte[] totalCount = new byte[8]; //将总数转为byte字符
            outputStream.write(totalCount);
            int PacketSize = 10240;//这里指定每包为10Kbyte

            int PacketCount = (int) (count / ((long) PacketSize));//总包数

            int LastDataPacket = (int) (count - ((long) (PacketSize * PacketCount)));//余字节数，也可能会是0
            byte[] buffer = new byte[PacketSize];//设定缓冲区
            for (int i = 0; i < PacketCount; i++) {

                fileOutStream.read(buffer, 0, buffer.length);//将文件读到缓冲区
                byte[] bufferLength = new byte[4];//需要将buffer的长度转换为byte字节
                outputStream.write(bufferLength);//写入4个字节代表buffer的长度
                outputStream.write(buffer);//将缓冲写入到流

            }
            if (LastDataPacket != 0) {
                buffer = new byte[LastDataPacket];//重新设定缓冲区大小
                fileOutStream.read(buffer, 0, buffer.length);//将文件读到缓冲区
                byte[] bufferLength = new byte[4];//需要将buffer的长度转换为byte字节
                outputStream.write(bufferLength);//写入4个字节代表buffer的长度
                outputStream.write(buffer);//将缓冲写入到流
            }
            outputStream.flush();

            fileOutStream.close();
            //outputStream.close();
            return true;
        }
        catch ( IOException e)
        {
            e.printStackTrace();
            return false;
        }
    };
    public void Close(){
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket.isInputShutdown()) { //判断输入流是否为打开状态
            try {
                socket.shutdownInput();  //关闭输入流
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket.isOutputShutdown()) {  //判断输出流是否为打开状态
            try {
                socket.shutdownOutput(); //关闭输出流（如果是在给对方发送数据，发送完毕之后需要关闭输出，否则对方的InputStream可能会一直在等待状态）
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket.isConnected()) {  //判断是否为连接状态
            try {
                socket.close();  //关闭socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
