import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

public class UI extends JFrame {
    public FileManager fm = new FileManager();
    public DiskManager dm = new DiskManager();
    String defLayoutSet = "shrink, grow, wmin 100, span 2";
    String defLayoutSetHalf = "shrink, grow, wmin 50";
    Font font = new Font("Arial", Font.PLAIN, 11);
    Settings settings = new Settings();
    Preferences preferences = new Preferences();
    String version ="v2.1.0";
    ArrayList<Disk> disks = new ArrayList<>();
    String filterFirm = "";
    String filterModel = "";
    public UI(){
        super("NDisk");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try {
            URL iconURL = UI.class.getResource("icon.png");

            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image image = icon.getImage();
                setIconImage(image);
            }
        } catch ( Exception exception){
            //No icons for you
        }
        try{
            preferences = fm.readPreferences();
            settings = fm.readSettings(preferences);
            disks.addAll(settings.disks);
            font = new Font("Arial", Font.PLAIN, preferences.fontSize);
            updateTheme();
        }catch (Exception exception){
            //settings failed
        }
//        new Thread(() -> {
//            while(true) {
//                try {
//                    System.out.println(fm.tasks);
//                    if (fm.tasks == 0) {
//                        fm.tasksL.setText("Все задачи выполнены!");
//                    } else {
//                        switch ((int) (System.currentTimeMillis()/1000 % 6)) {
//                            case 0 -> fm.tasksL.setText("Выполняется задач: " + fm.tasks + "    ");
//                            case 1 -> fm.tasksL.setText("Выполняется задач: " + fm.tasks + " .  ");
//                            case 2 -> fm.tasksL.setText("Выполняется задач: " + fm.tasks + " .. ");
//                            case 3 -> fm.tasksL.setText("Выполняется задач: " + fm.tasks + " ...");
//                            case 4 -> fm.tasksL.setText("Выполняется задач: " + fm.tasks + "  ..");
//                            case 5 -> fm.tasksL.setText("Выполняется задач: " + fm.tasks + "   .");
//                        }
//                        System.out.println((int) (System.currentTimeMillis()/1000 % 6));
//                    }
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();
        main();
    }
    private void setFont(Container container, Font font) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            try{
                component.setFont(font);
            }catch (Exception e){
                //not using fonts
            }
            if (component instanceof Container) {
                // Рекурсивный вызов для вложенных контейнеров
                setFont((Container) component, font);
            }
        }
    }
    public void main(){
        clear();
        JButton getDisk = new JButton("Найти диск");
        getDisk.addActionListener(e -> getDisk());
        add(getDisk, defLayoutSet);
        JButton addDisk = new JButton("Добавить диск");
        addDisk.addActionListener(e -> addDisk());
        add(addDisk, defLayoutSet);
        JButton settingsB = new JButton("Настройки");
        settingsB.addActionListener(e -> settings());
        add(settingsB, defLayoutSet);
        ender();
    }
    public void getDisk(){
        clear();
        fm.checkSettings(settings, preferences);
        JLabel modelsLa = new JLabel("Модель");
        JTextField modelsT = new JTextField(30);
        add(modelsLa, defLayoutSet);
        add(modelsT, defLayoutSet+", wrap");
        JLabel firmL = new JLabel("Производитель");
        JTextField firmT = new JTextField(30);
        JLabel firmA = new JLabel("-");
        add(firmL, defLayoutSet);
        add(firmT, defLayoutSet);
        add(firmA, defLayoutSet+", wrap");
        DefaultListModel<String> modelsM = new DefaultListModel<>();
        JList<String> modelsL = new JList<>(modelsM);
        updateDisks();
        fillList(modelsM);
        add(modelsL, defLayoutSet+", span 6, wrap");
        firmT.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                String c;
                if(firmT.getText().length()!=0){
                    if(dm.filterDisks(settings.disks, firmT.getText(), DiskManager.IDEAL_INCORPORATION, DiskManager.BY_FIRM).size()!=0){
                        c="+";
                    }else{
                        c="x";
                    }
                }else{
                    c="-";
                }
                if(!c.equals(firmA.getText())){
                    if(c.equals("+")) {
                        filterFirm = firmT.getText();
                    }else{
                        filterFirm = "";
                    }
                    updateDisks();
                    fillList(modelsM);
                }
                firmA.setText(c);
            }
        });
        modelsT.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                filterModel = modelsT.getText();
                updateDisks();
                fillList(modelsM);
            }
        });
        JButton open = new JButton("Открыть");
        open.setEnabled(false);
        modelsL.addListSelectionListener(e -> open.setEnabled(modelsL.getSelectedIndex()!=-1));
        open.addActionListener(e -> fm.openFolder(fm.selectSubFolder(fm.getFolders(new File(preferences.workingFolder)),disks.get(modelsL.getSelectedIndex()).folder)));
        JButton back = new JButton("Назад");
        back.addActionListener(e -> main());
        add(open, defLayoutSet);
        add(new JLabel(), defLayoutSet);
        add(back, defLayoutSet);
        ender();
    }
    public void addDisk(){
        clear();
        JLabel diskL = new JLabel("Расположение диска");
        JTextField diskT = new JTextField(30);
        diskT.setEditable(false);
        JFileChooser diskF = new JFileChooser();
        diskF.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        diskF.setAcceptAllFileFilterUsed(false);
        JButton diskB = new JButton("...");
        diskB.addActionListener(e ->{
            int returnVal = diskF.showOpenDialog(UI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = diskF.getSelectedFile();
                diskT.setText(selectedFolder.getAbsolutePath());
            }
        });
        add(diskL, defLayoutSet);
        add(diskT, defLayoutSet);
        add(diskB, defLayoutSet+", wrap");
        JLabel firmL = new JLabel("Производитель");
        JTextField firmT = new JTextField();
        add(firmL, defLayoutSet);
        add(firmT, defLayoutSet+", wrap");
        JLabel modelL = new JLabel("Модель");
        JTextField modelT = new JTextField();
        add(modelL, defLayoutSet);
        add(modelT, defLayoutSet+", wrap");
        JButton confirm = new JButton("Скопировать");
        confirm.addActionListener(e -> {
            System.out.println(diskT.getText().length());
            if(diskT.getText().length()==0){
                JOptionPane.showMessageDialog(null, "Выберите папку с диском и попробуйте ещё раз");
                return;
            }
            if(modelT.getText().length()==0){
                JOptionPane.showMessageDialog(null, "Введите модель на диске и попробуйте ещё раз");
                return;
            }
            try {
                if(fm.selectSubFolder(fm.getFolders(new File(preferences.workingFolder)), firmT.getText()+"_"+modelT.getText())!=null){
                    JOptionPane.showMessageDialog(null, "Папка для такой модели уже существует");
                    return;
                }
                fm.copyDirectory(this, Path.of(diskT.getText()), Path.of(preferences.workingFolder).resolve( firmT.getText() + "_" + modelT.getText()));
                settings.disks.add(new Disk(firmT.getText()+"_"+modelT.getText(), firmT.getText(), modelT.getText()));
                fm.writeSettings(settings, preferences);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Не удалось скопировать папку");
            }

        });
        add(confirm, defLayoutSet);
        add(new JLabel(""), defLayoutSet);
        JButton back = new JButton("Назад");
        back.addActionListener(e -> main());
        add(back, defLayoutSet+", wrap");
        for (JLabel task: fm.tasks){
            add(task, defLayoutSet+", span 6, wrap");
        }
        ender();
    }
    public void fillList(DefaultListModel<String> model){
        model.clear();
        for (Disk disk: disks) {
            model.addElement(disk.model);
        }
        pack();
    }
    public void updateDisks(){
        disks = settings.disks;
        if(filterFirm.length()!=0){
            disks = dm.filterDisks(disks, filterFirm, DiskManager.IDEAL_INCORPORATION, DiskManager.BY_FIRM);
        }
        if(filterModel.length()!=0){
            disks = dm.filterDisks(disks, filterModel, DiskManager.BEST_INCORPORATION, DiskManager.BY_MODEL);
        }
    }
    public void settings(){
        clear();
        JLabel fontL = new JLabel("Размер шрифта (по умолчанию 20)");
        JTextField fontT = new JTextField("0");
        fontT.setText(String.valueOf(preferences.fontSize));
        JButton fontB = new JButton("Подтвердить");
        fontB.addActionListener(e -> {
            if(Objects.equals(fontT.getText(), "")){
                JOptionPane.showMessageDialog(null, "Напишите размер шрифта в строку и подтвердите");
                return;
            }
            if(Integer.parseInt(fontT.getText())<10){
                JOptionPane.showMessageDialog(null, "Минимальный допустимый размер шрифта - 10");
                return;
            }
            if(Integer.parseInt(fontT.getText())>50){
                JOptionPane.showMessageDialog(null, "Максимальный допустимый размер шрифта - 50");
                return;
            }
            preferences.fontSize = Integer.parseInt(fontT.getText());
            font = new Font("Arial", Font.PLAIN, preferences.fontSize);
            try {
                fm.writePreferences(preferences);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Не удалось записать настройки");
            }
            settings();
        });
        add(fontL, defLayoutSet);
        add(fontT, defLayoutSet);
        add(fontB, defLayoutSet+", wrap");
        JLabel themeL = new JLabel("Тема");
        JComboBox<String> themeC = new JComboBox<>();
        themeC.addItem("Светлая");
        themeC.addItem("Серая");
        themeC.addItem("Тёмная");
        switch (preferences.theme) {
            case "Светлая" ->themeC.setSelectedIndex(0);
            case "Серая" -> themeC.setSelectedIndex(1);
            case "Тёмная" -> themeC.setSelectedIndex(2);
        }
            themeC.addActionListener(e ->{
            try {
                preferences.theme = Objects.requireNonNull(themeC.getSelectedItem()).toString();
                fm.writePreferences(preferences);
                if(!updateTheme()){
                    throw new Exception();
                }
                settings();
            }catch (Exception exception){
                JOptionPane.showMessageDialog(null, "Не удалось изменить тему");
            }
        });
        add(themeL, defLayoutSet);
        add(themeC, defLayoutSet+", span 4, wrap");
        JLabel folderL = new JLabel("Папка хранения дисков");
        JTextField folderT = new JTextField(preferences.workingFolder);
        folderT.setEditable(false);
        JFileChooser folderF = new JFileChooser();
        folderF.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderF.setAcceptAllFileFilterUsed(false);
        JButton folderB = new JButton("...");
        folderB.addActionListener(e ->{
            int returnVal = folderF.showOpenDialog(UI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = folderF.getSelectedFile();
                folderT.setText(selectedFolder.getAbsolutePath());
                preferences.workingFolder=selectedFolder.getAbsolutePath();
                try {
                    fm.writePreferences(preferences);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Не удалось записать настройки");
                }
            }
        });
        add(folderL, defLayoutSet);
        add(folderT, defLayoutSet);
        add(folderB, defLayoutSet+", wrap");
        JButton dev=new JButton("Developed by Morozov "+ version);
        dev.setFocusPainted(false);
        dev.setBorderPainted(false);
        dev.setContentAreaFilled(false);
        dev.setMargin(new Insets(0,0,0,0));
        dev.setOpaque(false);
        dev.addActionListener(e -> {
            List<String> text = Arrays.asList(dev.getText().split(""));
            Collections.shuffle(text);
            StringBuilder txet = new StringBuilder();
            for(String letter:text){
                txet.append(letter);
            }
            dev.setText(txet.toString());
        });
        JButton back = new JButton("Назад");
        back.addActionListener(e -> main());
        add(back, defLayoutSet);
        add(dev, defLayoutSet);
        ender();
        dev.setFont(new Font("Arial", Font.PLAIN, (int) (preferences.fontSize*0.7)));
    }
    public boolean updateTheme(){
        try {
            switch (preferences.theme) {
                case "Тёмная" -> {
                    UIManager.setLookAndFeel(new FlatMacDarkLaf());
                    getContentPane().setBackground(new Color(25, 25, 25));
                    UIManager.put("Button.background",new Color(48, 48, 48));
//                    UIManager.put("ComboBox.buttonBackground",new Color(48, 48, 48));
                    UIManager.put("ComboBox.background",new Color(48, 48, 48));
                }
                case "Серая" -> {
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                    getContentPane().setBackground(new Color(40, 40, 40));
                    UIManager.put("Button.background",new Color(78, 80, 82));
                    UIManager.put("ComboBox.background",new Color(78, 80, 82));
                }
                case "Светлая" -> {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    getContentPane().setBackground(new Color(238, 238, 238));
                    UIManager.put("Button.background",new Color(245, 245, 245));
                    UIManager.put("ComboBox.background",new Color(245, 245, 245));
                }
            }
        }catch (Exception exception){
            return false;
        }
        return true;
    }
    public void clear(){
        getContentPane().removeAll();
        setLayout(new MigLayout("","[grow, fill]rel[grow, fill]rel[grow, fill]rel[grow, fill]rel[grow, fill]"));
    }
    public void ender(){
        setFont(this, font);
        pack();
        validate();
        setVisible(true);
    }
}
