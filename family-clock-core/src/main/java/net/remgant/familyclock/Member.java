package net.remgant.familyclock;

import java.awt.*;

@SuppressWarnings("WeakerAccess")
public class Member {
    private String name;
    private int offset;
    private Color foregroundColor;
    private Color backgroundColor;

    public Member(String name, int offset, Color foregroundColor, Color backgroundColor) {
        this.name = name;
        this.offset = offset;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    @SuppressWarnings("unused")
    public Color getBackgroundColor() {
        return backgroundColor;
    }
}
