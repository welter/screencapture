package com.welter.Tcptools;

import java.util.ArrayList;

/**
 * Created by welter on 17年10月31日.
 */

public class TCPFrameHeader {
    private long iLength=0;
    private boolean bIsPacket=false;
    private long iPacketSize=0;
    private int iPacketCount=0;
    public  static  final  String HeaderSignal="WTCP";
    public  int getHeaderSize() {
        if (!bIsPacket)
        return 12;
        else return 25;
    }
    public static void parseHeader(byte[] data,byte[] header){

    }
    public byte[] getHeaderData(){
        byte[] baTemp;
        ArrayList <Byte> baHeaderData=new ArrayList <Byte> ();
        baTemp=HeaderSignal.getBytes();
        for (int i=0;i<baTemp.length;++i)
        {
            baHeaderData.add(baTemp[i]);
        }
        baTemp=Utilities.long2Bytes(iLength);
        for (int i=0;i<baTemp.length;++i)
        {
            baHeaderData.add(baTemp[i]);
        }
        if (bIsPacket)
        {
            baHeaderData.add((byte) 1);
            baTemp=Utilities.int2Bytes(iPacketCount);
            for (int i=0;i<baTemp.length;++i)
            {
                baHeaderData.add(baTemp[i]);
            }
            baTemp=Utilities.long2Bytes(iPacketSize);
            for (int i=0;i<baTemp.length;++i)
            {
                baHeaderData.add(baTemp[i]);
            }
        }
        else
            baHeaderData.add((byte) 0);
        byte[] r=new byte[baHeaderData.size()];
        for (int i=0;i<baHeaderData.size();++i){
            r[i]=baHeaderData.get(i);
        };
        return r;
    }
    public void setPacketSize(long l){
        iPacketSize=l;
        if (bIsPacket | (iPacketSize>0))
        {
            iPacketCount= (int) (l / ( iPacketSize)+1);
        }
    }
    public void setLength(long  l){
        iLength=l;
        if (bIsPacket | (iPacketSize>0))
        {
            iPacketCount= (int) (l / ( iPacketSize)+1);
        }
    }
    public void setIsPacket(boolean b){
        bIsPacket=b;
    }
}
