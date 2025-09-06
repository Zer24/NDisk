import java.sql.*;
import java.util.ArrayList;

public class DBManager {
    public boolean debug = false;
    String databaseURL = "jdbc:sqlite:disks.db"; // Укажите имя файла базы данных
    public DBManager(boolean debug){
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
            // Создание таблицы (если она не существует)
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
            // Выборка данных
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
        return disks;
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
