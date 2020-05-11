package ai.cochlear.sense;

public interface SenseResultListener {
    void onResult(Result result);
    void onError(String error);
    void onComplete();
}