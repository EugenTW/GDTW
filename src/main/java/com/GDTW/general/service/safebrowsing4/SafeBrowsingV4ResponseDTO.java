package com.GDTW.general.service.safebrowsing4;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class SafeBrowsingV4ResponseDTO {

    private List<ThreatMatch> matches;

    public List<ThreatMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<ThreatMatch> matches) {
        this.matches = matches;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThreatMatch {

        private String threatType;
        private String platformType;
        private String threatEntryType;
        private Threat threat;
        private String cacheDuration;

        // Getters and Setters
        public String getThreatType() {
            return threatType;
        }

        public void setThreatType(String threatType) {
            this.threatType = threatType;
        }

        public String getPlatformType() {
            return platformType;
        }

        public void setPlatformType(String platformType) {
            this.platformType = platformType;
        }

        public String getThreatEntryType() {
            return threatEntryType;
        }

        public void setThreatEntryType(String threatEntryType) {
            this.threatEntryType = threatEntryType;
        }

        public Threat getThreat() {
            return threat;
        }

        public void setThreat(Threat threat) {
            this.threat = threat;
        }

        public String getCacheDuration() {
            return cacheDuration;
        }

        public void setCacheDuration(String cacheDuration) {
            this.cacheDuration = cacheDuration;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Threat {
            private String url;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}
