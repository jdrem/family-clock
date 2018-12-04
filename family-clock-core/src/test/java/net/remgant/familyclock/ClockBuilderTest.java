package net.remgant.familyclock;

import org.junit.Test;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClockBuilderTest {
    @Test
    public void test() throws IOException {
        ClockBuilder clockBuilder = new ClockBuilder();
        byte b[] = clockBuilder.bounds(800.0, 800.0)
                .position("Home", 0.0)
                .position("Work", Math.PI / 3.0)
                .position("School", 2.0 * Math.PI / 3.0)
                .position("Studying", Math.PI / 2.0)
                .position("Eating", 4.0 * Math.PI / 3.0)
                .position("Unkown", 5.0 * Math.PI / 3.0)
                .pointer("JR", 140.0, Math.PI / 3.0 + Math.PI, Color.BLUE)
                .pointer("GR", 90.0, 2.0 * Math.PI / 3.0 + Math.PI, Color.MAGENTA)
                .format("PNG")
                .build();
        FileOutputStream fos = new FileOutputStream("Clock.png");
        fos.write(b);
        fos.flush();
        fos.close();
    }
}
