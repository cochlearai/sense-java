package ai.cochlear.sense;

import org.json.JSONObject;

import ai.cochlear.sense.proto.SenseClient;

/**
 * Event Class is dependent analyzing Object during one second
 */
public class Event {
    /**
     * name of the detected event
     */
    public String tag;
    /**
     * probability for the event to happen. Its values is between 0 and 1
     */
    public double probability;
    /**
     * start timestamp of the detected event since the beginning of the inference
     */
    public double startTime;
    /**
     * end timestamp of the detected event since the beginning of the inference
     */
    public double endTime;

    public Event(SenseClient.Event raw) {
        this.tag = raw.getTag();
        this.probability = raw.getProbability();
        this.startTime = raw.getStartTime();
        this.endTime = raw.getEndTime();
    }

    @Override
    public String toString() {
        return String.format("(tag: %s, probability: %f, start_time: %.1f, end_time: %.1f)", this.tag, this.probability,this.startTime, this.endTime);
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        object.put("tag", this.tag);
        object.put("probability", this.probability);
        object.put("start_time",(double) this.startTime);
        object.put("end_time",(double) this.endTime);
        return object;
    }
}

