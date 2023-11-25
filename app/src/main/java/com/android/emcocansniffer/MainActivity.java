package com.android.emcocansniffer;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.android.canbus.CanBusHelper.CanBusCallback;
import com.android.canbus.CanBusHelper;


public class MainActivity extends Activity {
    private TextView logTextView;

    private TextView mTextView;

    private DataSearchHelper mDataSearchHelper;

    private MyEditText mDisplay = null;
    private CanBusHelper mCanBusHelper = new CanBusHelper();
    private int mComBoudrate = 19200;
    private int mCanBoudrate = 250000;
    private TextView canDataTextView;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.TextView);
        logTextView = findViewById(R.id.logTextView);
        canDataTextView = findViewById(R.id.canDataTextView);
        mDisplay = (MyEditText) findViewById(R.id.display);
        String canData = "Данные с кан"; // Пример данных, полученных с CAN-шины
        canDataTextView.setText(canData);
        int ret = mCanBusHelper.setSerialBaudrate(0, mComBoudrate, 8, 0, 1);
        if (ret < 0) {
            logMessage("Подключите адаптер!");
            return;
        }

        if (mCanBusHelper.initialize(0, mComBoudrate, mCanBoudrate) == 0) {
            logMessage("Адаптер инициализирован!");
            Thread canThread = new Thread(mCanRunnable);
            canThread.start();
        }
    }

    private void logMessage(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logTextView.append(message + "\n");
            }
        });
    }

    private final Runnable mCanRunnable = new Runnable() {
        public void run() {
            mCanBusHelper.readCan(new CanBusCallback() {
                @Override
                public void onSetError() {
                }

                @Override
                public void onSendError() {

                }

                @Override
                public void onReceiveCanbusData(int FF, int RTR, int DLC, int ID, int[] DATA) {
                    mDisplay.setCanBusData(FF, RTR, DLC, ID, DATA);
                    DataSearchHelper.searchDataById(ID, DATA, mTextView);


                }
               /* public void onReceiveCanbusData2(int FF, int RTR, int DLC, int ID, int[] DATA) {
                    mDisplay.setCanBusData(FF, RTR, DLC, ID, DATA);
                    DataSearchHelper2.searchDataById2(ID, DATA, mTextViewTwo);
                }*/
            });
        }

    };
    }





