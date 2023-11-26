package com.android.emcocansniffer;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.android.canbus.CanBusHelper.CanBusCallback;
import com.android.canbus.CanBusHelper;


public class MainActivity extends Activity {
    private TextView logTextView;
    private TextView weightTextView;
    private TextView gpsTextView;
  //  private static final int[] SAMPLE_DATA = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
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
        weightTextView = findViewById(R.id.weightTextView);
        gpsTextView = findViewById(R.id.gpsTextView);
        logTextView = findViewById(R.id.logTextView);
        canDataTextView = findViewById(R.id.canDataTextView);
        mDisplay = (MyEditText) findViewById(R.id.display);
        String canData = "Данные с CAN шины"; // Данные полученные с CAN-шины
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
                    DataSearch18ff81cc.searchDataById(ID,DATA, weightTextView);
                    DataSearch18ff83dd.searchDataById(ID,DATA, gpsTextView);


                }
             
            });
        }

    };
    }





