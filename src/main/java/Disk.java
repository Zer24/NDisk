public class Disk {
    String folder;
    String firm;
    String model;

    public Disk(String folder, String firm, String model){
        this.folder=folder;
        this.firm=firm;
        this.model=model;
    }
    @Override
    public String toString(){
        return "{"+folder+", "+firm+", "+model+"}";
    }
}
