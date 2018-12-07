package net.remgant.familyclock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClockFactory {

    private final static Logger log = LoggerFactory.getLogger(ClockFactory.class);

    private FamilyClockDAO familyClockDAO;
    private byte[] clockImage;
    private Collection<Member> members;
    private String[] points = new String[]{"Home", "Work", "School", "Unknown"};
    private Map<String, Double> clockPositiosn;

    public ClockFactory() {
    }

    public ClockFactory(FamilyClockDAO familyClockDAO) {
        this.familyClockDAO = familyClockDAO;
    }

    @PostConstruct
    public void init() {
        clockPositiosn = new HashMap<>();
        for (int i = 0; i < points.length; i++) {
            clockPositiosn.put(points[i], ((double) i / (double) points.length) * (2.0 * Math.PI));
        }
        members = familyClockDAO.findMembers();
        refresh();
    }

    public void refresh() {
        ClockBuilder clockBuilder = new ClockBuilder();
        clockBuilder.bounds(800.0, 800.0);
        for (Map.Entry<String, Double> e : clockPositiosn.entrySet()) {
            clockBuilder.position(e.getKey(), e.getValue());
        }
        clockBuilder.format("PNG");
        double offsets[] = new double[]{210.0, 160.0, 110.0, 60.0};
        for (Member member : members) {
            Map<String, Object> map = familyClockDAO.findCurrentLocation(member.getName());
            double lat1 = Math.toRadians((double) map.get("lat"));
            double lon1 = Math.toRadians((double) map.get("lon"));
            double acc = (double) map.get("acc");
            String memberLocation = "Unknown";
            for (Map<String, Object> location : familyClockDAO.findLocationsForName(member.getName())) {

                double lat2 = Math.toRadians((double) location.get("lat"));
                double lon2 = Math.toRadians((double) location.get("lon"));

                double dlon = lon2 - lon1;
                double dlat = lat2 - lat1;
                double a = Math.pow(Math.sin(dlat / 2), 2)
                        + Math.cos(lat1) * Math.cos(lat2)
                        * Math.pow(Math.sin(dlon / 2), 2);
                double c = 2 * Math.asin(Math.sqrt(a));
                double r = 6371;
                double d = c * r * 1000.0;
                double rad = (double) location.get("radius");
                log.info("{} {} {} {} {} {} {}", member.getName(), lat1, lon1, lat2, lon2, d, rad);
                if (d <= rad + acc) {
                    memberLocation = location.get("name").toString();
                }

            }
            Double angle = clockPositiosn.get(memberLocation);
            if (angle == null)
                angle = clockPositiosn.get("Unknown");
            clockBuilder.pointer(member.getName(), offsets[member.getOffset()], angle + Math.PI, member.getForegroundColor());
        }
        clockImage = clockBuilder.build();
    }

    public byte[] getClockImage() {
        return clockImage;
    }

    public void setDao(FamilyClockDAO familyClockDAO) {
        this.familyClockDAO = familyClockDAO;
    }

    public void setClockPoints(String[] points) {
        this.points = points;
    }
}
