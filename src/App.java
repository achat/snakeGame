import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new GamePanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static class GamePanel extends JPanel {
        private static final int CELL_SIZE = 25;
        private static final int COLUMNS = 20;
        private static final int ROWS = 20;

        private final Deque<Point> snake = new ArrayDeque<>();
        private final Random random = new Random();
        private final Timer timer;
        private Point food;
        private int dx;
        private int dy;
        private int nextDx;
        private int nextDy;
        private boolean gameOver;

        GamePanel() {
            setPreferredSize(new Dimension(COLUMNS * CELL_SIZE, ROWS * CELL_SIZE));
            setBackground(new Color(25, 30, 35));
            setFocusable(true);
            bindKey("UP", KeyEvent.VK_UP, 0, -1);
            bindKey("DOWN", KeyEvent.VK_DOWN, 0, 1);
            bindKey("LEFT", KeyEvent.VK_LEFT, -1, 0);
            bindKey("RIGHT", KeyEvent.VK_RIGHT, 1, 0);
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "MOVE_UP");
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "MOVE_DOWN");
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "MOVE_LEFT");
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "MOVE_RIGHT");
            getActionMap().put("RESTART", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    if (gameOver) {
                        startGame();
                    }
                }
            });
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "RESTART");

            timer = new Timer(240, event -> moveSnake());
            startGame();
        }

        private void bindKey(String name, int keyCode, int newDx, int newDy) {
            String actionName = "MOVE_" + name;
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(keyCode, 0), actionName);
            getActionMap().put(actionName, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    if (gameOver || (newDx == -dx && newDy == -dy)) {
                        return;
                    }
                    nextDx = newDx;
                    nextDy = newDy;
                }
            });
        }

        private void startGame() {
            snake.clear();
            snake.add(new Point(COLUMNS / 2, ROWS / 2));
            snake.add(new Point(COLUMNS / 2 - 1, ROWS / 2));
            snake.add(new Point(COLUMNS / 2 - 2, ROWS / 2));
            dx = 1;
            dy = 0;
            nextDx = dx;
            nextDy = dy;
            gameOver = false;
            placeFood();
            timer.start();
            repaint();
        }

        private void placeFood() {
            Point candidate;
            do {
                candidate = new Point(random.nextInt(COLUMNS), random.nextInt(ROWS));
            } while (snake.contains(candidate));
            food = candidate;
        }

        private void moveSnake() {
            if (gameOver) {
                return;
            }

            dx = nextDx;
            dy = nextDy;
            Point head = snake.peekFirst();
            Point newHead = new Point(head.x + dx, head.y + dy);
            boolean eating = newHead.equals(food);
            Point tail = snake.peekLast();

            if (newHead.x < 0 || newHead.x >= COLUMNS
                    || newHead.y < 0 || newHead.y >= ROWS
                    || (snake.contains(newHead) && (eating || !newHead.equals(tail)))) {
                gameOver = true;
                timer.stop();
                repaint();
                return;
            }

            snake.addFirst(newHead);
            if (eating) {
                placeFood();
            } else {
                snake.removeLast();
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            graphics.setColor(new Color(45, 52, 58));
            for (int x = 0; x < COLUMNS; x++) {
                for (int y = 0; y < ROWS; y++) {
                    graphics.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }

            graphics.setColor(new Color(240, 85, 75));
            graphics.fillOval(food.x * CELL_SIZE + 3, food.y * CELL_SIZE + 3,
                    CELL_SIZE - 6, CELL_SIZE - 6);

            boolean isHead = true;
            for (Point segment : snake) {
                graphics.setColor(isHead ? new Color(120, 230, 120) : new Color(70, 180, 95));
                graphics.fillRoundRect(segment.x * CELL_SIZE + 2, segment.y * CELL_SIZE + 2,
                        CELL_SIZE - 4, CELL_SIZE - 4, 8, 8);
                isHead = false;
            }

            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            graphics.drawString("Score: " + (snake.size() - 3), 8, 20);

            if (gameOver) {
                graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
                String message = "Game Over";
                int textX = (getWidth() - graphics.getFontMetrics().stringWidth(message)) / 2;
                graphics.drawString(message, textX, getHeight() / 2 - 8);
                graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
                String restart = "Press Enter to restart";
                textX = (getWidth() - graphics.getFontMetrics().stringWidth(restart)) / 2;
                graphics.drawString(restart, textX, getHeight() / 2 + 22);
            }
        }
    }

    private static class Point {
        final int x;
        final int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Point)) {
                return false;
            }
            Point point = (Point) other;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }
}
