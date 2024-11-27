package com.GDTW.safebrowing4.service;

import java.util.List;

public class SafeBrowsingV4ResponseDTO {

    private List<ThreatMatch> matches;

    public List<ThreatMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<ThreatMatch> matches) {
        this.matches = matches;
    }

    public static class ThreatMatch {
        private String threatType;
        private String platformType;
        private String threatEntryType;
        private Threat threat;

        // Getters and setters
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

        public static class Threat {
            private String url;

            // Getters and setters
            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }

    }
}
