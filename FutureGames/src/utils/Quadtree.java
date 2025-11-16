package utils;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import entities.Enemy;

public class Quadtree {
    private static final int MAX_OBJECTS = 10;
    private static final int MAX_LEVELS = 5;

    private int level;
    private List<Enemy> objects;
    private Rectangle bounds;
    private Quadtree[] nodes;

    public Quadtree(int level, Rectangle bounds) {
        this.level = level;
        this.bounds = bounds;
        this.objects = new ArrayList<>();
        this.nodes = new Quadtree[4];
    }

    public void clear() {
        objects.clear();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].clear();
                nodes[i] = null;
            }
        }
    }

    public void insert(Enemy enemy) {
        if (nodes[0] != null) {
            int index = getIndex(enemy);
            if (index != -1) {
                nodes[index].insert(enemy);
                return;
            }
        }

        objects.add(enemy);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) split();

            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    nodes[index].insert(objects.remove(i));
                } else {
                    i++;
                }
            }
        }
    }

    public List<Enemy> query(Rectangle range) {
        List<Enemy> result = new ArrayList<>();

        int index = getIndex(range);
        if (index != -1 && nodes[0] != null) {
            result.addAll(nodes[index].query(range));
        }

        result.addAll(objects);
        return result;
    }

    private void split() {
        int subWidth = bounds.width / 2;
        int subHeight = bounds.height / 2;
        int x = bounds.x;
        int y = bounds.y;

        nodes[0] = new Quadtree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight)); // top-right
        nodes[1] = new Quadtree(level + 1, new Rectangle(x, y, subWidth, subHeight)); // top-left
        nodes[2] = new Quadtree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight)); // bottom-left
        nodes[3] = new Quadtree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight)); // bottom-right
    }

    private int getIndex(Enemy enemy) {
        return getIndex(enemy.getBounds());
    }

    private int getIndex(Rectangle rect) {
        int verticalMidpoint = bounds.x + bounds.width / 2;
        int horizontalMidpoint = bounds.y + bounds.height / 2;

        boolean top = rect.y < horizontalMidpoint && rect.y + rect.height < horizontalMidpoint;
        boolean bottom = rect.y > horizontalMidpoint;
        boolean left = rect.x < verticalMidpoint && rect.x + rect.width < verticalMidpoint;
        boolean right = rect.x > verticalMidpoint;

        if (top && right) return 0;
        if (top && left) return 1;
        if (bottom && left) return 2;
        if (bottom && right) return 3;

        return -1; // doesn't fit neatly into a quadrant
    }
}