package ai.cochlear.sense;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Result() {
        this.rawJson = null;
        this.result = null;
        this.service = null;
        this.events = new ArrayList<Event>();
    }


    @Override
    public String toString() {
        return events.toString();
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        JSONArray eventsArray = new JSONArray();
        for (Event event: events) {
            JSONObject temp = event.toJson();
            eventsArray.put(temp);
        }
        object.put("events", eventsArray);
        object.put("service", this.service);
        return object;
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
        List<Event> tempList = new ArrayList<>();
        for (Event event : events) {
            if(filter(event)) {
                tempList.add(event);
            }
        };
        return tempList;
    }

    public List<String> detectedTags() {
        ArrayList<String> tags = new ArrayList<String>();
        detectedEvents().forEach(frame -> tags.add(frame.tag));
        return tags;
    }

    public Map detectedEventsTiming() {
        Map<String, ArrayList<ArrayList<Float>>> summary = new HashMap<String, ArrayList<ArrayList<Float>>>();
        for (Event event : detectedEvents()) {
            ArrayList timing = (ArrayList) summary.getOrDefault(event.tag, new ArrayList<ArrayList<Float>>());
            ArrayList temp = new ArrayList<Float>();
            temp.add(event.startTime);
            temp.add(event.endTime);
            timing.add(temp);
            summary.put(event.tag, timing);
        }
        summary.replaceAll((key, value) -> mergeOverlappingEvents(value));
        return summary;
    }

    public List<Event> appendNewResult(String raw, int maxStoredEvents) {
        JSONObject newRawJson = new JSONObject(raw);
        JSONObject newResult = newRawJson.getJSONObject("result");
        this.service = newResult.getString("task");
        List<Event> newEvent = new ArrayList<Event>();
        JSONArray frameArray = newResult.getJSONArray("frames");
        if (frameArray != null) {
            for (int i = 0; i < frameArray.length(); i++) {
                newEvent.add(new Event(frameArray.getJSONObject(i)));
            }
        }
        if(maxStoredEvents < events.size()) {
            this.events = this.events.subList(events.size() - maxStoredEvents,events.size());
        }
        this.events.addAll(newEvent);
        return this.events;
    }

    private ArrayList<ArrayList<Float>> mergeOverlappingEvents(ArrayList<ArrayList<Float>> times) {
        if (times.isEmpty()) {
            return new ArrayList<ArrayList<Float>>();
        }
        Collections.sort(times, new Ascending());
        ArrayList merged = new ArrayList<ArrayList<Float>>();
        merged.add(times.get(0));
        for (ArrayList<Float> time : times.subList(1,times.size())) {
            ArrayList<Float> last = (ArrayList<Float>) merged.get(merged.size()-1);
            if (time.get(0) > last.get(1)) {
                merged.add(time);
                continue;
            }
            if(time.get(1) > last.get(1)) {
                ArrayList temp = new ArrayList<Float>();
                temp.add(last.get(0));
                temp.add(time.get(1));
                merged.set(merged.size()-1, temp);
            }
        }
        return merged;
    }
}


class Event {
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
        object.put("start_time", this.startTime);
        object.put("end_time", this.endTime);
        return object;
    }
}

class Ascending implements Comparator<ArrayList<Float>> {
    @Override
    public int compare(ArrayList<Float> a, ArrayList<Float> b) {
        if(a.get(0)>b.get(0)) {
            return 1;
        } else if (a.get(0) < b.get(0)) {
            return -1;
        } else {
            return 0;
        }
    }
}
