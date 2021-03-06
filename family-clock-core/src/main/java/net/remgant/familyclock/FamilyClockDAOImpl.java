package net.remgant.familyclock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FamilyClockDAOImpl implements FamilyClockDAO {
    private final static Logger log = LoggerFactory.getLogger(FamilyClockDAOImpl.class);
    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings({"unused", "WeakerAccess"})
    public FamilyClockDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public FamilyClockDAOImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @Override
    public void addLocationData(String id, Date timestamp, double lat, double lon, double acc, double alt, double vac) {
        int memberId;
        try {
            //noinspection ConstantConditions
            memberId = jdbcTemplate.queryForObject("select id from member where name = ?", new Object[]{id}, Integer.class);
        } catch (EmptyResultDataAccessException emrdae) {
            log.warn("no such user {}",id);
            throw new IDNotFoundException("no such user: "+id);
        }
        jdbcTemplate.update("insert into tracking (member_id,time,lat,lon,acc,alt,vac) values(?,?,?,?,?,?,?)",
                memberId, timestamp, lat, lon, acc, alt, vac);
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @Override
    @Deprecated
    public String findLocation(String name) {
        int memberId;
        try {
            //noinspection ConstantConditions
            memberId = jdbcTemplate.queryForObject("select id from member where name = ?", new Object[]{name}, Integer.class);
        } catch (EmptyResultDataAccessException emrdae) {
            log.warn("no such user {}",name);
            return "Unknown";
        }
        Map<String, Object> map = jdbcTemplate.queryForMap("select lat,lon,acc from tracking where member_id = ? order by time desc limit 1", memberId);
        double lat1 = Math.toRadians((double) map.get("lat"));
        double lon1 = Math.toRadians((double) map.get("lon"));
        double acc = (double) map.get("acc");
        StringBuilder location = new StringBuilder();
        jdbcTemplate.query("select name, lat, lon, radius from location where owner_id = ? order by priority desc", new Object[]{memberId}, resultSet -> {
            double lat2 = Math.toRadians(resultSet.getDouble(2));
            double lon2 = Math.toRadians(resultSet.getDouble(3));

            double dlon = lon2 - lon1;
            double dlat = lat2 - lat1;
            double a = Math.pow(Math.sin(dlat / 2), 2)
                    + Math.cos(lat1) * Math.cos(lat2)
                    * Math.pow(Math.sin(dlon / 2), 2);
            double c = 2 * Math.asin(Math.sqrt(a));
            double r = 6371;
            double d = c * r * 1000.0;
            double rad = resultSet.getDouble(4);
            log.info("{} {} {} {} {} {} {}", name,lat1,lon1,lat2,lon2,d,rad);
            if (d <= rad + acc) {
                location.setLength(0);
                location.append(resultSet.getString(1));
            }
        });

        if (location.length() > 0)
            return location.toString();
        return null;
    }

    @Override
    public Map<String, Object> findCurrentLocation(String name) {
        return jdbcTemplate.queryForMap("select lat,lon,acc from tracking t,member m  " +
                "where t.member_id = m.id and m.name = ? order by time desc limit 1", name);

    }


    public List<Map<String,Object>> findLocationsForName(String name) {
        return jdbcTemplate.queryForList("select l.name, l.lat, l.lon, l.radius from location l, member m " +
                "where l.owner_id = m.id and m.name = ? order by priority desc", new Object[]{name});
    }

    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @Override
    public Collection<Member> findMembers() {
        List<Member> list = new ArrayList<>();
        jdbcTemplate.query("select name, offset, fg_color, bg_color from member", rs -> {
            list.add(new Member(rs.getString(1),rs.getInt(2),
                    Color.decode(rs.getString(3)),Color.decode(rs.getString(4))));
        });
       return list;
    }
}
