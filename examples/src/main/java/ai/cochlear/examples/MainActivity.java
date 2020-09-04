package ai.cochlear.examples;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String initialApiKey = "< Enter API Key >";
    private static final List<String> localFiles = new ArrayList<>();

    private Spinner filesToInference;
    private TextView centralText;
    private EditText apiKey;
    private AssetManager assetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        localFiles.add("carhorn.wav");
        localFiles.add("cough.wav");
        localFiles.add("dogbark.wav");
        localFiles.add("glassbreak.wav");
        localFiles.add("siren.wav");

        assetManager = getResources().getAssets();

        boolean allowed = Permission.verify(this);
        if(!allowed){
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        filesToInference = findViewById(R.id.files);
        centralText = findViewById(R.id.centralText);
        apiKey = findViewById(R.id.api_key);

        apiKey.setText(initialApiKey);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, MainActivity.localFiles);
        filesToInference.setAdapter(adapter);
    }

    public void onFileInference(View v) {
        String file = filesToInference.getSelectedItem().toString();
        String key = apiKey.getText().toString();
        centralText.setText("file " + file + " is being inferenced");

        InferenceFile.Inference(key, file, centralText, assetManager);
    }

    public void onStreamInference(View v) {
        String key = apiKey.getText().toString();

        InferenceStream.Inference(key, centralText);
    }
}