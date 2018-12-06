package net.remgant.familyclock;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClockFactory {

    private FamilyClockDAO familyClockDAO;
    private byte[] clockImage;
    private Collection<Member> members;
    private String[] points = new String[]{"Home","Work","School","Unkown"};
    private Map<String, Double> clockPositiosn;

    public ClockFactory() {
    }

    public ClockFactory(FamilyClockDAO familyClockDAO) {
        this.familyClockDAO = familyClockDAO;
    }

    @PostConstruct
    public void init() {
        clockPositiosn = new HashMap<>();
        for (int i = 0; i< points.length; i++) {
            clockPositiosn.put(points[i],((double)i / (double) points.length) * (2.0 * Math.PI));
        }
        members = familyClockDAO.findMembers();
        refresh();
    }

    public void refresh() {
        ClockBuilder clockBuilder = new ClockBuilder();
        clockBuilder.bounds(800.0, 800.0);
        for (Map.Entry<String,Double> e : clockPositiosn.entrySet()) {
            clockBuilder.position(e.getKey(), e.getValue());
        }
        clockBuilder.format("PNG");
        double offsets[] = new double[]{210.0, 160.0, 110.0, 60.0};
        for (Member m : members) {
            String loc = familyClockDAO.findLocation(m.getName());
            Double angle = clockPositiosn.get(loc);
            if (angle == null)
                angle = clockPositiosn.get("Unknown");
            clockBuilder.pointer(m.getName(), offsets[m.getOffset()], angle + Math.PI, m.getForegroundColor());
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
