import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class DBManager {
    static DBManager instance;
    public boolean debug = false;
    String databaseURL = "jdbc:sqlite:disks.db"; // Укажите имя файла базы данных
    public static DBManager getInstance(boolean debug){
        if(instance==null){
            instance = new DBManager(debug);
        }
        return instance;
    }
    private DBManager(boolean debug){
        this.debug=debug;
    }
    public void setPath(String path){
        databaseURL = "jdbc:sqlite:"+path+"/disks.db";
        createDB();
    }
    public void createDB(){
        try (Connection connection = DriverManager.getConnection(databaseURL)) {
            // Создание таблицы (если она не существует)
            String createTableSQL = "CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "folder TEXT," +
                    "firm TEXT," +
                    "model TEXT" +
                    ");";
            Statement statement = connection.createStatement();
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public void insert(String folder, String firm, String model){
        try (Connection connection = DriverManager.getConnection(databaseURL)) {
            String insertSQL = "INSERT INTO products (folder, firm, model) VALUES (?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, folder);
            preparedStatement.setString(2, firm);
            preparedStatement.setString(3, model);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public ArrayList<Disk> select(String modelFilter, String firmFilter) {
        ArrayList<Disk> disks = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(databaseURL)) {
            String selectSQL = "SELECT folder, firm, model FROM products WHERE LOWER(model) LIKE LOWER(?) AND LOWER(firm) LIKE LOWER(?)";
            PreparedStatement statement = connection.prepareStatement(selectSQL);
            statement.setString(1, "%"+modelFilter+"%");
            statement.setString(2, "%"+firmFilter+"%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String firm = resultSet.getString("firm");
                String folder = resultSet.getString("folder");
                String model = resultSet.getString("model");
                disks.add(new Disk(folder, firm, model));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage()+" at DBManager.select()");
        }
        disks.sort((o1, o2) -> {
            if(firmFilter.length()!=0){
                if(o1.firm.startsWith(firmFilter) && !o2.firm.startsWith(firmFilter)){
                    return -1;
                }
                if(o2.firm.startsWith(firmFilter) && !o1.toString().equals(firmFilter)){
                    return 1;
                }
            }
            if(modelFilter.length()!=0){
                if(o1.model.startsWith(modelFilter) && !o2.model.startsWith(modelFilter)){
                    return -1;
                }
                if(o2.model.startsWith(modelFilter) && !o1.model.equals(modelFilter)){
                    return 1;
                }
            }
            if(!o1.firm.equals(o2.firm)){
                return o1.firm.compareTo(o2.firm);
            }
            return o1.model.compareTo(o2.model);
        });
        return disks;
    }
    public int deleteArray(ArrayList<Disk> disks){
        int amo=0;
        for (Disk disk: disks){
            amo+=delete(disk.model, disk.firm);
        }
        return amo;
    }
    public int delete(String modelFilter, String firmFilter) {
        int deletedRows = 0;
        try (Connection connection = DriverManager.getConnection(databaseURL)) {
            String deleteSQL = "DELETE FROM products WHERE LOWER(model) LIKE LOWER(?) AND LOWER(firm) LIKE LOWER(?)";
            PreparedStatement statement = connection.prepareStatement(deleteSQL);
            statement.setString(1, modelFilter);
            statement.setString(2, firmFilter);
            deletedRows = statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage()+" at DBManager.delete()");
        }
        return deletedRows;
    }
    public int delete(File file) {
        int deletedRows = 0;
        try (Connection connection = DriverManager.getConnection(databaseURL)) {
            String deleteSQL = "DELETE FROM products WHERE LOWER(folder) LIKE LOWER(?)";
            PreparedStatement statement = connection.prepareStatement(deleteSQL);
            statement.setString(1, file.getName());
            deletedRows = statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage()+" at DBManager.delete()");
        }
        return deletedRows;
    }
    public boolean exists(String modelFilter, String firmFilter) {
        try (Connection connection = DriverManager.getConnection(databaseURL)) {
            // Выборка данных
            String selectSQL = "SELECT folder, firm, model FROM products WHERE LOWER(model) = LOWER(?) AND LOWER(firm) = LOWER(?)";
            PreparedStatement statement = connection.prepareStatement(selectSQL);
            statement.setString(1, modelFilter);
            statement.setString(2, firmFilter);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.err.println(e.getMessage()+" at DBManager.select()");
        }
        return false;
    }
}
