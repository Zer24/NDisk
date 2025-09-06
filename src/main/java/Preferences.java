public class Preferences {
    int fontSize = 20;
    String theme = "Светлая";
    int colorBg1 = 230, colorBg2 = 230, colorBg3 = 230;
    int colorBtn1 = 51, colorBtn2 = 102, colorBtn3 = 255;
    int colorBtnDe1 = 102, colorBtnDe2 = 153, colorBtnDe3 = 255;
    int colorFont1 = 0, colorFont2 = 0, colorFont3 = 0;
    int colorField1 = 245, colorField2 = 245, colorField3 = 245;
    String workingFolder = ".";
    public String colorsToString(){
        return colorBg1+" "+colorBg2+" "+colorBg3+" "+colorBtn1+" "+colorBtn2+" "+colorBtn3+" "+colorBtnDe1+" "+colorBtnDe2+" "+colorBtnDe3+" "+colorFont1+" "+colorFont2+" "+colorFont3+" "+colorField1+" "+colorField2+" "+colorField3;
    }
    public void stringToColors(String string){
        String[] colors = string.split(" ");
        colorBg1 = Integer.parseInt(colors[0]);
        colorBg2 = Integer.parseInt(colors[1]);
        colorBg3 = Integer.parseInt(colors[2]);
        colorBtn1 = Integer.parseInt(colors[3]);
        colorBtn2 = Integer.parseInt(colors[4]);
        colorBtn3 = Integer.parseInt(colors[5]);
        colorBtnDe1 = Integer.parseInt(colors[6]);
        colorBtnDe2 = Integer.parseInt(colors[7]);
        colorBtnDe3 = Integer.parseInt(colors[8]);
        colorFont1 = Integer.parseInt(colors[9]);
        colorFont2 = Integer.parseInt(colors[10]);
        colorFont3 = Integer.parseInt(colors[11]);
        colorField1 = Integer.parseInt(colors[12]);
        colorField2 = Integer.parseInt(colors[13]);
        colorField3 = Integer.parseInt(colors[14]);
    }
}
