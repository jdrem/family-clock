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
                .position("Studying", Math.PI)
                .position("Eating", 4.0 * Math.PI / 3.0)
                .position("Unkown", 5.0 * Math.PI / 3.0)
                .pointer("XX", 210.0, Math.PI / 3.0 + Math.PI, Color.BLUE)
                .pointer("YY", 160.0,  Math.PI / 3.0 + Math.PI, Color.MAGENTA)
                .pointer("ZZ",110.0, Math.PI  + Math.PI, Color.ORANGE)
                .pointer("AA",60.0, 4.0 * Math.PI / 3.0  + Math.PI, Color.GREEN)
                .format("PNG")
                .build();
        FileOutputStream fos = new FileOutputStream("Clock.png");
        fos.write(b);
        fos.flush();
        fos.close();
    }
}
