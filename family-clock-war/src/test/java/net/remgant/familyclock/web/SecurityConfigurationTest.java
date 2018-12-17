package net.remgant.familyclock.web;

import net.remgant.familyclock.ClockFactory;
import net.remgant.familyclock.FamilyClockDAO;
import net.remgant.familyclock.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes =
        {SecurityConfigurationTest.TestConfiguraton.class,
                SecurityConfiguration.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {"rest.user=user", "rest.password=pwd"})
public class SecurityConfigurationTest {
    @Autowired
    private MockMvc mockMvc;

    @Configuration
    @EnableWebMvc
    static class TestConfiguraton {

        @Bean
        public FamilyClockController familyClockController() {
            FamilyClockController controller = new FamilyClockController();
            controller.setFamilyClockDAO(familyClockDAO());
            controller.setClockFactory(new ClockFactory());
            return controller;
        }

        @Bean
        public FamilyClockDAO familyClockDAO() {
            return new FamilyClockDAO() {
                @Override
                public void addLocationData(String id, Date timestamp, double lon, double lat, double acc, double alt, double vac) {

                }

                @Override
                public String findLocation(String name) {
                    return "home";
                }

                @Override
                public Collection<Member> findMembers() {
                    return Collections.emptyList();
                }

                @Override
                public Map<String, Object> findCurrentLocation(String name) {
                    return null;
                }

                @Override
                public List<Map<String, Object>> findLocationsForName(String name) {
                    return null;
                }
            };
        }

        @Bean
        public DataSource authDataSource() {
            DataSource dataSource = new SingleConnectionDataSource("jdbc:hsqldb:mem:AuthDataSource", "SA", "", true);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.update("create table users(\n" +
                    "    username varchar_ignorecase(50) not null primary key,\n" +
                    "    password varchar(100) not null,\n" +
                    "    enabled boolean not null\n" +
                    ")");
            jdbcTemplate.update("create table authorities (\n" +
                    "    username varchar_ignorecase(50) not null,\n" +
                    "    authority varchar_ignorecase(50) not null,\n" +
                    "    constraint fk_authorities_users foreign key(username) references users(username)\n" +
                    ")");
            jdbcTemplate.update("create unique index ix_auth_username on authorities (username,authority)");

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            jdbcTemplate.update("insert into users (username,password,enabled) values('admin',?,true)",
                    new Object[]{passwordEncoder.encode("pwd")});
            jdbcTemplate.update("insert into authorities (username,authority) values('admin','ROLE_ADMIN')");


            return dataSource;
        }

    }

    @Test
    public void testPostToTracking() throws Exception {
        this.mockMvc.perform(post("/tracking")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Basic %s", new String(Base64.encode("user:pwd".getBytes()))))
                .content("{\"test\":\"abc\"}"))
                .andExpect(status().is(204));
    }

    @Test
    public void testPostToTrackingMissingAuthorization() throws Exception {
        this.mockMvc.perform(post("/tracking")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"test\":\"abc\"}"))
                .andExpect(status().is(401));
    }

    @Test
    public void testPostToTrackingIncorrectAuthorization() throws Exception {
        this.mockMvc.perform(post("/tracking")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Basic %s", new String(Base64.encode("user:badpassword".getBytes()))))
                .content("{\"test\":\"abc\"}"))
                .andExpect(status().is(401));
    }

    @Test
    public void testGetFromLocation() throws Exception {
        this.mockMvc.perform(get("/location/XX")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(content().json("{\"XX\":\"home\"}"));
    }

    @Test
    public void testPostStatusPin() throws Exception {
        this.mockMvc.perform(post("/status")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Basic %s", new String(Base64.encode("admin:pwd".getBytes()))))
                .content("{\"action\":\"pinLocation\",\"configuration\":{\"id\":\"XX\",\"location\":\"Mortal Peril\"}}"))
                .andExpect(status().is(200))
                .andExpect(content().json("{\"status\":\"OK\"}"));
    }

    @Test
    public void testPostStatusUnpin() throws Exception {
        this.mockMvc.perform(post("/status")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", String.format("Basic %s", new String(Base64.encode("admin:pwd".getBytes()))))
                .content("{\"action\":\"unpinLocation\",\"configuration\":{\"id\":\"XX\"}}"))
                .andExpect(status().is(200))
                .andExpect(content().json("{\"status\":\"OK\"}"));
    }

    @Test
    public void testpasswordcreate() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("pwd"));
    }
}
