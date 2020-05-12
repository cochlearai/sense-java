package ai.cochlear.examples.simpleevent;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.common.io.ByteStreams;

import java.io.InputStream;

import ai.cochlear.sense.Result;
import ai.cochlear.sense.SenseResultListener;
import ai.cochlear.sense.Stream;



public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private final String TAG = "SenseAndroidClient";
    private static final String apiKey = "<Enter API KEY>";

    //Sound event textView
    TextView soundEventView = null;

    //Handler for textView update

    private final static int UPDATE_MESSAGE = 0x1000;
    private final Handler textHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            final int what = msg.what;

            if (what == UPDATE_MESSAGE) {
                soundEventView.setText((String) msg.obj);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);

        soundEventView = findViewById(R.id.soundEventTextView);
        String serviceName = getString(R.string.service_name);
        TextView serviceView = findViewById(R.id.serviceTextView);
        serviceView.append(serviceName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        test();

    }

    public void test() {
        AssetManager am = getResources().getAssets();
        InputStream is = null;
        try {
            is = am.open("a.raw");
            System.out.println(is);
            byte[] bytes = ByteStreams.toByteArray(is);
            final Stream streamClient = new Stream(apiKey, bytes, 22050, "float32", "sense.cochlear.ai", 1024);

            streamClient.setListener(new SenseResultListener() {
                @Override
                public void onResult(Result result) {
                    System.out.println(result);
                }

                @Override
                public void onError(String error) {
                    Log.d("CochlearSenseResult", error);
                }

                @Override
                public void onComplete() {
                    try {
                        streamClient.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            streamClient.inference();
//            Message message = Message.obtain();
//            message.obj = tag;
//            message.what = UPDATE_MESSAGE;
//            textHandler.sendMessage(message);
        } catch (Exception e) {
        }
    }
}