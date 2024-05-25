package kz.group.reactAndSpring.api;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoLocationApi {
    private static final String BASE_URL = "http://ipinfo.io/";

    private final RestTemplate restTemplate;

    @Autowired
    public GeoLocationApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GeoLocation geoLocationApi(String ip) {
        String url = BASE_URL + ip + "/geo";
        return restTemplate.getForObject(url, GeoLocation.class);
    }
}
