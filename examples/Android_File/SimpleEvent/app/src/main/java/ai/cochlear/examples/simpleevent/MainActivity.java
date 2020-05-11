package ai.cochlear.examples.simpleevent;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.io.InputStream;

import ai.cochlear.sense.File;
import ai.cochlear.sense.Result;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private final String TAG = "SenseAndroidClient";
    private static final String apiKey = "< Enter API KEY >";


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
    }

    @Override
    protected void onStart() {
        super.onStart();
        test();

    }

    public void test() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        AssetManager am = getResources().getAssets();
        InputStream is =  null;
        try {
            is = am.open("temp.wav");
            System.out.println(is);
            File fileClient = new File(apiKey,is,"wav","sense.cochlear.ai");
            Result result = fileClient.inference();

            //System.out.println(result);
            //System.out.println(result.allEvents());
            //System.out.println(result.detectedEvents());
            System.out.println(result.detectedTags());
            System.out.println(result.service());
            System.out.println(result.detectedEventsTiming());

        } catch(Exception e) {

        }
    }
}