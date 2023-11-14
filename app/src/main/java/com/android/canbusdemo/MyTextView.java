package com.android.canbusdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class MyTextView extends TextView {
    private int num = 0;

    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        Paint paint = new Paint();
//        paint.setTextSize(30);
////        paint.setColor(Color.GRAY);
//        //绘制提示文字  运行时可看到在该控件的左侧有灰色的提示性文字，
//        canvas.drawText(text,8,getHeight(),paint);
//
        setText(String.valueOf(num));
    }


    public void setMyText(int _num){
        num = _num;
    }

}
