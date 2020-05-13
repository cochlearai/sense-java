package ai.cochlear.sense;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<byte[]> streamer = new ArrayList<>();
        streamer.add(new byte[120]);
        streamer.add(new byte[120]);
        streamer.add(new byte[120]);
        streamer.add(new byte[120]);
        try {
            Stream stream = new Stream
                    .Builder()
                    .withApiKey("api")
                    .withStreamer(streamer)
                    .withDataType("int32")
                    .withSamplingRate(10)
                    .withHost("sense.cochlear.ai")
                    .withMaxEventsHistorySize(0)
                    .build();

            class Callbacks implements SenseResultListener {

                @Override
                public void onResult(Result result) {
                    System.out.println("new result");
                    System.out.println(result.allEvents());
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("error");
                    System.out.println(throwable);
                }

                @Override
                public void onComplete() {
                    System.out.println("finished");
                }
            }

            Callbacks callbacks = new Callbacks();

            stream.inference(callbacks);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
