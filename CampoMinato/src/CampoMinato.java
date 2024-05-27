import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CampoMinato extends JFrame {
    private static final int TILE_SIZE = 70;
    private static final int ICON_SIZE = TILE_SIZE - 20; // Smaller icon size
    private static final int ROWS = 8;
    private static final int COLS = 8;
    private static final int MINES = 10;
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Font TITLE_FONT = new Font("Helvetica", Font.BOLD, 24);
    private static final String ICON_PATH = "/cm.png";
    private static final String FLAG_PATH = "/flag.png";

    private int flagsPlaced = 0;
    private final int boardWidth = COLS * TILE_SIZE;
    private final int boardHeight = ROWS * TILE_SIZE;

    private JButton[][] buttons;
    private boolean[][] mines;
    private boolean[][] revealed;
    private JLabel statusLabel;
    private boolean firstMove;
    private final Image icon;
    private final ImageIcon flagIcon;

    public CampoMinato() {
        icon = new ImageIcon(getClass().getResource(ICON_PATH)).getImage();
        flagIcon = new ImageIcon(new ImageIcon(getClass().getResource(FLAG_PATH)).getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));

        setTitle("Campo Minato");
        setIconImage(icon);
        setSize(boardWidth, boardHeight + 100); // Adjusted height to fit the title and status label
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        JLabel titleLabel = new JLabel("Campo Minato", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        add(titleLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(ROWS, COLS));
        buttons = new JButton[ROWS][COLS];
        mines = new boolean[ROWS][COLS];
        revealed = new boolean[ROWS][COLS];

        statusLabel = new JLabel("Benvenuto nel Campo Minato!");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        for (int i = 0; i < ROWS; i++) {
            for (int k = 0; k < COLS; k++) {
                buttons[i][k] = new JButton();
                buttons[i][k].setFont(BUTTON_FONT);
                buttons[i][k].setBackground(Color.LIGHT_GRAY);
                buttons[i][k].setFocusPainted(false);
                buttons[i][k].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                int row = i;
                int col = k;
                buttons[i][k].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            placeFlag(row, col);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            handleClick(row, col);
                        }
                    }
                });
                gridPanel.add(buttons[i][k]);
            }
        }

        add(gridPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.addActionListener(e -> restartGame());
        add(restartButton, BorderLayout.SOUTH);

        restartGame();
        setVisible(true);
    }

    private void generateMines(int initialRow, int initialCol) {
        int count = 0;
        while (count < MINES) {
            int row = (int) (Math.random() * ROWS);
            int col = (int) (Math.random() * COLS);
            if (!mines[row][col] && !(row == initialRow && col == initialCol)) {
                mines[row][col] = true;
                count++;
            }
        }
    }

    private void handleClick(int row, int col) {
        if (firstMove) {
            generateMines(row, col);
            firstMove = false;
        }

        if (mines[row][col]) {
            revealAllCells();
            statusLabel.setText("Hai perso! Clicca su 'Restart' per giocare di nuovo.");
            disableAllButtons();
        } else {
            revealCell(row, col);
            if (checkWin()) {
                statusLabel.setText("Hai vinto! Clicca su 'Restart' per giocare di nuovo.");
                disableAllButtons();
            }
        }
    }

    private void revealAllCells() {
        for (int i = 0; i < ROWS; i++) {
            for (int k = 0; k < COLS; k++) {
                if (mines[i][k]) {
                    buttons[i][k].setText("*");
                    buttons[i][k].setBackground(Color.RED);
                } else {
                    revealCell(i, k);
                }
            }
        }
    }

    private void updateFlagCount() {
        statusLabel.setText("Bandiere piazzate: " + flagsPlaced);
    }

    private void placeFlag(int row, int col) {
        if (!revealed[row][col]) {
            if (buttons[row][col].getIcon() == flagIcon) {
                buttons[row][col].setIcon(null);
                flagsPlaced--;
            } else if (flagsPlaced < MINES) {
                buttons[row][col].setIcon(flagIcon);
                flagsPlaced++;
            }
            updateFlagCount();
        }
    }

    private void revealCell(int row, int col) {
        if (!revealed[row][col]) {
            revealed[row][col] = true;
            buttons[row][col].setEnabled(false);
            buttons[row][col].setBackground(Color.WHITE);
            int nearbyMines = countNearbyMines(row, col);
            if (nearbyMines > 0) {
                buttons[row][col].setText(Integer.toString(nearbyMines));
            } else {
                buttons[row][col].setText("");
                for (int i = row - 1; i <= row + 1; i++) {
                    for (int k = col - 1; k <= col + 1; k++) {
                        if (i >= 0 && i < ROWS && k >= 0 && k < COLS && !(i == row && k == col)) {
                            revealCell(i, k);
                        }
                    }
                }
            }
        }
    }

    private int countNearbyMines(int row, int col) {
        int count = 0;
        for (int i = row - 1; i <= row + 1; i++) {
            for (int k = col - 1; k <= col + 1; k++) {
                if (i >= 0 && i < ROWS && k >= 0 && k < COLS && mines[i][k]) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean checkWin() {
        for (int i = 0; i < ROWS; i++) {
            for (int k = 0; k < COLS; k++) {
                if (!revealed[i][k] && !mines[i][k]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void disableAllButtons() {
        for (int i = 0; i < ROWS; i++) {
            for (int k = 0; k < COLS; k++) {
                buttons[i][k].setEnabled(false);
            }
        }
    }

    private void restartGame() {
        for (int i = 0; i < ROWS; i++) {
            for (int k = 0; k < COLS; k++) {
                buttons[i][k].setEnabled(true);
                buttons[i][k].setText("");
                buttons[i][k].setIcon(null);
                buttons[i][k].setBackground(Color.LIGHT_GRAY);
                mines[i][k] = false;
                revealed[i][k] = false;
            }
        }
        flagsPlaced = 0;
        firstMove = true;
        updateFlagCount();
        statusLabel.setText("Benvenuto nel Campo Minato!");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CampoMinato::new);
    }
}
