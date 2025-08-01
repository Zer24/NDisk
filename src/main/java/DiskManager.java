import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class DiskManager {
    public static final boolean IDEAL_INCORPORATION = true;
    public static final boolean BEST_INCORPORATION = false;
    public static final int BY_FOLDER = 1;
    public static final int BY_FIRM = 2;
    public static final int BY_MODEL = 3;
    public DiskManager() {
    }
    public ArrayList<Disk> filterDisks(ArrayList<Disk> disks, String filter, boolean incorporation, int filterType){
        ArrayList<Disk> newDisks;
        if(incorporation) {
            newDisks = new ArrayList<>();
            for (Disk disk : disks) {
                String line = "";
                switch (filterType) {
                    case 1 -> line = disk.folder;
                    case 2 -> line = disk.firm;
                    case 3 -> line = disk.model;
                }
                if (!Objects.equals(line.toLowerCase(), filter.toLowerCase())) {
                    continue;
                }
                newDisks.add(disk);
            }
        }else{
            newDisks = new ArrayList<>(disks);
            switch (filterType) {
                case 1 -> newDisks.sort(new Comparator<Disk>() {
                    @Override
                    public int compare(Disk o1, Disk o2) {
                        return longestPrefix(o2.folder, filter) - longestPrefix(o1.folder, filter);
                    }
                });
                case 2 -> newDisks.sort(new Comparator<Disk>() {
                    @Override
                    public int compare(Disk o1, Disk o2) {
                        return longestPrefix(o2.firm, filter) - longestPrefix(o1.firm, filter);
                    }
                });
                case 3 -> newDisks.sort(new Comparator<Disk>() {
                    @Override
                    public int compare(Disk o1, Disk o2) {
                        return longestPrefix(o2.model, filter) - longestPrefix(o1.model, filter);
                    }
                });
            }
        }
        return newDisks;
    }
    public int longestPrefix(String from, String filter){
        from=from.toLowerCase();
        filter=filter.toLowerCase();
        //System.out.print("between "+from+" and "+filter+": ");
        for (int i = filter.length(); i >= 0; i--) {
            if(from.contains(filter.substring(0,i))){
                //System.out.println(i);
                return i;
            }
        }
        return 0;
    }
}
