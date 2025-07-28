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
import java.util.List;
import java.util.*;

public class UI extends JFrame {
    public FileManager fm = new FileManager();
    String defLayoutSet = "shrink, grow, wmin 100";
    String defLayoutSetHalf = "shrink, grow, wmin 50";
    Font font = new Font("Arial", Font.PLAIN, 11);
    int fontSize = 11;
    String theme = "Серая";
    String version ="v1.0.1";
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
            fontSize = Integer.parseInt(fm.readSettings().get(0));
            font = new Font("Arial", Font.PLAIN, fontSize);
            theme=fm.readSettings().get(1);
            updateTheme();
        }catch (Exception exception){
            //settings failed
        }
        /*try{
            fontSize = Integer.parseInt(fm.readSettings().get(0));
            font = new Font("Arial", Font.PLAIN, fontSize);
            theme=fm.readSettings().get(1);
            updateTheme();
            UIDefaults defaults = UIManager.getDefaults();
            Enumeration<Object> keys = defaults.keys();

            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = defaults.get(key);
                System.out.println(key + " = " + value);
            }
        }catch (Exception exception){

        }*/
        /*        try {
            // Попробуйте Nimbus
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("GTK+".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            // Если Nimbus недоступен, используйте системный LAF (Windows, GTK и т.д.)
            if (UIManager.getLookAndFeel().getName().equals("Metal")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace(); // Обработка ошибок при загрузке LAF
        }*/
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
        JLabel firmL = new JLabel("Производитель");
        add(firmL,  defLayoutSet+ ", span 2");
        JLabel modelL = new JLabel("Модель");
        add(modelL, defLayoutSet+ ", span 2");
        JButton settings = new JButton("Настройки");
        add(settings, defLayoutSet+ ", wrap, span 2 2");
        settings.addActionListener(e -> settings());
        JTextField firmT = new JTextField();
        add(firmT,  defLayoutSet+ ", span 2");
        JTextField modelT = new JTextField();
        add(modelT, defLayoutSet+", span 2, wrap");
        DefaultListModel<String> firmM = new DefaultListModel<>();
        JList<String> firm = new JList<>(firmM);
        add(new JScrollPane(firm), defLayoutSet+", span 2");
        fillList(firmM, fm.getFolders(fm.getCurFolder()), "");
        DefaultListModel<String> modelM = new DefaultListModel<>();
        JList<String> model = new JList<>(modelM);
        model.setVisible(false);
        add(new JScrollPane(model), defLayoutSet+", span 2");
        firm.addListSelectionListener(e -> {
            modelT.setText("");
            modelM.clear();
            System.out.println("checking on "+firm.getSelectedValue());
            if(firm.getSelectedValue()==null){
                model.setVisible(false);
            }else {
                fillList(modelM, fm.getFolders(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firm.getSelectedValue())), "");
//            for (File folder:fm.getFolders(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firm.getSelectedValue().toString()))){
//                modelM.addElement(folder.getName());
//            }
                model.setVisible(modelM.getSize() != 0);
            }
            pack();
        });
        JButton confirm = new JButton("Открыть");
        add(confirm, defLayoutSet+", span 2, wrap");
        confirm.addActionListener(e -> {
            if(firm.getSelectedValue()==null){
                JOptionPane.showMessageDialog(null, "Необходимо выбрать производителя");
                return;
            }
            if(model.getSelectedValue()==null){
                JOptionPane.showMessageDialog(null, "Необходимо выбрать модель");
                return;
            }
            fm.openFolder(fm.selectSubFolder(fm.getFolders(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()),firm.getSelectedValue())),model.getSelectedValue()));
        });
        firmT.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                fillList(firmM, fm.getFolders(fm.getCurFolder()), firmT.getText());
            }
        });
        modelT.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if(firm.getSelectedValue()!=null) {
                    fillList(modelM, fm.getFolders(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firm.getSelectedValue())), modelT.getText());
                }
            }
        });
        JButton adderFirm = new JButton("Добавить");
        adderFirm.addActionListener(e -> {
            if(Objects.equals(firmT.getText(), "")){
                JOptionPane.showMessageDialog(null, "Сначала введите название производителя в поле \"Производитель\", и попробуйте ещё раз");
                return;
            }
            if(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firmT.getText())!=null){
                JOptionPane.showMessageDialog(null, "Такой производитель уже указан");
                return;
            }
            if(fm.createFolder(fm.getCurFolder(), firmT.getText())){
                JOptionPane.showMessageDialog(null, "Производитель был добавлен успешно");
                fillList(firmM, fm.getFolders(fm.getCurFolder()), firmT.getText());
                firm.setVisible(true);
            }else{
                JOptionPane.showMessageDialog(null, "Произошла неизвестная ошибка");
            }
        });
        add(adderFirm, defLayoutSetHalf);
        JButton removerFirm = new JButton("Удалить");
        removerFirm.addActionListener(e -> {
            if(firm.getSelectedValue()==null){
                JOptionPane.showMessageDialog(null, "Выберите производителя в списке и попробуйте ещё раз");
                return;
            }
            int confirmed = JOptionPane.showConfirmDialog(null, "Папка производителя и все файлы находящиеся в ней будут удалены, Вы действительно хотите продолжить?", "Удаление", JOptionPane.YES_NO_OPTION);
            if(confirmed==JOptionPane.YES_OPTION) {
                if (fm.deleteFolder(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firm.getSelectedValue()))) {
                    JOptionPane.showMessageDialog(null, "Производитель был удалён успешно");
                    fillList(firmM, fm.getFolders(fm.getCurFolder()), firmT.getText());
                    firm.setVisible(firmM.getSize()!=0);
                } else {
                    JOptionPane.showMessageDialog(null, "Произошла неизвестная ошибка");
                }
            }
        });
        add(removerFirm, defLayoutSetHalf);

        JButton adderModel = new JButton("Добавить");
        adderModel.addActionListener(e -> {
            if(firm.getSelectedValue()==null){
                JOptionPane.showMessageDialog(null, "Выберите производителя и попробуйте ещё раз");
                return;
            }
            if(Objects.equals(modelT.getText(), "")){
                JOptionPane.showMessageDialog(null, "Введите название модели в поле \"Модель\" и попробуйте ещё раз");
                return;
            }
            if(fm.selectSubFolder(fm.getFolders(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firm.getSelectedValue())), modelT.getText())!=null){
                JOptionPane.showMessageDialog(null, "Такая модель уже указана");
                return;
            }
            if(fm.createFolder(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firm.getSelectedValue()), modelT.getText())){
                JOptionPane.showMessageDialog(null, "Модель была добавлена успешно");
                fillList(modelM, fm.getFolders(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firm.getSelectedValue())), modelT.getText());
                model.setVisible(true);
            }else{
                JOptionPane.showMessageDialog(null, "Произошла неизвестная ошибка");
            }
        });
        add(adderModel, defLayoutSetHalf);
        JButton removerModel = new JButton("Удалить");
        removerModel.addActionListener(e -> {
            if(firm.getSelectedValue()==null){
                JOptionPane.showMessageDialog(null, "Выберите производителя и попробуйте ещё раз");
                return;
            }
            if(model.getSelectedValue()==null){
                JOptionPane.showMessageDialog(null, "Выберите модель в списке и попробуйте ещё раз");
                return;
            }
            int confirmed = JOptionPane.showConfirmDialog(null, "Папка модели и все файлы находящиеся в ней будут удалены, Вы действительно хотите продолжить?", "Удаление", JOptionPane.YES_NO_OPTION);
            if(confirmed==JOptionPane.YES_OPTION) {
                if (fm.deleteFolder(fm.selectSubFolder(fm.getFolders(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firm.getSelectedValue())), model.getSelectedValue()))) {
                    JOptionPane.showMessageDialog(null, "Модель была удалена успешно");
                    fillList(modelM, fm.getFolders(fm.selectSubFolder(fm.getFolders(fm.getCurFolder()), firm.getSelectedValue())), modelT.getText());
                    model.setVisible(modelM.getSize()!=0);
                } else {
                    JOptionPane.showMessageDialog(null, "Произошла неизвестная ошибка");
                }
            }
        });
        add(removerModel, defLayoutSetHalf);
        ender();
    }
    public void fillList(DefaultListModel<String> model, ArrayList<File> folders, String filter){
        filter=filter.toLowerCase();
        model.clear();
        for (File folder:folders) {
            if(folder.getName().toLowerCase().contains(filter)) {
                model.addElement(folder.getName());
            }
        }
    }
    public void settings(){
        clear();
        JLabel fontL = new JLabel("Размер шрифта (по умолчанию 20)");
        JTextField fontT = new JTextField("0");
        try {
            fontT.setText(String.valueOf(fm.readSettings().get(0)));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Произошла неизвестная ошибка");
        }
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
            fontSize = Integer.parseInt(fontT.getText());
            font = new Font("Arial", Font.PLAIN, fontSize);
            try {
                System.out.println("WRITING TO SETTINGS FONTSIZE: "+fontSize);
                fm.writeSettings(fontSize, theme);
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
        if(Objects.equals(theme, "Светлая")){
            themeC.setSelectedIndex(0);
        }
        if(Objects.equals(theme, "Серая")){
            themeC.setSelectedIndex(1);
        }
        if(Objects.equals(theme, "Тёмная")){
            themeC.setSelectedIndex(2);
        }

        themeC.addActionListener(e ->{
            try {
                theme = Objects.requireNonNull(themeC.getSelectedItem()).toString();
                fm.writeSettings(fontSize, theme);
                if(!updateTheme()){
                    throw new Exception();
                }
                settings();
            }catch (Exception exception){
                JOptionPane.showMessageDialog(null, "Не удалось изменить тему");
            }
        });
        add(themeL, defLayoutSet);
        add(themeC, defLayoutSet+", span 2, wrap");
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
        add(dev, defLayoutSet+", span 2");
        ender();
        dev.setFont(new Font("Arial", Font.PLAIN, (int) (fontSize*0.7)));
    }
    public boolean updateTheme(){
        try {
            switch (theme) {
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
