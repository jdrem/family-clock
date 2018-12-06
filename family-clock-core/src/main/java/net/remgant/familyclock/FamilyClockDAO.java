package net.remgant.familyclock;

import java.util.Collection;
import java.util.Date;

public interface FamilyClockDAO {
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
    void addLocationData(String id, Date timestamp, double lon, double lat, double acc, double alt, double vac);

    String findLocation(String name);

    Collection<Member> findMembers();
}
