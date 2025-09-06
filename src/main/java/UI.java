import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    boolean debug = false;
    public FileManager fm = new FileManager(debug);
    public DBManager dbm = new DBManager(debug);
    String defLayoutSet = "shrink, grow, wmin 100, span 2";
    String defLayoutSetHalf = "shrink, grow, wmin 50";
    Font font = new Font("Arial", Font.PLAIN, 11);
    Preferences preferences = new Preferences();
    String version ="v3.0.0";
    ArrayList<Disk> disks = new ArrayList<>();
    String filterFirm = "";
    String filterModel = "";
    Image logoImage;
    ImageIcon logo;
    JPanel menu;
    public UI(){
        super("NDisk");
        setExtendedState(MAXIMIZED_BOTH);
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
        try {
            URL iconURL = UI.class.getResource("NDiskLogo.png");

            if (iconURL != null) {
                logoImage = new ImageIcon(iconURL).getImage();
                logo = scaleImage(2.0);
            }
        } catch ( Exception exception){
            //No logo for you
        }

        try{
            preferences = fm.readPreferences();
            dbm.setPath(preferences.workingFolder);
            disks.addAll(dbm.select("", ""));
            font = new Font("Arial", Font.PLAIN, preferences.fontSize);
            updateTheme();
        }catch (Exception exception){
            //no settings
        }
        pack();
        getDisk();
//        main();
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
//        fm.checkSettings(settings, preferences);
        JTextField modelsT = new JTextField(10);
        JTextField firmT = new JTextField(10);
//        add(new JLabel("Производитель"), defLayoutSet+", span 3");
//        add(new JLabel("Модель"), defLayoutSet+", span 3, wrap");
        add(firmT, defLayoutSet+", span 3");
        add(modelsT, defLayoutSet+", span 3, wrap");
        String[] columns = {"Производитель", "Модель"};
        DefaultTableModel modelsM = new DefaultTableModel(columns, 0);
        JTable modelsL = new JTable(modelsM);
        modelsL.setRowHeight(preferences.fontSize);
        modelsL.setShowGrid(true);
        updateDisks();
        fillList(modelsM);
        add(new JScrollPane(modelsL), defLayoutSet+", span 6, growy, wrap");
        firmT.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
//                firmM.removeAllElements();
//                firmM.addAll(dm.findUniqueFirms(dm.filterDisks(disks,firmT.getText(), DiskManager.IDEAL_INCORPORATION, DiskManager.BY_FIRM)));
                filterFirm = firmT.getText();
                updateDisks();
                fillList(modelsM);
            }
        });
//        firmC.addActionListener(e -> firmT.setText((String) firmC.getSelectedItem()));
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
        modelsL.getSelectionModel().addListSelectionListener(e -> open.setEnabled(modelsL.getSelectedRow()!=-1));
        open.addActionListener(e -> fm.openFolder(fm.selectSubFolder(fm.getFolders(new File(preferences.workingFolder)),disks.get(modelsL.getSelectedRow()).folder)));
        add(open, defLayoutSet);
