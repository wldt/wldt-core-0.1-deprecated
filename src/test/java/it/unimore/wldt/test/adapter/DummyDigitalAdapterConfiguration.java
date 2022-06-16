package it.unimore.wldt.test.adapter;

public class DummyDigitalAdapterConfiguration {

    private String brokerIp = "127.0.0.1";

    private int brokerPort = 1884;

    private String username = "demo";

    private String password = "demo";

    public DummyDigitalAdapterConfiguration() {
    }

    public DummyDigitalAdapterConfiguration(String brokerIp, int brokerPort, String username, String password) {
        this.brokerIp = brokerIp;
        this.brokerPort = brokerPort;
        this.username = username;
        this.password = password;
    }

    public String getBrokerIp() {
        return brokerIp;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    public int getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(int brokerPort) {
        this.brokerPort = brokerPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DummyPhysicalAdapterConfiguration{");
        sb.append("brokerIp='").append(brokerIp).append('\'');
        sb.append(", brokerPort=").append(brokerPort);
        sb.append(", username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
