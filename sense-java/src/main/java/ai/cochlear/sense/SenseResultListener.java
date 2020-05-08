package ai.cochlear.sense;

public interface SenseResultListener {
    void onResult(String result);
    void onError(String error);
    void onComplete();
}