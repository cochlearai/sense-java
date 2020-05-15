package ai.cochlear.examples.simpleevent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.util.List;

import ai.cochlear.sense.Event;
import ai.cochlear.sense.Result;
import ai.cochlear.sense.SenseResultListener;
import ai.cochlear.sense.Stream;

public class MainActivity extends AppCompatActivity {
    private static final String apiKey = " < Enter API Key >";
    private static final int SecondToInference = 30;
    private static final int SamplingRate = 22050;

    TextView soundEventView = null;
    TextView serviceEventView = null;

    private final static int UPDATE_MESSAGE_SOUND = 0x1000;
    private final static int UPDATE_MESSAGE_SERVICE = 0x2000;
    private final Handler textHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            final int what = msg.what;

            if (what == UPDATE_MESSAGE_SOUND) {
                soundEventView.setText((String) msg.obj);
            }
            else if (what == UPDATE_MESSAGE_SERVICE) {
                serviceEventView.setText((String) "Service: ");
                serviceEventView.append((String) msg.obj);
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
        serviceEventView = findViewById(R.id.serviceTextView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean allowed = Permission.verify(this);
        if(!allowed){
            return;
        }

        try {
            inference();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeSoundMessage(String text) {
        Message message = Message.obtain();
        message.obj = text;
        message.what = UPDATE_MESSAGE_SOUND;
        textHandler.sendMessage(message);
    }

    public void changeServiceMessage(String text) {
        Message message = Message.obtain();
        message.obj = text;
        message.what = UPDATE_MESSAGE_SERVICE;
        textHandler.sendMessage(message);
    }

    public void inference() throws Exception {
        SenseMediaRecorder recorder = new SenseMediaRecorder(SecondToInference);

        Thread recorderThread  = new Thread(recorder);
        recorderThread.start();

        final Stream sense = new Stream
                .Builder()
                .withApiKey(apiKey)
                .withStreamer(recorder)
                .withSamplingRate(SamplingRate)
                .withDataType("float32")
                .withMaxEventsHistorySize(0)
                .build();

        Thread inferenceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                class Callbacks implements SenseResultListener {
                    @Override
                    public void onResult(Result result) {
                        System.out.println(result.toJson());
                        List<Event> events = result.allEvents();
                        changeServiceMessage(result.service());
                        changeSoundMessage(events.get(events.size() - 1).tag);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        recorder.stop();
                        changeSoundMessage(throwable.toString());
                    }

                    @Override
                    public void onComplete() {
                        recorder.stop();
                        changeServiceMessage("Finished");
                        changeSoundMessage("Finished");
                    }
                }

                Callbacks callbacks = new Callbacks();

                try {
                    sense.inference(callbacks);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        inferenceThread.start();
    }
}