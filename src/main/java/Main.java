/*import java.io.File;
import java.io.IOException;*/

public class Main {
    public static void main(String[] args) {
/*        File workDir = new File("./out/artifacts/NDrivers_jar");
        for (File directory:workDir.listFiles()){
            if(!directory.isDirectory()){
                continue;
            }
            for (File subdirectory:directory.listFiles()){
                try {
                    new File(subdirectory,"for testing purposes.txt").createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }*/
        new UI();
    }
}