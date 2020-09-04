package ai.cochlear.examples;

import android.widget.TextView;

import java.util.List;

import ai.cochlear.sense.Event;
import ai.cochlear.sense.Result;
import ai.cochlear.sense.SenseResultListener;
import ai.cochlear.sense.Stream;

public class InferenceStream {
    public static int SECOND_TO_INFERENCE = 60;

    public static void Inference(String apiKey, final TextView centralText) {
        final SenseMediaRecorder recorder = new SenseMediaRecorder(SECOND_TO_INFERENCE);

        Thread recorderThread  = new Thread(recorder);
        recorderThread.start();

        class ScreenText {
            public void setText(final String message) {
                centralText.post(new Runnable() {
                    @Override
                    public void run() {
                        centralText.setText(message);
                    }
                });
            }
        }
        final ScreenText screen = new ScreenText();

        try {
            final Stream sense = new Stream
                    .Builder()
                    .withApiKey(apiKey)
                    .withStreamer(recorder)
                    .withSamplingRate(SenseMediaRecorder.SAMPLE_RATE)
                    .withDataType("float32")
                    .withSmartFiltering(true)
                    .build();

            Thread inferenceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    class Callbacks implements SenseResultListener {
                        @Override
                        public void onResult(Result result) {
                            System.out.println(result.toJson());
                            List<Event> events = result.allEvents();
                            screen.setText(events.get(events.size() - 1).tag);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            recorder.stop();
                            screen.setText(throwable.toString());
                        }

                        @Override
                        public void onComplete() {
                            recorder.stop();
                            screen.setText("Stream finished");
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
        } catch(Exception e) {
            centralText.setText(e.toString());
        }
    }

}
