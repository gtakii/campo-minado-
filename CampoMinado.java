import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class CampoMinado extends JFrame {
    private enum Level {
        FACIL(8, 8, 10, "FÁCIL"),
        MEDIO(10, 10, 15, "MÉDIO"),
        DIFICIL(12, 12, 25, "DIFÍCIL");

        final int rows, cols, mines;
        final String label;

        Level(int rows, int cols, int mines, String label) {
            this.rows = rows;
            this.cols = cols;
            this.mines = mines;
            this.label = label;
        }
    }

    private Level currentLevel = Level.FACIL;
    private int rows, cols, mines;
    private boolean[][] mineGrid;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int flagsLeft;
    private boolean firstClick = true;
    private boolean gameOver = false;
    private int seconds = 0;
    private Timer timer;

    // Componentes de Interface
    private JLabel minesLabel;
    private JLabel timerLabel;
    private JPanel gridPanel;
    private JButton[][] cellButtons;
    private JButton btnFacil, btnMedio, btnDificil;
    private JButton btnRestart;

    // Cores (Tema Preto e Branco Minimalista)
    private static final Color BG_DARK = new Color(12, 12, 12);
    private static final Color CARD_BG = new Color(24, 24, 24);
    private static final Color BORDER_COLOR = new Color(45, 45, 45);
    private static final Color CELL_UNREVEALED = new Color(32, 32, 32);
    private static final Color CELL_HOVER = new Color(55, 55, 55);
    private static final Color CELL_REVEALED = new Color(18, 18, 18);
    private static final Color TEXT_WHITE = new Color(245, 245, 245);
    private static final Color TEXT_MUTED = new Color(150, 150, 150);
    private static final Color TEXT_DIM = new Color(80, 80, 80);

    public CampoMinado() {
        setTitle("CAMPO MINADO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setBackground(BG_DARK);

        initUI();
        startNewGame(Level.FACIL);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cabeçalho
        JLabel titleLabel = new JLabel("CAMPO MINADO");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("EDIÇÃO MINIMALISTA");
        subtitleLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
        subtitleLabel.setForeground(TEXT_DIM);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Níveis de Dificuldade
        JPanel levelPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        levelPanel.setOpaque(false);
        levelPanel.setMaximumSize(new Dimension(340, 32));
        levelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnFacil = createNavButton(Level.FACIL);
        btnMedio = createNavButton(Level.MEDIO);
        btnDificil = createNavButton(Level.DIFICIL);

        levelPanel.add(btnFacil);
        levelPanel.add(btnMedio);
        levelPanel.add(btnDificil);

        // Painel de Status (Minas, Reiniciar e Tempo)
        JPanel statusPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        statusPanel.setOpaque(false);
        statusPanel.setMaximumSize(new Dimension(340, 48));
        statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel minesBox = createStatBox("MINAS", minesLabel = new JLabel("00"));
        JPanel timerBox = createStatBox("TEMPO", timerLabel = new JLabel("000s"));

        btnRestart = new JButton("↻ NOVO");
        btnRestart.setFont(new Font("Monospaced", Font.BOLD, 11));
        btnRestart.setFocusPainted(false);
        btnRestart.setBackground(CARD_BG);
        btnRestart.setForeground(TEXT_WHITE);
        btnRestart.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        btnRestart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRestart.setOpaque(true);
        btnRestart.setContentAreaFilled(true);
        btnRestart.addActionListener(e -> startNewGame(currentLevel));

        statusPanel.add(minesBox);
        statusPanel.add(btnRestart);
        statusPanel.add(timerBox);

        // Grid do Jogo
        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        mainPanel.add(levelPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        mainPanel.add(statusPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        mainPanel.add(gridPanel);

        add(mainPanel);

        timer = new Timer(1000, e -> {
            seconds++;
            timerLabel.setText(String.format("%03ds", seconds));
        });
    }

    private JButton createNavButton(Level lvl) {
        JButton btn = new JButton(lvl.label);
        btn.setFont(new Font("Monospaced", Font.BOLD, 10));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.addActionListener(e -> startNewGame(lvl));
        return btn;
    }

    private JPanel createStatBox(String title, JLabel valueLabel) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(CARD_BG);
        box.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JLabel tLabel = new JLabel(title);
        tLabel.setFont(new Font("Monospaced", Font.PLAIN, 9));
        tLabel.setForeground(TEXT_DIM);
        tLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
        valueLabel.setForeground(TEXT_WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(Box.createVerticalGlue());
        box.add(tLabel);
        box.add(valueLabel);
        box.add(Box.createVerticalGlue());
        return box;
    }

    private void updateLevelButtons() {
        updateNavButtonStyle(btnFacil, currentLevel == Level.FACIL);
        updateNavButtonStyle(btnMedio, currentLevel == Level.MEDIO);
        updateNavButtonStyle(btnDificil, currentLevel == Level.DIFICIL);
    }

    private void updateNavButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(TEXT_WHITE);
            btn.setForeground(BG_DARK);
            btn.setBorder(BorderFactory.createLineBorder(TEXT_WHITE, 1));
        } else {
            btn.setBackground(CARD_BG);
            btn.setForeground(TEXT_MUTED);
            btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        }
    }

    private void startNewGame(Level level) {
        this.currentLevel = level;
        this.rows = level.rows;
        this.cols = level.cols;
        this.mines = level.mines;
        this.flagsLeft = mines;
        this.firstClick = true;
        this.gameOver = false;
        this.seconds = 0;

        if (timer != null) timer.stop();
        timerLabel.setText("000s");
        minesLabel.setText(String.format("%02d", flagsLeft));

        updateLevelButtons();

        mineGrid = new boolean[rows][cols];
        revealed = new boolean[rows][cols];
        flagged = new boolean[rows][cols];

        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(rows, cols, 3, 3));
        cellButtons = new JButton[rows][cols];

        int cellSize = (currentLevel == Level.FACIL) ? 44 : (currentLevel == Level.MEDIO ? 38 : 32);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final int row = r;
                final int col = c;
                JButton btn = new JButton("");
                btn.setPreferredSize(new Dimension(cellSize, cellSize));
                btn.setFont(new Font("SansSerif", Font.BOLD, cellSize / 2));
                btn.setFocusPainted(false);
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setBackground(CELL_UNREVEALED);
                btn.setForeground(TEXT_WHITE);
                btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!revealed[row][col] && !gameOver) {
                            btn.setBackground(CELL_HOVER);
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!revealed[row][col] && !gameOver) {
                            btn.setBackground(CELL_UNREVEALED);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (gameOver) return;

                        if (SwingUtilities.isRightMouseButton(e)) {
                            toggleFlag(row, col);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            handleCellClick(row, col);
                        }
                    }
                });

                cellButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
        pack();
        setLocationRelativeTo(null);
    }

    private void placeMines(int avoidRow, int avoidCol) {
        Random rand = new Random();
        int placed = 0;
        while (placed < mines) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if ((r != avoidRow || c != avoidCol) && !mineGrid[r][c]) {
                mineGrid[r][c] = true;
                placed++;
            }
        }
    }

    private void handleCellClick(int r, int c) {
        if (flagged[r][c] || revealed[r][c]) return;

        if (firstClick) {
            firstClick = false;
            placeMines(r, c);
            timer.start();
        }

        if (mineGrid[r][c]) {
            triggerGameOver(false, r, c);
            return;
        }

        revealCell(r, c);

        if (checkWin()) {
            triggerGameOver(true, -1, -1);
        }
    }

    private void revealCell(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return;
        if (revealed[r][c] || flagged[r][c]) return;

        revealed[r][c] = true;
        JButton btn = cellButtons[r][c];
        btn.setBackground(CELL_REVEALED);
        btn.setBorder(BorderFactory.createLineBorder(new Color(35, 35, 35), 1));

        int adjacentMines = countAdjacentMines(r, c);
        if (adjacentMines > 0) {
            btn.setText(String.valueOf(adjacentMines));
            btn.setForeground(TEXT_WHITE);
        } else {
            btn.setText("");
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr != 0 || dc != 0) {
                        revealCell(r + dr, c + dc);
                    }
                }
            }
        }
    }

    private void toggleFlag(int r, int c) {
        if (revealed[r][c]) return;

        if (flagged[r][c]) {
            flagged[r][c] = false;
            cellButtons[r][c].setText("");
            flagsLeft++;
        } else if (flagsLeft > 0) {
            flagged[r][c] = true;
            cellButtons[r][c].setText("🚩");
            cellButtons[r][c].setForeground(TEXT_WHITE);
            flagsLeft--;
        }
        minesLabel.setText(String.format("%02d", flagsLeft));
    }

    private int countAdjacentMines(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = r + dr;
                int nc = c + dc;
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    if (mineGrid[nr][nc]) count++;
                }
            }
        }
        return count;
    }

    private boolean checkWin() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!mineGrid[r][c] && !revealed[r][c]) return false;
            }
        }
        return true;
    }

    private void triggerGameOver(boolean win, int hitR, int hitC) {
        gameOver = true;
        timer.stop();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mineGrid[r][c]) {
                    if (!win) {
                        cellButtons[r][c].setText("💣");
                        cellButtons[r][c].setBackground(new Color(60, 20, 20));
                        cellButtons[r][c].setForeground(TEXT_WHITE);
                    }
                } else if (flagged[r][c]) {
                    cellButtons[r][c].setText("✕");
                    cellButtons[r][c].setForeground(TEXT_DIM);
                }
            }
        }

        if (!win && hitR >= 0 && hitC >= 0) {
            cellButtons[hitR][hitC].setBackground(TEXT_WHITE);
            cellButtons[hitR][hitC].setForeground(BG_DARK);
        }

        String msg = win 
            ? "Parabéns! Você venceu em " + seconds + "s!\nDeseja jogar novamente?" 
            : "Você atingiu uma mina!\nDeseja tentar novamente?";

        int option = JOptionPane.showConfirmDialog(
            this, 
            msg, 
            win ? "VITÓRIA!" : "DERROTA", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            startNewGame(currentLevel);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CampoMinado().setVisible(true);
        });
    }
}