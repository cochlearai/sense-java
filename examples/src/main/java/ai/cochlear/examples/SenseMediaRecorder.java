package ai.cochlear.examples;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class SenseMediaRecorder implements Runnable, Iterable<byte[]> {
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    public static final int SAMPLE_RATE = 22050;

    private final int HALF_SECONDS_SAMPLES = SAMPLE_RATE/2;
    private final int RECORD_BUF_SIZE = HALF_SECONDS_SAMPLES * 4;
    private int floatsLeftToRegister = 0;

    private boolean stoppedRecording = false;
    private LinkedBlockingQueue<byte[]> stream = new LinkedBlockingQueue();

    SenseMediaRecorder(int seconds) {
        floatsLeftToRegister = seconds * SAMPLE_RATE;
    }

    public void run() {
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                RECORD_BUF_SIZE * 10);
        recorder.startRecording();

        byte[] bytes = new byte[RECORD_BUF_SIZE];
        float[] floats = new float[HALF_SECONDS_SAMPLES];
        while (!stoppedRecording && floatsLeftToRegister > 0) {
            recorder.read(floats, 0, HALF_SECONDS_SAMPLES, AudioRecord.READ_BLOCKING);
            floatsLeftToRegister -= floats.length;

            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(floats);

            try {
                stream.put(bytes);
            } catch (InterruptedException e) {
                System.out.println(e);
                stop();
            }
        }

        stop();
    }

    public void stop() {
        stoppedRecording = true;
    }

    public Iterator<byte[]> iterator() {
        class CustomIterator implements Iterator<byte[]> {
            private boolean nullReturned = false;

            public boolean hasNext() {
                return !nullReturned;
            }

            public byte[] next() {
                if (stoppedRecording) {
                    nullReturned = true;
                    return null;
                }

                try {
                    return stream.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }

        };

        return new CustomIterator();
    }
}



