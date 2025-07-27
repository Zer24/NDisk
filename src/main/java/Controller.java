import java.util.Scanner;

public class Controller {
    String rules = """
            1 - вывести список всех папок
            2 - создать папку
            3 - открыть папку""";
    public void mainLoop(){
        new UI();
//        Scanner scanner = new Scanner(System.in);
//        int var;
//        FileManager fm = new FileManager();
//        while(true) {
//            try {
//                System.out.println(rules);
//                var = scanner.nextInt();
//                switch (var) {
//                    case 1 -> fm.readFolders(fm.getFolders());
//                    case 2 -> fm.createFolder(scanner);
//                    case 3 -> {System.out.println("Название папки?: "); fm.openFolder(scanner.next());}
//                    case default -> System.out.println("Такого варианта нет!");
//                }
//            }catch (Exception e){
//                System.err.println("Ошибка!");
//            }
//        }
    }
}
