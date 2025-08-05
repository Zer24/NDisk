import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class FileManager {
    ArrayList<JLabel> tasks = new ArrayList<>();
    boolean debug;
    public FileManager(boolean debug){
        this.debug=debug;
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
    public void openFolder(File folder){
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(folder);
                if(debug)System.out.println("Папка открыта в проводнике");
            } else {
                JOptionPane.showMessageDialog(null, "Операция открытия папки не поддерживается на данной платформе.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при открытии папки: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    public Settings readSettings(Preferences preferences) throws IOException {
        Settings settings = new Settings();
        Path path = Paths.get(preferences.workingFolder, "settings.ini");
        if(!Files.exists(path)){
            Files.createFile(path);
            writeSettings(new Settings(), preferences);
        }
        BufferedReader br = Files.newBufferedReader(path);
        settings.disks.clear();
        String line;
        while((line = br.readLine())!=null){
            settings.disks.add(new Disk(line.split(", ")[0], line.split(", ")[1], line.split(", ")[2]));
        }
        if(debug)System.out.println("READSETTINGS FOUND "+settings.disks.size());
        br.close();
        return settings;
    }
    public Preferences readPreferences() throws IOException {
        Preferences preferences = new Preferences();
        File file = new File("preferences.ini");
        if(!file.exists()){
            if(!file.createNewFile()){
                JOptionPane.showMessageDialog(null, "Не удалось создать файл настроек");
            }
            writePreferences(new Preferences());
        }
        BufferedReader br = new BufferedReader(new FileReader("preferences.ini"));
        preferences.fontSize = Integer.parseInt(br.readLine());
        preferences.theme = br.readLine();
        preferences.workingFolder = br.readLine();
        br.close();
        return preferences;
    }
    public void writeSettings(Settings settings, Preferences preferences) throws IOException {
        int badFolders = checkSettings(settings, preferences);
        if(badFolders!=0){
            if(debug)System.out.println("Found and deleted "+badFolders+" bad folders");
        }
        BufferedWriter bw = Files.newBufferedWriter(Paths.get(preferences.workingFolder, "settings.ini"));
        for (Disk disk:settings.disks){
            bw.write(disk.folder+", "+disk.firm+", "+disk.model+"\n");
        }
        bw.close();
    }
    public void writePreferences(Preferences preferences) throws IOException {
        BufferedWriter bw = Files.newBufferedWriter(Paths.get("preferences.ini"));
        bw.write(preferences.fontSize+"\n"+preferences.theme+"\n"+preferences.workingFolder+"\n");
        bw.close();
    }
    public void copyDirectory(UI ui, Path sourceDir, Path targetDir) throws IOException {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        AtomicLong totalFiles = new AtomicLong(0);
        AtomicLong copiedFiles = new AtomicLong(0);

        long time = System.currentTimeMillis();
        JLabel task = new JLabel("");
        tasks.add(task);
        ui.add(task, ui.defLayoutSet+", span 6, wrap");
        new Thread(() -> {
            try {
                task.setText("Выполняется подсчёт копируемых файлов...");
                ui.ender();
                Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        totalFiles.incrementAndGet();
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }
                });
                // Копируем файлы и папки (рекурсивно)
                Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path target = targetDir.resolve(sourceDir.relativize(dir));
                        if (!Files.exists(target)) {
                            Files.createDirectories(target);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path target = targetDir.resolve(sourceDir.relativize(file));
                        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                        copiedFiles.incrementAndGet();
                        task.setText(copiedFiles.get()*100 / totalFiles.get()+"% "+copiedFiles.get()+"/"+ totalFiles.get());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JOptionPane.showMessageDialog(null, "Копирование было завершено за "+(double)((System.currentTimeMillis()-time)/100)/10 +" секунд");
            ui.remove(task);
            tasks.remove(task);
        }).start();
    }
    public int checkSettings(Settings settings, Preferences preferences){
        int bad = 0;
        for (int i = 0; i < settings.disks.size(); i++) {
            if(!Files.exists(Paths.get(preferences.workingFolder, settings.disks.get(i).folder))){
                settings.disks.remove(i);
                bad++;
                i--;
            }
        }
        return bad;
    }
}
