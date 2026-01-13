package utils;

import java.awt.*;

public class UISlider {
    public int x, y, width;
    public float value; // 0.0 to 1.0

    public UISlider(int x, int y, int width, float initial) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.value = initial;
    }

    public void draw(Graphics2D g) {
        // Track
        g.setColor(new Color(80, 80, 80)); // darker gray
        g.fillRect(x, y, width, 6);

        // Knob
        int knobX = (int)(x + value * width);
        g.setColor(new Color(30, 30, 30)); // dark knob
        g.fillOval(knobX - 8, y - 8, 16, 16);
    }

    public boolean contains(int mx, int my) {
        int knobX = (int)(x + value * width);
        return new Rectangle(knobX - 6, y - 6, 12, 12).contains(mx, my);
    }

    public void setFromMouse(int mx) {
        value = Math.max(0, Math.min(1, (mx - x) / (float)width));
    }
}