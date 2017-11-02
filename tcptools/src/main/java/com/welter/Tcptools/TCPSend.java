package com.welter.Tcptools;

/**
 * Created by welter on 17年10月17日.
 */

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 发送
 */
public class TCPSend {

    /**
     * 发送数据的客户端Socket
     */
    private Socket socket;

    private OutputStream out = null;

    /**
     * @param ip   接收方的ip地址
     * @param port 接收方的端口号
     */
    public TCPSend(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = socket.getOutputStream();
            Log.i("---", "isBound" + socket.isBound() + " isConnected" + socket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     */
    public boolean sendMessage(String msg) {
        Log.i("---", "isBound" + socket.isBound() + " isConnected" + socket.isConnected());
        try {
            TCPFrameHeader tcpHeader= new TCPFrameHeader();
            long count = msg.length();
            byte[] totalCount = new byte[8]; //将总数转为byte字符
            //outputStream.write(totalCount);
            tcpHeader.setLength(count);
            tcpHeader.setIsPacket(false);
            out.write(tcpHeader.getHeaderData());
            int PacketSize = 10240;//这里指定每包为10Kbyte
            ByteArrayInputStream byteStream=new ByteArrayInputStream(msg.getBytes());
            int PacketCount = (int) (count / ((long) PacketSize));//总包数

            int LastDataPacket = (int) (count - ((long) (PacketSize * PacketCount)));//余字节数，也可能会是0
            byte[] buffer = new byte[PacketSize];//设定缓冲区
            for (int i = 0; i < PacketCount; i++) {

                byteStream.read(buffer,0, buffer.length);//将文件读到缓冲区

//                byte[] bufferLength = new byte[4];//需要将buffer的长度转换为byte字节
//                outputStream.write(bufferLength);//写入4个字节代表buffer的长度
                out.write(buffer);//将缓冲写入到流

            }
            if (LastDataPacket != 0) {
                buffer = new byte[LastDataPacket];//重新设定缓冲区大小
                byteStream.read(buffer,0, buffer.length);//将文件读到缓冲区
//                byte[] bufferLength = new byte[4];//需要将buffer的长度转换为byte字节
//                outputStream.write(bufferLength);//写入4个字节代表buffer的长度
                out.write(buffer);//将缓冲写入到流
            }
            out.flush();
            byteStream.close();
            //fileOutStream.close();
            //outputStream.close();
            return true;
        }
        catch ( IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMessage(byte[] msg){
        Log.i("---", "isBound" + socket.isBound() + " isConnected" + socket.isConnected());
        try {
            TCPFrameHeader tcpHeader= new TCPFrameHeader();
            long count = msg.length;
            byte[] totalCount = new byte[8]; //将总数转为byte字符
            //outputStream.write(totalCount);
            tcpHeader.setLength(count);
            tcpHeader.setIsPacket(false);
            out.write(tcpHeader.getHeaderData());
            int PacketSize = 10240;//这里指定每包为10Kbyte
            ByteArrayInputStream byteStream=new ByteArrayInputStream(msg);
            int PacketCount = (int) (count / ((long) PacketSize));//总包数

            int LastDataPacket = (int) (count - ((long) (PacketSize * PacketCount)));//余字节数，也可能会是0
            byte[] buffer = new byte[PacketSize];//设定缓冲区
            for (int i = 0; i < PacketCount; i++) {

                byteStream.read(buffer,0, buffer.length);//将文件读到缓冲区

//                byte[] bufferLength = new byte[4];//需要将buffer的长度转换为byte字节
//                outputStream.write(bufferLength);//写入4个字节代表buffer的长度
                out.write(buffer);//将缓冲写入到流

            }
            if (LastDataPacket != 0) {
                buffer = new byte[LastDataPacket];//重新设定缓冲区大小
                byteStream.read(buffer,0, buffer.length);//将文件读到缓冲区
//                byte[] bufferLength = new byte[4];//需要将buffer的长度转换为byte字节
//                outputStream.write(bufferLength);//写入4个字节代表buffer的长度
                out.write(buffer);//将缓冲写入到流
            }
            out.flush();
            byteStream.close();
            //fileOutStream.close();
            //outputStream.close();
            return true;
        }
        catch ( IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (out != null) {
            try {
                out.close();
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
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }

}