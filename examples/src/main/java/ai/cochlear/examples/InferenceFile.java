package ai.cochlear.examples;

import android.content.res.AssetManager;
import android.widget.TextView;

import java.io.InputStream;

import ai.cochlear.sense.File;
import ai.cochlear.sense.Result;

public class InferenceFile {
    public static void Inference(String apiKey, String file, final TextView centralText, AssetManager assetManager) {
        String extension = file.substring(file.lastIndexOf(".")+1);

        try {
            InputStream is = assetManager.open(file);
            final File senseFile = new ai.cochlear.sense.File
                    .Builder()
                    .withApiKey(apiKey)
                    .withReader(is)
                    .withFormat(extension)
                    .withSmartFiltering(true)
                    .build();

            centralText.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Result result = senseFile.inference();

                        final StringBuilder tags = new StringBuilder();
                        for(String tag : result.detectedTags()) {
                            tags.append(tag + " ");
                        }

                        centralText.setText(tags.toString());
                    } catch (Exception e) {
                        centralText.setText(e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            centralText.setText(e.toString());
        }

    }
}
