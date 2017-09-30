package app.locator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.ArrayList;

public class collect_data extends AppCompatActivity {
    private static int level = -85;
    private TextView print;
    private EditText ex;
    private EditText ey;
    private EditText ez;
    private int x;
    private int y;
    private int floor;
    private ScrollView scrollView;
    private WifiManager WifiM;
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 111;


    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        StrictMode.ThreadPolicy policy = new StrictMode
                .ThreadPolicy
                .Builder()
                .permitAll()
                .build();
        StrictMode.setThreadPolicy(policy);

        print = (TextView) this.findViewById(R.id.print);
        ex = (EditText) this.findViewById(R.id.ex);
        ey = (EditText) this.findViewById(R.id.ey);
        ez = (EditText) this.findViewById(R.id.ez);
        Button startbutton = (Button) this.findViewById(R.id.start);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        WifiM = (WifiManager) getSystemService(WIFI_SERVICE);

        checkPermission();

        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sx = String.valueOf(ex.getText());
                String sy = String.valueOf(ey.getText());
                String sf = String.valueOf(ez.getText());

                if (sx.equals("") || sy.equals("") || sf.equals("")) {
                    print.append(System.currentTimeMillis() + ": 请正确填写信息!\n");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    return;
                }

                x = Integer.parseInt(sx);
                y = Integer.parseInt(sy);
                floor = Integer.parseInt(sf);
                startwork();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("hello", "onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                if (permissions.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                        (permissions.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                                grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    WifiM.startScan();
                    Log.d("hello", "permission allow");
                    Toast.makeText(this, "permission allow", Toast.LENGTH_LONG).show();
                    //list is still empty
                } else {
                    // Permission Denied
                    Toast.makeText(this, "permission deny", Toast.LENGTH_LONG).show();
                    Log.d("hello", "permission deny");
                }
                break;
        }
    }

    @TargetApi(23)
    private boolean checkPermission() {
        Log.d("hello", "checkPermission");
        String[] permission = new String[2];
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permission[0] = Manifest.permission.ACCESS_FINE_LOCATION;
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permission[1] = Manifest.permission.ACCESS_COARSE_LOCATION;
        }
        if (permission[0] != null || permission[1] != null) {
            Log.d("hello", "regist Permission");
            requestPermissions(permission, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public void startwork() {
        WifiM.startScan();
        ArrayList<ScanResult> list = (ArrayList<ScanResult>) WifiM.getScanResults();
        int n = list.size();
        StringBuilder sb = new StringBuilder();
        sb.append(floor).append(",").append(x).append(",").append(y);
        for (int i = 0; i < list.size(); i++) {
            int level = list.get(i).level;
            if (level < collect_data.level) {
                n--;
                continue;
            }
            String bb = list.get(i).BSSID;
            String cc = bb.substring(0, 2) + bb.substring(3, 5) + bb.substring(6, 8);
            String dd = bb.substring(9, 11) + bb.substring(12, 14) + bb.substring(15);
            sb.append(";").append(cc).append(dd).append(",").append(level);
        }
        if (n == 0) {
            print.append(System.currentTimeMillis() + ": x:" + x + " y:" + y + "未采集到wifi信息\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            return;
        }
        sb.append(";").append(n);
        Sendmessage sendmessage = new Sendmessage(sb.toString());
        sendmessage.start();
        print.append(System.currentTimeMillis() + ": x:" + x + " y:" + y + "已上传\n");
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

}

class Sendmessage extends Thread {
    private static AmazonSQS sqs;
    private static String QueueUrl;
    private String message;

    static {
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIAPNCADUXD3HUR2QMQ",
                "vzDKSwqgx57UBUp+OIMwA1uG0sCkwRv/BX4iHHlM");
        AmazonSQSClient amazonSQSClient = new AmazonSQSClient(credentials);
        amazonSQSClient.setEndpoint("https://sqs.cn-north-1.amazonaws.com.cn");
        sqs = amazonSQSClient;
        QueueUrl = "https://sqs.cn-north-1.amazonaws.com.cn/444376591338/Wifi_Indoor_Positioning";
    }

    public Sendmessage(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        sqs.sendMessage(new SendMessageRequest(QueueUrl, message));
    }
}
