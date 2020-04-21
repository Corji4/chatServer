package main;

import java.util.Objects;

public class Address {

    private String IP;
    private Integer port;

    public Address(String IP, Integer port) {
        this.IP = IP;
        this.port = port;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(IP, address.IP) &&
                Objects.equals(port, address.port);
    }

    @Override
    public String toString() {
        return IP + ":" + String.valueOf(port);
    }
}
