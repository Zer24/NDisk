import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

public class FileManager {
    int tasks = 0;
    JLabel tasksL = new JLabel("Выполняется задач: "+tasks);
    public File getCurFolder(){
        return new File(".");
    }
    public ArrayList<File> getFolders(File parentFolder){
        File[] filesM = parentFolder.listFiles();
        if (filesM != null) {
            ArrayList<File> filesA = new ArrayList<>();
            for (File file: filesM){
                if (file.isDirectory()) {
                    filesA.add(file);
                }
            }
            return filesA;
        } else {
            System.err.println("Не удалось получить список файлов и папок.  Возможно, нет прав доступа.");
            return null;
        }
    }
    public File selectSubFolder(ArrayList<File> folders, String name){
        name = name.toLowerCase();
        for(File folder: folders){
            if(folder.getName().toLowerCase().equals(name)){
                return folder;
            }
        }
        return null;
    }

    public File createFolder(File parentFolder, String name){
        System.out.println("Creating folder "+name+" at folder "+parentFolder.getName());
        File folder = new File(parentFolder, name);
        if(!folder.isDirectory()) {
            folder.mkdir();
        }
        return folder;
    }
    public boolean deleteFolder(File folder){
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                if (!deleteFolder(file)) {
                    return false;
                }
            } else {
                if (!file.delete()) {
                    return false;
                }
            }
        }
        return folder.delete();
    }
    public void openFolder(File folder){
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(folder);
                System.out.println("Папка открыта в проводнике");
            } else {
                JOptionPane.showMessageDialog(null, "Операция открытия папки не поддерживается на данной платформе.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при открытии папки: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    public Settings readSettings() throws IOException {
        Settings settings = new Settings();
        File file = new File("settings.ini");
        if(!file.exists()){
            if(!file.createNewFile()){
                JOptionPane.showMessageDialog(null, "Не удалось создать файл настроек");
            }
            writeSettings(new Settings());
        }
        BufferedReader br = new BufferedReader(new FileReader("settings.ini"));
        settings.fontSize = Integer.parseInt(br.readLine());
        settings.theme = br.readLine();
        settings.disks.clear();
        String line = "";
        while((line = br.readLine())!=null){
            settings.disks.add(new Disk(line.split(", ")[0], line.split(", ")[1], line.split(", ")[2]));
        }
        System.out.println("READSETTINGS FOUND "+settings.disks.size());
        br.close();
        return settings;
    }
    public void writeSettings(Settings settings) throws IOException {
        int badFolders = checkSettings(settings);
        if(badFolders!=0){
            System.out.println("Found and deleted "+badFolders+" bad folders");
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter("settings.ini"));
        bw.write(settings.fontSize+"\n"+settings.theme+"\n");
        for (Disk disk:settings.disks){
            bw.write(disk.folder+", "+disk.firm+", "+disk.model+"\n");
        }
        bw.close();
    }
    public void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation) throws IOException {
        new Thread(() -> {
            try {
                tasks++;
                JOptionPane.showMessageDialog(null, "Начато копирования содержимого папки");
                long time = System.currentTimeMillis()/100;
                File sourceDirectory = new File(sourceDirectoryLocation);
                File destinationDirectory = new File(destinationDirectoryLocation);
                FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
                double difTime = (double)(System.currentTimeMillis()/100-time)/10;
                JOptionPane.showMessageDialog(null, "Копирование папки завершено успешно за "+difTime+" секунд");
                tasks--;
            } catch (IOException e) {
                tasks--;
                throw new RuntimeException(e);
            }
        }).start();
    }
    public int checkSettings(Settings settings){
        int bad = 0;
        for (int i = 0; i < settings.disks.size(); i++) {
            if(!new File(settings.disks.get(i).folder).exists()){
                settings.disks.remove(i);
                bad++;
                i--;
            }
        }
        return bad;
    }
}
