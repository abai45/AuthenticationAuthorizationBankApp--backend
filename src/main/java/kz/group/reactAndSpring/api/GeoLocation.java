package kz.group.reactAndSpring.api;

import lombok.*;

@Getter
@Setter
public class GeoLocation {
    private String ip;
    private String city;
    private String region;
    private String country;
    private String loc;
}
