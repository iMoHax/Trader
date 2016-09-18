package ru.trader.emdn.entities;

import java.time.LocalDateTime;

public class Header {
    private final String uploaderId;
    private final String softwareName;
    private final String softwareVersion;
    private LocalDateTime gatewayTimestamp;

    public Header(String uploaderId, String softwareName, String softwareVersion) {
        this.uploaderId = uploaderId;
        this.softwareName = softwareName;
        this.softwareVersion = softwareVersion;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public LocalDateTime getGatewayTimestamp() {
        return gatewayTimestamp;
    }

    public void setGatewayTimestamp(LocalDateTime gatewayTimestamp) {
        this.gatewayTimestamp = gatewayTimestamp;
    }

    @Override
    public String toString() {
        return "Header{" +
                "uploaderId='" + uploaderId + '\'' +
                ", softwareName='" + softwareName + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", gatewayTimestamp=" + gatewayTimestamp +
                '}';
    }
}
