package net.remgant.familyclock.web;

import net.remgant.familyclock.ClockFactory;
import net.remgant.familyclock.FamilyClockDAO;
import net.remgant.familyclock.FamilyClockDAOImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@SpringBootApplication
@EnableScheduling
public class Application extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Application.class);
    }

    public static void main(String[] args) {
        initInitialContext();
        SpringApplication.run(Application.class, args);
    }

    private static void initInitialContext() {
        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        String dbUrl = System.getProperty("db.url");
        String dbUser = System.getProperty("db.user");
        String dbPwd = System.getProperty("db.pwd");
        DataSource dataSource = new DriverManagerDataSource(dbUrl, dbUser, dbPwd);
        builder.bind("java:/comp/env/jdbc/familyclock", dataSource);
        try {
            builder.activate();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public FamilyClockController familyClockController() {
        FamilyClockController controller = new FamilyClockController();
        controller.setFamilyClockDAO(familyClockDAO());
        controller.setClockFactory(clockFactory());
        return controller;
    }

    @Bean
    public FamilyClockDAO familyClockDAO() {
        return new FamilyClockDAOImpl(dataSource());
    }

    @Bean
    public ClockFactory clockFactory() {
        ClockFactory clockFactory = new ClockFactory();
        clockFactory.setDao(familyClockDAO());
        clockFactory.setClockPoints(new String[]{"Home","Work","School","Studying","Play-ing","Eating","Unknown"});
        return clockFactory;
    }

    @Bean
    public ClockFactoryWrapper clockFactoryWrapper() {
        return new ClockFactoryWrapper(clockFactory());
    }

    public static class ClockFactoryWrapper {
        ClockFactory clockFactory;

        public ClockFactoryWrapper(ClockFactory clockFactory) {
            this.clockFactory = clockFactory;
        }

        @Scheduled(cron = "0 */5 * * * *")
        public void refresh() {
            if (clockFactory != null)
                clockFactory.refresh();
        }
    }

    @Bean
    public MBeanExporter exporter() {
        final MBeanExporter exporter = new AnnotationMBeanExporter();
        exporter.setAutodetect(true);
        exporter.setExcludedBeans("dataSource");
        return exporter;
    }

    @Bean
    public DataSource dataSource() {
        Context initContext;
        try {
            initContext = new InitialContext();
            return (DataSource) initContext.lookup("java:/comp/env/jdbc/familyclock");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

}