//        pack();
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
            if(debug)System.out.println(diskT.getText().length());
            if(diskT.getText().length()==0){
                JOptionPane.showMessageDialog(null, "Выберите папку с диском и попробуйте ещё раз");
                return;
            }
            if(modelT.getText().length()==0){
                JOptionPane.showMessageDialog(null, "Введите модель на диске и попробуйте ещё раз");
                return;
            }
            try {
                if(dbm.exists(modelT.getText(), firmT.getText())){
                    JOptionPane.showMessageDialog(null, "Такая модель уже существует в базе");
                    return;
                }
                fm.copyDirectory(this, Path.of(diskT.getText()), Path.of(preferences.workingFolder).resolve( firmT.getText() + "_" + modelT.getText()));
                dbm.insert(firmT.getText()+"_"+modelT.getText(), firmT.getText(), modelT.getText());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Не удалось скопировать папку");
            }

        });
        add(confirm, defLayoutSet+", wrap");
        for (JLabel task: fm.tasks){
            add(task, defLayoutSet+", span 6, wrap");
        }
        ender();
    }
    public void fillList(DefaultTableModel model){
        if(debug)System.out.println("Started filling list with "+disks.size());
        model.setRowCount(0);
        for (Disk disk: disks) {
            if(debug)System.out.println(disk);
            model.addRow(new Object[]{disk.firm, disk.model});
        }
        if(debug)for (int i = 0; i < 200; i++) {model.addRow(new Object[]{"disk.firm", "disk.model"});}
//        pack();
    }
    public void updateDisks(){
        disks = dbm.select(filterModel, filterFirm);
    }
    public void settings(){
        clear();
        JLabel fontL = new JLabel("Размер шрифта");
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
        themeC.addItem("Тёмная");
        switch (preferences.theme) {
            case "Светлая" ->themeC.setSelectedIndex(0);
            case "Тёмная" -> themeC.setSelectedIndex(1);
        }
        themeC.addActionListener(e ->{
            try {
                preferences.theme = Objects.requireNonNull(themeC.getSelectedItem()).toString();
                switch (preferences.theme) {
                    case "Тёмная" -> {
                        preferences.colorBg1 = 30;
                        preferences.colorBg2 = 30;
                        preferences.colorBg3 = 30;
                        preferences.colorField1 = 40;
                        preferences.colorField2 = 40;
                        preferences.colorField3 = 40;
                        preferences.colorBtn1 = 80;
                        preferences.colorBtn2 = 120;
                        preferences.colorBtn3 = 255;
                        preferences.colorBtnDe1 = 60;
                        preferences.colorBtnDe2 = 100;
                        preferences.colorBtnDe3 = 200;
                        preferences.colorFont1 = 255;
                        preferences.colorFont2 = 255;
                        preferences.colorFont3 = 255;

//                    getContentPane().setBackground(new Color(25, 25, 25));
//                    UIManager.put("Button.background",new Color(48, 48, 48));
//                    UIManager.put("ComboBox.background",new Color(48, 48, 48));
                    }
                    case "Светлая" -> {
//                    UIManager.setLookAndFeel(new FlatLightLaf());
                        preferences.colorBg1 = 230;
                        preferences.colorBg2 = 230;
                        preferences.colorBg3 = 230;
                        preferences.colorField1 = 245;
                        preferences.colorField2 = 245;
                        preferences.colorField3 = 245;
                        preferences.colorBtn1 = 51;
                        preferences.colorBtn2 = 102;
                        preferences.colorBtn3 = 255;
                        preferences.colorBtnDe1 = 102;
                        preferences.colorBtnDe2 = 153;
                        preferences.colorBtnDe3 = 255;
                        preferences.colorFont1 = 0;
                        preferences.colorFont2 = 0;
                        preferences.colorFont3 = 0;
                    }
                }
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
            LookAndFeel l =  UIManager.getLookAndFeel();
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (UnsupportedLookAndFeelException ex) {
                System.out.println("wow");
            }
            int returnVal = folderF.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = folderF.getSelectedFile();
                folderT.setText(selectedFolder.getAbsolutePath());
                preferences.workingFolder=selectedFolder.getAbsolutePath();
                try {
                    fm.writePreferences(preferences);
                    dbm.setPath(preferences.workingFolder);
//                    settings = fm.readSettings(preferences);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Не удалось записать настройки");
                }
            }
            try {
                UIManager.setLookAndFeel(l);
            } catch (UnsupportedLookAndFeelException ex) {
                System.out.println("wow");
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
        add(dev, defLayoutSet+", span 6");
        ender();
        dev.setFont(new Font("Arial", Font.PLAIN, (int) (preferences.fontSize*0.7)));
    }
    public boolean updateTheme(){
        try {
            Color colorBg = new Color(preferences.colorBg1, preferences.colorBg2, preferences.colorBg3);
            Color colorBtn = new Color(preferences.colorBtn1, preferences.colorBtn2, preferences.colorBtn3);
            Color colorBtnDe = new Color(preferences.colorBtnDe1, preferences.colorBtnDe2, preferences.colorBtnDe3);
            Color colorFont = new Color(preferences.colorFont1, preferences.colorFont2, preferences.colorFont3);
            Color colorField = new Color(preferences.colorField1, preferences.colorField2, preferences.colorField3);
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            getContentPane().setBackground(colorBg);
//            menu.setBackground(new Color(colorBg1, colorBg2, colorBg3));
            UIManager.put("Button.background",colorBtn);
            UIManager.put("Button.disabledBackground",colorBtnDe);
//            UIManager.put("Button.foreground",colorFont);
//            UIManager.put("Button.disabledForeground",colorFont);
            UIManager.put("Button.disabledText",colorFont);
            UIManager.put("ComboBox.background",colorField);
//            UIManager.put("ComboBox.foreground",colorFont);
            UIManager.put("ComboBox.popupBackground", colorField);
//            UIManager.put("Label.foreground", colorFont);
//            UIManager.put("TextField.foreground", colorFont);
//            UIManager.put("TextField.caretForeground", colorFont);
            UIManager.put("TextField.background", colorField);
//            UIManager.put("Table.foreground", colorFont);
            UIManager.put("Table.background", colorField);
            UIManager.put("Table.gridColor", normalize(colorFont));
            UIManager.put("Table.selectionBackground", colorBtn);
            UIManager.put("Table.selectionInactiveBackground", colorBtnDe);
//            UIManager.put("TableHeader.foreground", colorFont);
            UIManager.put("TableHeader.background", colorBg);
            UIManager.put("ScrollBar.thumb", colorBtn);
            UIManager.put("ScrollBar.track", colorField);
            UIManager.put("Panel.background", colorBg);
            UIManager.put("FileChooser.listViewBackground", colorField);
            UIManager.put("FileChooser.background", colorBg);
            UIManager.put("FileChooser.background", colorBg);
            UIManager.put("RootPane.background", colorField);
            UIManager.put("ToolBar.background", colorBg);
            UIManager.put("OptionPane.background", colorBg);
//            UIManager.put("FileChooser.treeForeground", colorFont);
            UIDefaults defaults = UIManager.getDefaults();
            Enumeration<Object> keys = defaults.keys();

            if(debug)System.out.println("Keys for current Look and Feel:");
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                if(key.toString().toLowerCase().contains("foreground")){UIManager.put(key, colorFont);}
                if (debug && key.toString().startsWith("JOptionPane") && key.toString().toLowerCase().contains("")/* && key.toString().contains("")*/) {
//                    System.out.println("hello");
                    System.out.println(key + " " + defaults.get(key));
//                    UIManager.put(key, colorBg);
//                    new JFileChooser().showOpenDialog(this);
                }
            }
        }catch (Exception exception){
            return false;
        }
        return true;
    }
    public ImageIcon scaleImage(double times){
        return new ImageIcon(logoImage.getScaledInstance((int) (logoImage.getWidth(null)/times), (int) (logoImage.getHeight(null)/times), Image.SCALE_SMOOTH));
    }
    public void clear(){
        getContentPane().removeAll();
        setLayout(new MigLayout("","[grow, fill]rel[grow, fill]rel[grow, fill]rel[grow, fill]rel[grow, fill]"));
        menu = new JPanel();
        menu.setLayout(new MigLayout("", "[grow, fill]rel[grow, fill]rel[grow, fill]rel[grow, fill]rel[grow, fill]"));
        JLabel logoL = new JLabel(logo);
        menu.add(logoL,defLayoutSet);
        JButton getDisk = new JButton("Найти диск");
        getDisk.addActionListener(e -> getDisk());
        menu.add(getDisk, defLayoutSet);
        JButton addDisk = new JButton("Добавить диск");
        addDisk.addActionListener(e -> addDisk());
        menu.add(addDisk, defLayoutSet);
        JButton settingsB = new JButton("Настройки");
        settingsB.addActionListener(e -> settings());
        menu.add(settingsB, defLayoutSet);

        add(menu, defLayoutSet+", span 6, wrap");
    }
    public void ender(){
        setFont(this, font);
//        pack();
        setExtendedState(MAXIMIZED_BOTH);
        revalidate();
        repaint();
        setVisible(true);
    }
    public Color normalize(Color color){
        double mod = 0.4;
        if(color.getRed()+color.getGreen()+color.getBlue()>170){
            if(debug) System.out.println("Bright");
        }else{
            if (debug) System.out.println("Dark");
            mod*=-1;
        }
        int r = Math.max(0, Math.min(255, (int) Math.round(color.getRed() * (1 - mod))));
        int g = Math.max(0, Math.min(255, (int) Math.round(color.getGreen() * (1 - mod))));
        int b = Math.max(0, Math.min(255, (int) Math.round(color.getBlue() * (1 - mod))));
        if(debug) System.out.println(new Color(r,g,b));
        return new Color(r,g,b);
    }
}
