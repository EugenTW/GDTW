package com.GDTW.safebrowsing4.service;

public class SafeBrowsingV4RequestDTO {

    private Client client;
    private ThreatInfo threatInfo;

    public SafeBrowsingV4RequestDTO(String url) {
        this.client = new Client("GDTW_Short_URL", "1.0");
        this.threatInfo = new ThreatInfo(url);
    }

    public static class Client {
        private String clientId;
        private String clientVersion;

        public Client(String clientId, String clientVersion) {
            this.clientId = clientId;
            this.clientVersion = clientVersion;
        }

        // Getters and Setters
        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientVersion() {
            return clientVersion;
        }

        public void setClientVersion(String clientVersion) {
            this.clientVersion = clientVersion;
        }
    }

    public static class ThreatInfo {
        private String[] threatTypes = {"MALWARE", "SOCIAL_ENGINEERING"};
        private String[] platformTypes = {"ANY_PLATFORM"};
        private String[] threatEntryTypes = {"URL"};
        private ThreatEntry[] threatEntries;

        public ThreatInfo(String url) {
            this.threatEntries = new ThreatEntry[]{new ThreatEntry(url)};
        }

        public static class ThreatEntry {
            private String url;

            public ThreatEntry(String url) {
                this.url = url;
            }

            // Getters and Setters
            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }

        // Getters and Setters
        public String[] getThreatTypes() {
            return threatTypes;
        }

        public void setThreatTypes(String[] threatTypes) {
            this.threatTypes = threatTypes;
        }

        public String[] getPlatformTypes() {
            return platformTypes;
        }

        public void setPlatformTypes(String[] platformTypes) {
            this.platformTypes = platformTypes;
        }

        public String[] getThreatEntryTypes() {
            return threatEntryTypes;
        }

        public void setThreatEntryTypes(String[] threatEntryTypes) {
            this.threatEntryTypes = threatEntryTypes;
        }

        public ThreatEntry[] getThreatEntries() {
            return threatEntries;
        }

        public void setThreatEntries(ThreatEntry[] threatEntries) {
            this.threatEntries = threatEntries;
        }
    }

    // Getters and Setters for client and threatInfo
    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public ThreatInfo getThreatInfo() {
        return threatInfo;
    }

    public void setThreatInfo(ThreatInfo threatInfo) {
        this.threatInfo = threatInfo;
    }
}
