package net.remgant.familyclock.web;

import net.remgant.familyclock.ClockFactory;
import net.remgant.familyclock.FamilyClockDAO;
import net.remgant.familyclock.IDNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

@RestController
public class FamilyClockController {

    private final static Logger log = LoggerFactory.getLogger(FamilyClockController.class);

    private FamilyClockDAO familyClockDAO;
    private ClockFactory clockFactory;
    /*
     {cog=308,
     batt=95,
     lon=-71.56352134428133,
     acc=10,
     p=99.55830383300781,
     vel=1,
     vac=8,
     lat=42.53898551225898,
     topic=owntracks/user/8AAE8FF2-C600-4116-836B-F7C6D62BD7B3,
     t=u,
     conn=w,
     tst=1543493939,
     alt=96,
     _type=location,
     tid=JR}

     */

    @RequestMapping(value = "/tracking", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void processTrackingData(@RequestBody Map<String, Object> data) {
        log.info("recieved data: {}", data);

        if (!data.containsKey("_type") || !data.get("_type").equals("location"))
            return;

        double lat = ((Number) data.get("lat")).doubleValue();
        double lon = ((Number) data.get("lon")).doubleValue();
        double acc = ((Number) data.get("acc")).doubleValue();
        double alt = ((Number) data.get("alt")).doubleValue();
        double vac = data.containsKey ("vac") ? ((Number) data.get("vac")).doubleValue() : 0.0;
        int ts = ((Number) data.get("tst")).intValue();
        String id = (String) data.get("tid");
        log.info("lon = {}, lat = {}, alt = {}, ts = {}", lon, lat, alt, new Date(ts * 1000L));
        familyClockDAO.addLocationData(id, new Date(ts * 1000L), lat, lon, acc, alt, vac);
    }

    @RequestMapping(value = "/location/{name}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> findLocationFor(@PathVariable("name") String name) {
        String location = familyClockDAO.findLocation(name);
        if (location == null)
            return Collections.singletonMap(name, "unkown");
        return Collections.singletonMap(name, location);

    }

    @RequestMapping(value = "/clock.png", method = RequestMethod.GET, produces = "image/png")
    @ResponseBody
    public Object clock() {
        return clockFactory.getClockImage();
    }


    @RequestMapping(value = "/clock", method = RequestMethod.GET, produces = "text/html")
    public String clockIndex() {
        return "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "<head>\r\n" +
                "    <meta http-equiv=\"refresh\" content=\"60\">\r\n" +
                "</head>\r\n" +
                "<body>\r\n" +
                "<img src=\"/clock.png\">\r\n" +
                "</body>\r\n" +
                "</html>\r\n";
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User not found")
    @ExceptionHandler(IDNotFoundException.class)
    public void userNotFound() {

    }

    public void setFamilyClockDAO(FamilyClockDAO familyClockDAO) {
        this.familyClockDAO = familyClockDAO;
    }

    public void setClockFactory(ClockFactory clockFactory) {
        this.clockFactory = clockFactory;
    }
}
