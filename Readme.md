# Sense Java

API Library for Java and Android with audio analysis and recognition solutions from [Cochlear.ai](https://cochlear.ai)

## Overview

Sense API enables developers to extract various non-verbal information from an audio input with the power of audio processing and neural network techniques. It is robust against noises and different types of recording environments, so it can be used for analyzing input audio from various IoT devices such as smart speakers or IP cameras, not to mention its potential usage in searching through video clips.

To date, Sense API is the only publicly available API online for machine listening, a rapidly emerging technology. It supports both prerecorded audio and real-time streaming as inputs, and multiple audio encodings are supported including mp3, wav, and FLAC.


## Get started

### 1. Add Dependency

**Add this to your project build.gradle file:**
```
repositories {
        jcenter()
}
dependencies {
    implementation 'ai.cochlear.sense:cochl-sense:1.1.0'
}
```

 
### 2. Get API Key

Go to https://dashboard.cochlear.ai/

Then sign up and get 10 dollars free of charge.


## Usage

**Import the library.**
```java
import ai.cochlear.sense.Event;
import ai.cochlear.sense.Result;
import ai.cochlear.sense.File;
import ai.cochlear.sense.SenseResultListener;
import ai.cochlear.sense.Stream;

private static final String apiKey = "< Enter API Key >";
```

 **1. Analyze audio files**
 
file represents a class that can inference audio coming from an audio file.

An audio file is any source of audio data which duration is known at runtime. Because duration is known at runtime, server will wait for the whole file to be received before to start inferencing. All inferenced data will be received in one payload.

A file can be for instance, a mp3 file stored locally, a wav file accessible from an url etc...

So far wav, flac, mp3, ogg, mp4 are supported.

If you are using another file encoding format, let us know at support@cochlear.ai so that we can priorize it in our internal roadmap.

File implements the following interface :
```java
class File {
  Result inference() -> Result;
}
```
When calling inference, a GRPC connection will be established with the backend, audio data of the File will be sent and a Result instance will be returned in case of success (described bellow).

Note that network is not reached until inference method is called.

Note that inference can be called only once per file instance.

To create a file instance, you need to use a Builder instance. Builder is following the builder pattern and calling its build method will create a file instance.

fileBuider implements the following interface :
```java
class Builder {
  //api key of cochlear.ai projects available at https://dashboard.cochlear.ai
  void withApiKey(String apiKey) -> Builder;

  //data reader to the file data
  void withReader(InputStream reader) -> Builder;

  //format of the audio file : can be mp3, flac, wav, ogg, etc...
  void withFormat(String format) -> Builder;

  //host address that performs grpc communication.
  //If this method is not used, default host is used.
  void withHost(String host) -> Builder;

  //Turn on or off smart filtering (default off)
  void withSmartFiltering(boolean smartFiltering) -> Builder;


  //creates a File instance
  File build();
}

```
 
**2. Analyze audio stream**

stream represents a class that can inference audio coming from an audio stream.

An audio stream is any source of data which duration is not known at runtime. Because duration is not known, server will inference audio as it comes. One second of audio will be required before the first result to be returned. After that, one result will be given every 0.5 second of audio.

A stream can be for instance, the audio data comming from a microphone, audio data comming from a web radio etc...

Streams can be stopped at any moment while inferencing.

For now, the only format that is supported for streaming is a raw data stream (PCM float32 stream). Raw data being sent has to be a mono channel audio stream.

 Its sampling rate has to be given to describe the raw audio data.

For best performance, we recommend using a sampling rate of 22050Hz and data represented as float32.

Multiple results will be returned by Listener.

If you are using another stream encoding format that is not supported, let us know at support@cochlear.ai so that we can priorize it in our internal roadmap.

Stream implements the following interface :
```java
class Stream {
  void inference(SenseResultListener listener);
}
```

SenseResultLister implements the following interface : 
```java
public interface SenseResultListener {
    void onResult(Result result);
    void onError(Throwable error);
    void onComplete();
}
```

When calling inference, a GRPC connection will be established with the backend, audio data of the stream will be sent every 0.5s. Once result is returned by the server, the add Result function is called.

Note that network is not reached until inference method is called.

Note that inference can be called only once per stream instance.

To create a stream instance, you need to use a Builder instance. Builder is following the builder pattern and calling its build method will create a stream instance.

streamBuilder implements the following interface :
```java
class Builder {
  //api key of cochlear.ai projects available at dashboard.cochlear.ai
  void withApiKey(String apiKey) -> Builder;

  //data of the pcm stream
  void withStreamer(Iterable<byte[]>) -> Builder;

  //max number of events from previous inference to keep in memory
  void withMaxEventsHistorySize(int n) -> Builder;

  //sampling rate of the pcm stream
  void withSamplingRate(int samplingRate) -> Builder;

  //type of the pcm float32 stream
  void withDataType(String dataType) -> Builder;

  //host address that performs grpc communication.
  //If this method is not used, default host is used.
  void withHost(String host) -> Builder;

  //Turn on or off smart filtering (default off)
  void withSmartFiltering(boolean smartFiltering) -> Builder;


  //creates a stream instance
  Stream build();
}
```
Note that withApiKey, withDataType, withSamplingRate and withStreamer method needs to be called before calling the build method, otherwise an error will be thrown.

**3. Result**

Result is a class that is returned by both file and stream when calling inference method.

Multiple results will be returned by a stream. For a file only one result will be returned.

Result implements the following interface :
```java
class Result {
  //returns all events
  List<Event> allEvents();

  //returns all events that match the "filter function" defined below
  List<Event> detectedEvents();

  //group events that match the "filter function" and shows segments of time of when events were detected
  Map detectedEventsTiming();

  //return only the "tag" of the event that match the "filter" function
  List<String> detectedTags();

  //returns the service name : "human-interaction" or "emergency" for instance
  String service();

  //returns a raw json object containing service name and an array of events
  JSONObject toJson ();

  //use a filter function : that function takes an event as input and return a boolean. An event will be "detected" if the filter function returns true for that event
  //the default filter is to consider all events as detected. So by default, allEvents() and detectedEvents() will return the same result
  //where filter is a function that takes an event in input and returns a boolean
  bool filter(Event event);
}
```

Note that if you are inferencing a stream, multiple results will be returned. By default, calling allEvents() will only returned the newly inferenced result. It's possible to keep track of previous events of the stream. To do so, call the withMaxEventsHistorySize method on the streamBuilder class. Its default value is 0, and increasing it will allow to "remember" previous events.

**4. Event**

An event contains the following data :
```java
class Event {
  //name of the detected event
  String tag;

  //probability for the event to happen. Its values is between 0 and 1
  double probability;

  //start timestamp of the detected event since the beginning of the inference
  float startTime;

  //end timestamp of the detected event since the beginning of the inference
  float endTime;
}
```