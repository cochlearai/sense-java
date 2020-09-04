package ai.cochlear.sense;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.cochlear.sense.proto.SenseClient;

/**
 * Result is a class that is returned by both file and stream when calling inference method.
 * Multiple results will be returned by a stream. For a file only one result will be returned.
 */
public class Result {
    private String service;
    private List<Event> events;

    public Result(SenseClient.CochlSense raw) {
        this.service = raw.getService();
        this.events = new ArrayList<>();

        for(SenseClient.Event ev : raw.getEventsList()) {
            this.events.add(new Event(ev));
        }
    }

    @Override
    public String toString() {
        return events.toString();
    }

    /**
     * returns a raw json object containing service name and an array of events
     * @return JSONObject
     */
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

    /**
     * use a filter function : that function takes an event as input and return a boolean.
     * An event will be "detected" if the filter function returns true for that event
     * the default filter is to consider all events as detected.
     * So by default, allEvents() and detectedEvents() will return the same result
     * where filter is a function that takes an event in input and returns a boolean
     * @param event
     */
    private boolean filter(Event event) {
        return defaultEventFilter(event);
    }

    /**
     * @return the service name : "human-interaction" or "emergency" for instance
     */
    public String service() {
        return this.service;
    }

    /**
     * returns all events
     * @return List of events
     */
    public List<Event> allEvents() {
        return events;
    }

    /**
     * returns all events that match the "filter function" defined below
     * @return List of detected events
     */
    public List<Event> detectedEvents() {
        List<Event> tempList = new ArrayList<>();
        for (Event event : events) {
            if(filter(event)) {
                tempList.add(event);
            }
        };
        return tempList;
    }

    /**
     * return only the "tag" of the event that match the "filter" function
     * @return List of detected tags
     */
    public List<String> detectedTags() {
        ArrayList<String> tags = new ArrayList<>();
        for( Event event: detectedEvents()) {
            tags.add(event.tag);
        }
        return tags;
    }

    /**
     * group events that match the "filter function" and shows segments of time of when events were detected
     * @return Map
     */
    public Map<String, ArrayList<ArrayList<Double>>> detectedEventsTiming() {
        Map<String, ArrayList<ArrayList<Double>>> summary = new HashMap<>();
        for (Event event : detectedEvents()) {
            ArrayList<ArrayList<Double>> timing = summary.get(event.tag);
            if(timing == null) {
                timing = new ArrayList<>();
            }
            ArrayList<Double> temp = new ArrayList<>();
            temp.add(event.startTime);
            temp.add(event.endTime);
            timing.add(temp);
            summary.put(event.tag, timing);
        }

        for( Map.Entry<String, ArrayList<ArrayList<Double>>> entry: summary.entrySet()) {
            summary.put(entry.getKey(), mergeOverlappingEvents(entry.getValue()));
        }

        return summary;
    }

    public List<Event> appendNewResult(SenseClient.CochlSense raw, int maxStoredEvents) {
        List<Event> newEvent = new ArrayList<>();
        for (SenseClient.Event ev : raw.getEventsList()) {
            newEvent.add(new Event(ev));
        }

        if(maxStoredEvents < events.size()) {
            this.events = this.events.subList(events.size() - maxStoredEvents,events.size());
        }

        this.events.addAll(newEvent);

        return this.events;
    }

    private ArrayList<ArrayList<Double>> mergeOverlappingEvents(ArrayList<ArrayList<Double>> times) {
        if (times.isEmpty()) {
            return new ArrayList<>();
        }
        Collections.sort(times, new Ascending());
        ArrayList<ArrayList<Double>> merged = new ArrayList<>();
        merged.add(times.get(0));
        for (ArrayList<Double> time : times.subList(1,times.size())) {
            ArrayList<Double> last = (ArrayList<Double>) merged.get(merged.size()-1);
            if (time.get(0) > last.get(1)) {
                merged.add(time);
                continue;
            }
            if(time.get(1) > last.get(1)) {
                ArrayList<Double> temp = new ArrayList<>();
                temp.add(last.get(0));
                temp.add(time.get(1));
                merged.set(merged.size()-1, temp);
            }
        }
        return merged;
    }
}

class Ascending implements Comparator<ArrayList<Double>> {
    @Override
    public int compare(ArrayList<Double> a, ArrayList<Double> b) {
        return a.get(0).compareTo(b.get(0));
    }
}
