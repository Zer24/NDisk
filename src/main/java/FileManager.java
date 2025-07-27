import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

public class FileManager {
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

    public boolean createFolder(File parentFolder, String name){
        System.out.println("Creating folder "+name+" at folder "+parentFolder.getName());
        return new File(parentFolder, name).mkdir();
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
    public ArrayList<String> readSettings() throws IOException {
        ArrayList<String> settings = new ArrayList<>();
        File file = new File("settings.ini");
        if(!file.exists()){
            if(!file.createNewFile()){
                JOptionPane.showMessageDialog(null, "Не удалось создать файл настроек");
            }
            writeSettings(20, "Светлая");
        }
        BufferedReader br = new BufferedReader(new FileReader("settings.ini"));
        settings.add(br.readLine());
        settings.add(br.readLine());
        br.close();
        return settings;
    }
    public void writeSettings(int font, String theme) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("settings.ini"));
        bw.write(font+"\n"+theme+"\n");
        bw.close();
    }
}
