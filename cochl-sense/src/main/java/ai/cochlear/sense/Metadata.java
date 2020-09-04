package ai.cochlear.sense;

import java.util.Locale;

public class Metadata {
        public String apiKey;
        public String format;
        public boolean smartFiltering;

        public Metadata(){
            apiKey = "";
            format = "";
            smartFiltering = false;
        };

        public Metadata withApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Metadata withFileFormat(String format) {
            this.format = format;
            return this;
        }

        public Metadata withStreamFormat(String format, int samplingRate) {
            this.format = String.format(Locale.US, "PCM(%s,%d,1)", format, samplingRate);
            return this;
        }

        public Metadata withSmartFiltering(boolean smartFiltering) {
            this.smartFiltering = smartFiltering;
            return this;
        }

        public io.grpc.Metadata toGRPCMetadata() {
            io.grpc.Metadata metadata = new io.grpc.Metadata();

            Metadata.addKey(metadata, Constants.API_VERSION_METADATA, Constants.API_VERSION);
            Metadata.addKey(metadata, Constants.USER_AGENT_METADATA, Constants.USER_AGENT);
            Metadata.addKey(metadata, Constants.API_KEY_METADATA, this.apiKey);
            Metadata.addKey(metadata, Constants.FORMAT_METADATA, this.format);
            if(this.smartFiltering) {
                Metadata.addKey(metadata, Constants.SMART_FILTERING_METADATA, "true");
            }

            return metadata;
        }

        private static void addKey(io.grpc.Metadata metadata, String key, String value) {
            metadata.put(io.grpc.Metadata.Key.of(key, io.grpc.Metadata.ASCII_STRING_MARSHALLER), value);
        }
    }
