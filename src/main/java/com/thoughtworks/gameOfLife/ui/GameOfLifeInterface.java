package com.thoughtworks.gameOfLife.ui;

import com.thoughtworks.gameOfLife.models.CellMatrix;
import com.thoughtworks.gameOfLife.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

public class GameOfLifeInterface extends JFrame {
    // 游戏状态
    private volatile boolean running = false;
    // 默认动画间隔（默认时长为200ms）
    private final static int DEFAULT_DURATION = 200;
    private int duration = DEFAULT_DURATION;
    // 控件
    private JPanel controlPanel;
    private JPanel cellPanel;
    private JButton startGameButton;
    private JButton chooseFileButton;
    private JButton pauseButton;
    private JButton randomInitButton;
    private JTextField durationTextField;
    private JLabel durationLabel;
    private JComboBox colorComboBox;
    private JLabel colorComboBoxLabel;
    // 网格
    private JTextField[][] jTextFieldMatrix;
    // 配色
    private final static Color DEFAULT_LIVECOLOR = Color.BLACK;
    private final static Color DEFAULT_DEADCOLOR = Color.WHITE;
    private volatile Color liveColor = DEFAULT_LIVECOLOR;
    private volatile Color deadColor = DEFAULT_DEADCOLOR;
    private final static String WHITE_BLACK = "黑白";
    private final static String RED_BLADK = "红黑";
    private final static String YELLOW_BLUE = "黄蓝";
    // cellMatrix
    private volatile CellMatrix cellMatrix;

    public GameOfLifeInterface() throws HeadlessException {
        chooseFileButton = new JButton("选择文件");
        chooseFileButton.setPreferredSize(new Dimension(200, 30));
        chooseFileButton.addActionListener(new ChooseFileActionListener());

        startGameButton = new JButton("开始游戏");
        startGameButton.setPreferredSize(new Dimension(200, 30));
        startGameButton.addActionListener(new StartGameActionListener());

        pauseButton = new JButton("暂停游戏");
        pauseButton.setPreferredSize(new Dimension(200, 30));
        pauseButton.addActionListener(new PauseGameActionListener());

        durationTextField = new JTextField();
        durationTextField.setPreferredSize(new Dimension(200, 30));

        durationLabel = new JLabel("间隔时间：");

        randomInitButton = new JButton("随机初始化");
        randomInitButton.setPreferredSize(new Dimension(200, 30));
        randomInitButton.addActionListener(new RandomInitActionListener());

        colorComboBoxLabel = new JLabel("更改配色：");

        colorComboBox=new JComboBox();
        colorComboBox.addItem(WHITE_BLACK);
        colorComboBox.addItem(RED_BLADK);
        colorComboBox.addItem(YELLOW_BLUE);
        colorComboBox.addActionListener(new ChangeColorActionListener());
        colorComboBox.setPreferredSize(new Dimension(100, 30));

        controlPanel = new JPanel();
        controlPanel.add(chooseFileButton);
        controlPanel.add(randomInitButton);
        controlPanel.add(durationLabel);
        controlPanel.add(durationTextField);
        controlPanel.add(startGameButton);
        controlPanel.add(pauseButton);
        controlPanel.add(colorComboBoxLabel);
        controlPanel.add(colorComboBox);

        getContentPane().add("North", controlPanel);
        this.setSize(1400, 1000);
    }

    /**
     * 使用cellMatrix创建cellPanel布局
     */
    private void setCellPanel() {
        int rows = cellMatrix.getHeight();
        int cols = cellMatrix.getWidth();
        cellPanel = new JPanel();
        cellPanel.setLayout(new GridLayout(rows, cols));

        int[][] matrix = cellMatrix.getMatrix();
        jTextFieldMatrix = new JTextField[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JTextField jTextField = new JTextField();
                if(matrix[i][j]==1){
                    jTextField.setBackground(liveColor);
                }else{
                    jTextField.setBackground(deadColor);
                }
                jTextFieldMatrix[i][j] = jTextField;
                cellPanel.add(jTextField);
            }
        }
        this.add(cellPanel);
        cellPanel.updateUI();
    }

    /**
     * 显示cellPanel的更新
     */
    private void updateCellPanel(){
        cellMatrix.transform();
        int rows = cellMatrix.getHeight();
        int cols = cellMatrix.getWidth();
        int[][] matrix = cellMatrix.getMatrix();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == 1) {
                    jTextFieldMatrix[i][j].setBackground(liveColor);
                } else {
                    jTextFieldMatrix[i][j].setBackground(deadColor);
                }
            }
        }
    }

    /**
     * 选择文件
     */
    private class ChooseFileActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 暂定游戏/游戏未开始
            running = false;
            JFileChooser fileChooser = new JFileChooser(".");
            fileChooser.setDialogTitle("请选择初始配置文件");
            int returnVal = fileChooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                //读取文件，初始化cellMatrix
                String filepath = fileChooser.getSelectedFile().getPath();
                cellMatrix = FileUtils.readFileInitCellMatrix(filepath);
                //初始化cellPanel
                setCellPanel();
            }
        }
    }

    /**
     * 随机初始化一个状态
     */
    private class RandomInitActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            // 暂定游戏/游戏未开始
            running = false;
            // 初始化随机矩阵
            cellMatrix = FileUtils.randomInitMatrix();
            // 初始化cellPanel
            setCellPanel();
        }
    }

    /**
     * 开始游戏
     */
    private class StartGameActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!running) {
                // 获取设定的时间
                try {
                    duration = Integer.parseInt(durationTextField.getText().trim());
                } catch (NumberFormatException e1) {
                    duration = DEFAULT_DURATION;
                }
                new Thread(new GameControlTask()).start();
                running = true;
            }
        }
    }

    /**
     * 暂停游戏
     */
    private class PauseGameActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            if(running){
                running = false;
            }
        }
    }

    /**
     * 更换配色
     */
    private class ChangeColorActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(running){
                running = false; // 更改配色时必须暂停运行
            }
            String s = (String) colorComboBox.getSelectedItem();
            switch (s) {
                case WHITE_BLACK:
                    liveColor = Color.BLACK;
                    deadColor = Color.WHITE;
                    break;
                case RED_BLADK:
                    liveColor = Color.RED;
                    deadColor = Color.BLACK;
                    break;
                case YELLOW_BLUE:
                    liveColor = Color.YELLOW;
                    deadColor = Color.BLUE;
                    break;
                default:
                    liveColor = Color.BLACK;
                    deadColor = Color.WHITE;
                    break;
            }
        }
    }


    /**
     * 运行游戏
     */
    private class GameControlTask implements Runnable {
        @Override
        public void run() {
            while (running) {
                setCellPanel();
                updateCellPanel();
                try {
                    TimeUnit.MILLISECONDS.sleep(duration);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
