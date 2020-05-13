package ai.cochlear.sense;

public interface SenseResultListener {
    void onResult(Result result);
    void onError(Throwable error);
    void onComplete();
}