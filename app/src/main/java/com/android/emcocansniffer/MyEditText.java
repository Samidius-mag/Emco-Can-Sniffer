package com.android.emcocansniffer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;


public class MyEditText extends EditText {
    private CanBusData[] list;
    private int displayCount = 0;
    private int recvCount = 0;
    private boolean isEnable = false;

    private final Context mContext;
    public MyEditText(Context context) {
        super(context);
        mContext = context;
        list = new CanBusData[1000];
        for (int i=0; i<1000; i++){
            list[i] = new CanBusData();
            list[i].ID = -1;
        }

    }
    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        list = new CanBusData[1000];
        for (int i=0; i<1000; i++){
            list[i] = new CanBusData();
            list[i].ID = -1;
        }
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        list = new CanBusData[1000];
        for (int i=0; i<1000; i++){
            list[i] = new CanBusData();
            list[i].ID = -1;
        }
    }

    private static StringBuffer buf=new StringBuffer("");
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (mContext) {
            if (isEnable) {
                if (recvCount >= displayCount) {
                    displayCount = recvCount;
                    for (int i = 0; i < recvCount; i++) {
                        if (list[i].ID >= 0) {
 //                           buf.append(list[i].FF==0?"      Standard  ":"      Extend      ");
 //                           buf.append(list[i].RTR==0?"          Data            ":"          Remote       ");
                            buf.append("            ").append(String.format("%08x", list[i].ID)).append("            ");
 //                           buf.append("            ").append(list[i].DLC).append("                             ");
                            buf.append(String.format("%02x", list[i].data0)).append(" ");
                            buf.append(String.format("%02x", list[i].data1)).append(" ");
                            buf.append(String.format("%02x", list[i].data2)).append(" ");
                            buf.append(String.format("%02x", list[i].data3)).append(" ");
                            buf.append(String.format("%02x", list[i].data4)).append(" ");
                            buf.append(String.format("%02x", list[i].data5)).append(" ");
                            buf.append(String.format("%02x", list[i].data6)).append(" ");
                            buf.append(String.format("%02x", list[i].data7)).append("\r\n");
                            list[i].ID = -1;
                        }
                    }
                }else {
                    for (int i=displayCount; i<1000; i++){
                        if (list[i].ID >= 0) {
  //                          buf.append(list[i].FF==0?"      Standard  ":"      Extend      ");
  //                          buf.append(list[i].RTR==0?"          Data            ":"          Remote       ");
                            buf.append("            ").append(String.format("%08x", list[i].ID)).append("            ");
  //                          buf.append("            ").append(list[i].DLC).append("                             ");
                            buf.append(String.format("%02x", list[i].data0)).append(" ");
                            buf.append(String.format("%02x", list[i].data1)).append(" ");
                            buf.append(String.format("%02x", list[i].data2)).append(" ");
                            buf.append(String.format("%02x", list[i].data3)).append(" ");
                            buf.append(String.format("%02x", list[i].data4)).append(" ");
                            buf.append(String.format("%02x", list[i].data5)).append(" ");
                            buf.append(String.format("%02x", list[i].data6)).append(" ");
                            buf.append(String.format("%02x", list[i].data7)).append("\r\n");
                            list[i].ID = -1;
                        }
                    }
                    displayCount = 0;
                    setText("");
                }
                append(buf.toString());
                buf.setLength(0);
                isEnable = false;
            }
        }
        postInvalidate();
    }

    public void setCanBusData(int FF, int RTR, int DLC, int ID, int[] data){
        synchronized (mContext) {
            isEnable = true;
  //          list[recvCount].FF = FF;
  //          list[recvCount].RTR = RTR;
  //          list[recvCount].DLC = DLC;
            list[recvCount].ID = ID;
            list[recvCount].data0 = data[0];
            list[recvCount].data1 = data[1];
            list[recvCount].data2 = data[2];
            list[recvCount].data3 = data[3];
            list[recvCount].data4 = data[4];
            list[recvCount].data5 = data[5];
            list[recvCount].data6 = data[6];
            list[recvCount].data7 = data[7];
            recvCount++;
            if (recvCount>=1000){
                recvCount = 0;
            }
        }

    }

    class CanBusData{
  //      int FF;
  //      int RTR;
  //      int DLC;
        int ID;
//        int[] data;
        int data0;
        int data1;
        int data2;
        int data3;
        int data4;
        int data5;
        int data6;
        int data7;
    }
}
