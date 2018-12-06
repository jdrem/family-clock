package net.remgant.familyclock;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ClockFactory {

    private FamilyClockDAO familyClockDAO;
    private byte[] clockImage;
    Map<String, Object> members;

    public ClockFactory(FamilyClockDAO familyClockDAO) {
        this.familyClockDAO = familyClockDAO;
    }

    @PostConstruct
    public void init() {
        members = familyClockDAO.findMembers();
        refresh();
    }

    private static Map<String, Double> clockPositiosn = new HashMap<>();

    static {
        clockPositiosn.put("Home", 0.0);
        clockPositiosn.put("Work", Math.PI / 3.0);
        clockPositiosn.put("School", 2.0 * Math.PI / 3.0);
        clockPositiosn.put("Studying", Math.PI);
        clockPositiosn.put("Eating", 4.0 * Math.PI / 3.0);
        clockPositiosn.put("Unknown", 5.0 * Math.PI / 3.0);
    }

    public void refresh() {
        ClockBuilder clockBuilder = new ClockBuilder();
        clockBuilder.bounds(800.0, 800.0);
        for (Map.Entry<String,Double> e : clockPositiosn.entrySet()) {
            clockBuilder.position(e.getKey(), e.getValue());
        }
        clockBuilder.format("PNG");
        for (Map.Entry<String, Object> e : members.entrySet()) {
            String loc = familyClockDAO.findLocation(e.getValue().toString());
            Double angle = clockPositiosn.get(loc);
            if (angle == null)
                angle = clockPositiosn.get("Unknown");
            clockBuilder.pointer(e.getValue().toString(),140.0, angle + Math.PI, Color.BLUE);
        }
        clockImage = clockBuilder.build();
    }

    public byte[] getClockImage() {
        return clockImage;
    }
}
