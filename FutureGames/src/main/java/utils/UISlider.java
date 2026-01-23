package utils;

import java.awt.*;

public class UISlider {
    public int x, y, width, height;
    public float value; // 0.0 to 1.0
    private Color fillColor = Color.RED;

    public UISlider(int x, int y, int width, float initial) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = 4; // default
        this.value = initial;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics2D g) {
        // Track
        g.setColor(fillColor);
        g.fillRect(x, y, width, height);

        // Knob
        int knobX = (int)(x + value * width);

        // Outline (slightly larger)
        g.setColor(Color.RED);
        g.fillOval(knobX - 10, y - 10, 20, 20);

        // Knob fill
        g.setColor(new Color(30, 30, 30)); // dark knob
        g.fillOval(knobX - 8, y - 8, 16, 16);
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    public boolean contains(int mx, int my) {
        int knobX = (int)(x + value * width);
        return new Rectangle(knobX - height, y - height, height * 2, height * 2).contains(mx, my);
    }

    public void setFromMouse(int mx) {
        value = Math.max(0, Math.min(1, (mx - x) / (float)width));
    }
}