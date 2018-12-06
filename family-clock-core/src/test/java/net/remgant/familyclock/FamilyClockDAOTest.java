package net.remgant.familyclock;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

public class FamilyClockDAOTest {

    JdbcTemplate jdbcTemplate;

    @Before
    public void setup() {
        DataSource dataSource = new SingleConnectionDataSource("jdbc:hsqldb:mem:FamilyClockDAOTest", "SA", "", false);
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("create table member (" +
                "id int primary key, " +
                "name char(2) null " +
                ");");
        jdbcTemplate.update("insert into member (id,name) values(2,'GR')");
        jdbcTemplate.update("create table tracking\n" +
                "(\n" +
                "\tmember_id int not null,\n" +
                "\ttime timestamp not null,\n" +
                "\tlat double not null,\n" +
                "\tlon double not null,\n" +
                "\tacc double null,\n" +
                "\talt double not null,\n" +
                "\tvac double null\n" +
                ")\n" +
                ";\n");
        jdbcTemplate.update("insert into tracking (member_id,time,lat,lon,acc,alt,vac) " +
                "values(2,'2018-12-05 23:10:18',42.25470982961566,-72.57404970943236,5,58,12)");
        jdbcTemplate.update("create table location\n" +
                "(\n" +
                "\tid int\n" +
                "\t\tprimary key,\n" +
                "\towner_id int null,\n" +
                "\tname varchar(32) null,\n" +
                "\tlon double null,\n" +
                "\tlat double null,\n" +
                "\tradius double null,\n" +
                "\tpriority int default '1' null\n" +
                ")");

        jdbcTemplate.update("INSERT INTO location (id, owner_id, name, lon, lat, radius, priority) VALUES (3, 2, 'School', -72.57416, 42.25555, 500, 2)");
        jdbcTemplate.update("INSERT INTO location (id, owner_id, name, lon, lat, radius, priority) VALUES (4, 2, 'Eating', -72.57365, 42.25473, 50, 1)");
        jdbcTemplate.update("INSERT INTO location (id, owner_id, name, lon, lat, radius, priority) VALUES (5, 2, 'Studying', -72.57287, 42.25736, 25, 1)");
        jdbcTemplate.update("INSERT INTO location (id, owner_id, name, lon, lat, radius, priority) VALUES (6, 2, 'Studying', -72.5751, 42.2581, 25, 1)");
    }

    @Test
    public void test() {
        FamilyClockDAOImpl dao = new FamilyClockDAOImpl(jdbcTemplate);
        assertEquals("Eating", dao.findLocation("GR"));
    }
}
