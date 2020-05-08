package ai.cochlear.sense;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Result {
    private JSONObject rawJson;
    private JSONObject result;
    private String service;
    private List<Event> events;

    public Result(String raw) {
        this.rawJson = new JSONObject(raw);
        this.result = rawJson.getJSONObject("result");
        this.service = result.getString("task");
        this.events = new ArrayList<Event>();
        JSONArray frames = result.getJSONArray("frames");
        for(int i = 0; i < frames.length(); i++) {
            events.add(new Event(frames.getJSONObject(i)));
        }
    }

    private boolean defaultEventFilter(Event event) {
        return true;
    }

    private boolean filter(Event event) {
        return defaultEventFilter(event);
    }

    public String service() {
        return this.service;
    }

    public List<Event> allEvents() {
        return events;
    }

    public List<Event> detectedEvents() {
        List<Event> tempList = new ArrayList<Event>();
        for (Event event : events) {
            if(filter(event)) {
                tempList.add(event);
            }
        };
        return tempList;
    }

    public List<String> detectedTags() {
        List<String> tags = new ArrayList<String>();
        detectedEvents().forEach(frame -> tags.add(frame.tag));
        return tags;
    }

//    public Map detectedEventsTiming() {
//
//    }
}

class Event {
    public String tag;
    public double probability;
    public float startTime;
    public float endTime;

    public Event(JSONObject json) {
        this.tag = json.getString("tag");
        this.probability = json.getDouble("probability");
        this.startTime = json.getFloat("start_time");
        this.endTime = json.getFloat("end_time");
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
