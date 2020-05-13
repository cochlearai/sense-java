package ai.cochlear.sense;

import org.json.JSONObject;

public class Event {
    public String tag;
    public double probability;
    public float startTime;
    public float endTime;

    public Event(JSONObject json) {
        this.tag = json.getString("tag");
        this.probability = json.getDouble("probability");
        this.startTime = (float) json.getDouble("start_time");
        this.endTime = (float) json.getDouble("end_time");
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

