package net.remgant.familyclock;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.sql.DataSource;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FamilyClockDAOImpl implements FamilyClockDAO {
    private JdbcTemplate jdbcTemplate;


    public FamilyClockDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public FamilyClockDAOImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void addLocationData(String id, Date timestamp, double lat, double lon, double acc, double alt, double vac) {
        int memberId = jdbcTemplate.queryForObject("select id from member where name = ?", new Object[]{id}, Integer.class);
        jdbcTemplate.update("insert into tracking (member_id,time,lat,lon,acc,alt,vac) values(?,?,?,?,?,?,?)",
                new Object[]{memberId, timestamp, lat, lon, acc, alt, vac});
    }

    @Override
    public String findLocation(String name) {
        int memberId = jdbcTemplate.queryForObject("select id from member where name = ?", new Object[]{name}, Integer.class);
        Map<String, Object> map = jdbcTemplate.queryForMap("select lat,lon,acc from tracking where member_id = ? order by time desc limit 1", new Object[]{memberId});
        double lat1 = Math.toRadians((double) map.get("lat"));
        double lon1 = Math.toRadians((double) map.get("lon"));
        double acc = (double) map.get("acc");
        final List<String> list = new ArrayList();
        jdbcTemplate.query("select name, lat, lon, radius from location where owner_id = ?", new Object[]{memberId}, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
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
                if (d <= rad + acc)
                    list.add(resultSet.getString(1));
            }
        });

        if (list.size() > 0)
            return list.get(0);
        return null;
    }
}
