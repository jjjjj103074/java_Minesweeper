import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Minesweeper extends JFrame {
    private static final int SIZE = 10; // 地图大小
    private static final int MINES = 10; // 地雷数量
    private JButton[][] buttons = new JButton[SIZE][SIZE];
    private boolean[][] mines = new boolean[SIZE][SIZE];
    private int[][] adjacentMines = new int[SIZE][SIZE];
    private boolean[][] revealed = new boolean[SIZE][SIZE];
    private boolean[][] flagged = new boolean[SIZE][SIZE];
    private boolean gameOver = false;
    private JButton restartButton;

    public Minesweeper() {
        setTitle("Minesweeper");
        setSize(600, 600); // 扩大窗口大小以适应重新开始按钮
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));
        add(gridPanel, BorderLayout.CENTER);

        restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> restartGame());
        add(restartButton, BorderLayout.SOUTH);

        createAndAddButtons(gridPanel);

        // Start a new game
        restartGame();

        setVisible(true);
    }

    private void initializeGame() {
        Random rand = new Random();
        int placedMines = 0;

        while (placedMines < MINES) {
            int row = rand.nextInt(SIZE);
            int col = rand.nextInt(SIZE);
            if (!mines[row][col]) {
                mines[row][col] = true;
                placedMines++;
            }
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (mines[row][col]) {
                    adjacentMines[row][col] = -1; // Marking mines
                } else {
                    adjacentMines[row][col] = countAdjacentMines(row, col);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r >= 0 && r < SIZE && c >= 0 && c < SIZE && mines[r][c]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void createAndAddButtons(JPanel gridPanel) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(40, 40));
                button.setFont(new Font("Arial", Font.PLAIN, 18));
                button.setBackground(Color.LIGHT_GRAY);
                button.addActionListener(new ButtonClickListener(row, col));
                button.addMouseListener(new ButtonRightClickListener(row, col));
                buttons[row][col] = button;
                gridPanel.add(button);
            }
        }
    }

    private void reveal(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE || revealed[row][col] || flagged[row][col] || gameOver) {
            return;
        }

        revealed[row][col] = true;
        JButton button = buttons[row][col];

        if (mines[row][col]) {
            button.setText("M");
            button.setBackground(Color.RED);
            gameOver = true;
            JOptionPane.showMessageDialog(this, "Game Over!");
            revealAllMines();
            return;
        }

        int count = adjacentMines[row][col];
        if (count == 0) {
            button.setText("");
            button.setBackground(Color.WHITE);
            for (int r = row - 1; r <= row + 1; r++) {
                for (int c = col - 1; c <= col + 1; c++) {
                    if (r != row || c != col) {
                        reveal(r, c);
                    }
                }
            }
        } else {
            button.setText(Integer.toString(count));
            button.setForeground(getColorForNumber(count));
            button.setBackground(Color.WHITE);
        }

        if (checkVictory()) {
            JOptionPane.showMessageDialog(this, "You Win!");
            gameOver = true;
        }
    }

    private Color getColorForNumber(int number) {
        switch (number) {
            case 1: return Color.BLUE;
            case 2: return Color.GREEN;
            case 3: return Color.RED;
            case 4: return Color.MAGENTA;
            case 5: return Color.ORANGE;
            case 6: return Color.CYAN;
            case 7: return Color.BLACK;
            case 8: return Color.GRAY;
            default: return Color.BLACK;
        }
    }

    private void revealAllMines() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (mines[row][col]) {
                    buttons[row][col].setText("M");
                    buttons[row][col].setBackground(Color.RED);
                }
            }
        }
    }

    private boolean checkVictory() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!mines[row][col] && !revealed[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void restartGame() {
        gameOver = false;
        revealed = new boolean[SIZE][SIZE];
        flagged = new boolean[SIZE][SIZE];
        mines = new boolean[SIZE][SIZE]; // Ensure mines are reset
        adjacentMines = new int[SIZE][SIZE]; // Ensure adjacentMines are reset

        initializeGame();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JButton button = buttons[row][col];
                button.setText("");
                button.setBackground(Color.LIGHT_GRAY);
                button.setEnabled(true);
            }
        }
    }

    private void revealSurrounding(int row, int col) {
        int count = adjacentMines[row][col];
        int flaggedCount = 0;

        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r >= 0 && r < SIZE && c >= 0 && c < SIZE && flagged[r][c]) {
                    flaggedCount++;
                }
            }
        }

        if (count == flaggedCount) {
            for (int r = row - 1; r <= row + 1; r++) {
                for (int c = col - 1; c <= col + 1; c++) {
                    if ((r != row || c != col) && r >= 0 && r < SIZE && c >= 0 && c < SIZE) {
                        if (!flagged[r][c] && !revealed[r][c]) {
                            reveal(r, c);
                        }
                    }
                }
            }
        }
    }

    private class ButtonClickListener implements ActionListener {
        private int row;
        private int col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!gameOver && !flagged[row][col]) {
                if (revealed[row][col]) {
                    revealSurrounding(row, col);
                } else {
                    reveal(row, col);
                }
            }
        }
    }

    private class ButtonRightClickListener extends MouseAdapter {
        private int row;
        private int col;

        public ButtonRightClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3 && !gameOver) {
                JButton button = buttons[row][col];
                if (!revealed[row][col]) {
                    if (flagged[row][col]) {
                        button.setText("");
                        button.setBackground(Color.LIGHT_GRAY);
                        flagged[row][col] = false;
                    } else {
                        button.setText("F");
                        button.setBackground(Color.YELLOW); // 固定颜色
                        flagged[row][col] = true;
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Minesweeper());
    }
}
