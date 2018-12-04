package net.remgant.familyclock;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class ClockBuilder {

    double width = 800.0;
    double height = 800.0;
    List<String> positionNames;
    List<Double> positionAngles;
    List<String> pointerNames;
    List<Double> pointerOffsets;
    List<Double> pointerAngles;
    List<Color> pointerColors;
    String format;

    public ClockBuilder() {
        positionNames = new ArrayList<>();
        positionAngles = new ArrayList<>();
        pointerNames = new ArrayList<>();
        pointerAngles = new ArrayList<>();
        pointerOffsets = new ArrayList<>();
        pointerColors = new ArrayList<>();
    }

    public ClockBuilder bounds(double width, double height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public ClockBuilder position(String name, double angle) {
       this.positionNames.add(name);
       this.positionAngles.add(angle);
       return this;
    }

    public ClockBuilder pointer(String name, double offset, double angle, Color color) {
       this.pointerNames.add(name);
       this.pointerOffsets.add(offset);
       this.pointerAngles.add(angle);
       this.pointerColors.add(color);
       return this;
    }

    public ClockBuilder format(String format) {
        this.format = format;
        return this;
    }

    public byte[] build() {
        BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = image.createGraphics();
         drawCircle3(g, positionNames, positionAngles);
         for (int i=0; i<pointerNames.size(); i++) {
            drawPointer(g, pointerNames.get(i), pointerOffsets.get(i), pointerAngles.get(i), pointerColors.get(i));
         }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, format, baos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return baos.toByteArray();
    }

    void drawCircle3(Graphics2D g, List<String> names, List<Double> angles) {
          g.setColor(Color.WHITE);
          g.fill(new Rectangle2D.Double(0.0, 0.0, 800, 800));
          double d = 700.0;
          Ellipse2D.Double e = new Ellipse2D.Double(-d / 2.0, -d / 2.0, d, d);
          Area a = new Area();
          a.add(new Area(e));
          d = 675.0;
          e = new Ellipse2D.Double(-d / 2.0, -d / 2.0, d, d);
          a.subtract(new Area(e));
          a.transform(AffineTransform.getTranslateInstance(400.0, 400.0));
          g.setColor(Color.BLACK);
          g.fill(a);

          Font font = new Font("Serif", Font.PLAIN, 24);
          FontRenderContext frc = g.getFontRenderContext();

          for (int i = 0; i < names.size(); i++) {
              GlyphVector gv = font.createGlyphVector(frc, names.get(i));
              double r = d / 2.0 + 2.5 * gv.getOutline().getBounds2D().getHeight();
              Area v = new Area(gv.getOutline());
              double theta = angles.get(i);
              //v.transform(AffineTransform.getRotateInstance(theta));
              v.transform(AffineTransform.getTranslateInstance(-gv.getOutline().getBounds2D().getWidth() / 2.0,
                      gv.getOutline().getBounds2D().getHeight() / 2.0));
              v.transform(AffineTransform.getTranslateInstance(400.0, 400.0));
              v.transform(AffineTransform.getTranslateInstance(r * Math.cos(theta - Math.PI / 2.0), r * Math.sin(theta - Math.PI / 2.0)));
              g.fill(v);
          }
      }

      void drawPointer(Graphics2D g, String name, double offset, double rotation, Color color) {
          Area a = new Area();

          Rectangle2D.Double r = new Rectangle2D.Double(-5.0, 0.0, 10.0, 200.0);
          a.add(new Area(r));

          Path2D.Double p = new Path2D.Double();
          p.moveTo(0.0, 200.0);
          p.lineTo(20.0, 200.0);
          p.lineTo(0.0, 245.0);
          p.lineTo(-20.0, 200.0);
          p.lineTo(0.0, 200.0);
          a.add(new Area(p));

          Ellipse2D.Double c = new Ellipse2D.Double(-12.5, -12.5, 25.0, 25.0);
          a.add(new Area(c));
          c = new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0);
          a.subtract(new Area(c));

  //        c = new Ellipse2D.Double(-25.0, 100.0, 50.0, 50.0);
          c = new Ellipse2D.Double(-25.0, offset, 50.0, 50.0);
          a.add(new Area(c));
  //        c = new Ellipse2D.Double(-22.5, 102.5, 45.0, 45.0);
          c = new Ellipse2D.Double(-22.5, offset + 2.5, 45.0, 45.0);

          a.subtract(new Area(c));

  //        a.transform(AffineTransform.getRotateInstance(Math.PI / 3.0));
          a.transform(AffineTransform.getRotateInstance(rotation));

          a.transform(AffineTransform.getTranslateInstance(400.0, 400.0));

          g.setColor(Color.BLACK);
          g.fill(a);

          Font font = new Font("Serif", Font.PLAIN, 24);
          FontRenderContext frc = g.getFontRenderContext();
          GlyphVector gv = font.createGlyphVector(frc, name);
          Area v = new Area(gv.getOutline());
          v.transform(AffineTransform.getScaleInstance(-1.0, -1.0));
          double xoff = gv.getOutline().getBounds2D().getWidth() / 2.0;
          double yoff = gv.getOutline().getBounds().getHeight();
  //        v.transform(AffineTransform.getTranslateInstance(xoff, 100.0+yoff));
          v.transform(AffineTransform.getTranslateInstance(xoff, offset + yoff));


  //        v.transform(AffineTransform.getRotateInstance(Math.PI / 3.0));
          v.transform(AffineTransform.getRotateInstance(rotation));
          v.transform(AffineTransform.getTranslateInstance(400.0, 400.0));
          g.setColor(color);
          g.fill(v);

      }
}
