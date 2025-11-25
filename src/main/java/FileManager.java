import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        if(folder == null){
            JOptionPane.showMessageDialog(null, "Такой папки не существует! Обновите список");
        }
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
    public int deleteArray(ArrayList<Disk> disks, String workingFolder){
        if(debug)System.out.println(new File(workingFolder).toPath().resolve("rev_rev").toFile().getAbsolutePath());
        int amo=0;
        for (Disk disk: disks){
            if(deleteFolderRecursively(new File(workingFolder).toPath().resolve(disk.folder).toFile())){
                amo++;
            }
        }
        return amo;
    }
    private boolean deleteFolderRecursively(File file){
        if(file.isDirectory()){
            try {
                for (File inFile: Objects.requireNonNull(file.listFiles())){
                    if(!deleteFolderRecursively(inFile)){
                        return false;
                    }
                }
            }catch (NullPointerException nullPointerException){
                JOptionPane.showMessageDialog(null, "Указанная папка не существует!");
                return false;
            }
        }
        return file.delete();

    }
    public JPanel getTree(File folder){
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("insets 0, gap 0px 0px"));
        File[] filesArray = folder.listFiles();
        ArrayList<File> files = new ArrayList<>(List.of(filesArray));
        files.sort((o1, o2) -> o1.isDirectory() ? -1 : 1);
        for (File file: files){
            JPanel filePanel = new JPanel();
            filePanel.setLayout(new MigLayout());
            if(file.isDirectory()) {
                JButton opener = new JButton("+");
                opener.addActionListener(e -> {
                    opener.setEnabled(false);
                    opener.setBackground(new Color(40,40,40));
                    filePanel.add(getTree(file), "gap 0px 0px, span 2");
                    filePanel.revalidate();
                });
                filePanel.add(opener, "gap 0px 0px");
            }
            filePanel.add(new JLabel(file.getName(), JLabel.LEFT), "gap 0px 0px, wrap");
            panel.add(filePanel, "grow, shrink, wrap");
        }
        return panel;
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
        preferences.stringToColors(br.readLine());
        preferences.workingFolder = br.readLine();
        br.close();
        return preferences;
    }
    public void writePreferences(Preferences preferences) throws IOException {
        BufferedWriter bw = Files.newBufferedWriter(Paths.get("preferences.ini"));
        bw.write(preferences.fontSize+"\n"+preferences.theme+"\n"+preferences.colorsToString()+"\n"+preferences.workingFolder+"\n");
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
                JOptionPane.showMessageDialog(null, "Копирование было завершено за "+(double)((System.currentTimeMillis()-time)/100)/10 +" секунд");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Копирование не было завершено! Ошибка чтения файла");
                deleteFolderRecursively(targetDir.toFile());
                DBManager.getInstance(false).delete(targetDir.toFile());
            }finally {
                ui.remove(task);
                tasks.remove(task);
            }
        }).start();
    }
}
