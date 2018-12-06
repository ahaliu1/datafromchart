package utils;

import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.util.LoadLibs;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tess4jUtils {

    private static File tessDataFolder = LoadLibs.extractTessResources("tessdata");
//    /**
//     * 从图片中提取文字,默认设置英文字库,使用classpath目录下的训练库
//     * @param path
//     * @return
//     */
//    public static String readChar(String path){
//        // JNA Interface Mapping
//        ITesseract instance = new Tesseract();
//        // JNA Direct Mapping
//        // ITesseract instance = new Tesseract1();
//        File imageFile = new File(path);
//        //In case you don't have your own tessdata, let it also be extracted for you
//        //这样就能使用classpath目录下的训练库了
//        //Set the tessdata path
//        instance.setDatapath(tessDataFolder.getAbsolutePath());
//        //英文库识别数字比较准确
//        instance.setLanguage("eng");
//        return (instance, imageFile);
//    }


    /**
     *
     */
    public String doOCRInRectangle(String path,int x,int y,int width,int height) throws Exception {
        // JNA Interface Mapping
        ITesseract instance = new Tesseract();
        // JNA Direct Mapping
        // ITesseract instance = new Tesseract1();
        File imageFile = new File(path);
        //In case you don't have your own tessdata, let it also be extracted for you
        //Set the tessdata path
        instance.setDatapath(tessDataFolder.getAbsolutePath());
        //英文库识别数字比较准确
        instance.setLanguage("eng");
        //划定区域
        // x,y是以左上角为原点，width和height是以xy为基础
        Rectangle rect = new Rectangle(x, y, width, height);
        return instance.doOCR(imageFile, rect);

    }

    /**
     * Test of getSegmentedRegions method, of class Tesseract.
     * 得到每一个划分区域的具体坐标
     * @throws java.lang.Exception
     */
    public List<Word> getNumWords(String path) throws Exception {
        // JNA Interface Mapping
        ITesseract instance = new Tesseract();
        File imageFile = new File(path);
        //Set the tessdata path
        instance.setDatapath(tessDataFolder.getAbsolutePath());
        //英文库识别数字比较准确
        instance.setLanguage("eng");
        BufferedImage bi = ImageIO.read(imageFile);
        //设置识别的level
        int level = ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE;
        //得到
        List<Word> words =instance.getWords(bi,level);

        Pattern p = Pattern.compile("^[0-9]*$");
        List<Word> wordList = new ArrayList<Word>();

        for (int i = 0; i < words.size(); i++) {
            String text = words.get(i).getText().replaceAll("\n","").replaceAll(" ","");
            Matcher m=p.matcher(text);
            if (m.matches()){
                Word word = words.get(i);
                wordList.add(word);
            }
        }
        return wordList;
    }

    /**
     * Test of getSegmentedRegions method, of class Tesseract.
     * 得到每一个划分区域的具体坐标
     * @throws java.lang.Exception
     */
    public List<Word> getChiWords(String path) throws Exception {
        // JNA Interface Mapping
        ITesseract instance = new Tesseract();
        File imageFile = new File(path);
        //Set the tessdata path
        instance.setDatapath(tessDataFolder.getAbsolutePath());
        //英文库识别数字比较准确
        instance.setLanguage("chi_sim");
        BufferedImage bi = ImageIO.read(imageFile);
        //设置识别的level
        int level = ITessAPI.TessPageIteratorLevel.RIL_WORD;
        //得到
        List<Word> words =instance.getWords(bi,level);

        List<Word> wordList = new ArrayList<Word>();
        for (int i = 0; i < words.size(); i++) {
            String text = words.get(i).getText();

            if (text.contains("月")&&text.contains("日")){

                Word word = words.get(i);
//                Rectangle rect = word.getBoundingBox();
//                System.out.println(String.format("Box[%d]: x=%d, y=%d, w=%d, h=%d word=%s", i, rect.x, rect.y, rect.width, rect.height, words.get(i).getText()));

                wordList.add(word);
            }
        }
        return wordList;
    }


}