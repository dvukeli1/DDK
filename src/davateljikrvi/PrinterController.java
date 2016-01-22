/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package davateljikrvi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mitja
 */
public class PrinterController {
   
    BarcodeController coder;
    private String barcodeString;
    private String decodedString;
    private static Byte CLeftM[];
    private final Byte CBigFontOn[];
    private final Byte CBigFontOff[];
    private final Byte CBoldOn[];
    private final Byte CBoldOff[];
    private final Byte CUnderOn[];
    private final Byte CUnderOff[];
    private final Byte CBarcodeStx[] =new Byte[]{0x1b, 0x62, 0x03,0x01,0x03, 0x064}; 
    private final Byte CBarcodeC[] = new Byte[]{};
    private final Byte CBarcodeEtx[] =new Byte[]{0x1E}; //  (0x1E is end msg)
    private final Byte CTxt[] ={};
    private final Byte CLF[];
    private final Byte CCut[] = new Byte[]{0x1b, 0x64, 0x03}; // Cut paper
    private final Byte CReset[];
    private final Byte CEOT[];
    private final Byte CSTC[];
    private final Byte cCodePage[];
    private String tekstKarte;
    private int counter;
    private static Properties prop;
    private static InputStream input = null;
    private static OutputStream output = null;
    private byte margin[];
    private final String IDt,IDp,pass1,pass2;
    private boolean encode;
    
    public int GetCounter(){
        return this.counter;
    }
    
    /**
     *  Init with decode 
     * @param pass1
     * @param pass2
     * @param IDt
     * @param IDp 
     */
    public PrinterController(String pass1,String pass2, String IDt,String IDp) {
        LoadProperties();
        this.pass1 = pass1;
        this.pass2 = pass2;
        this.IDt = IDt;
        this.IDp = IDp;
        this.tekstKarte ="";
        this.counter = Integer.valueOf(prop.getProperty("counter","0"));
        this.coder = new BarcodeController(pass1, pass2, this.IDt, this.IDp);
        this.margin =  prop.getProperty("margin","1").getBytes(); 
        this.CBigFontOn = new Byte[]{0x1B,0x57,0x01,0x1B,0x68,0x01};
        this.CBigFontOff = new Byte[]{0x1B,0x14,0x14};
        this.CBoldOn = new Byte[]{0x1B,0x45};
        this.CBoldOff = new Byte[]{0x1B,0x46};
        this.CUnderOn = new Byte[]{0x1B,0x2D,0x01};
        this.CUnderOff = new Byte[]{0x1B,0x2D,0x00};
        this.CLeftM = new Byte[]{0x1B,0x6C,margin[0]};
        this.CLF = new Byte[]{0x0A};
        this.CEOT = new Byte[]{0x1B,0x0C,0x04};
        this.CReset = new Byte[]{0x1B,0x06,0x18};
        this.CSTC = new Byte[]{0x1B,0x1E,0x61,0x03};
        this.cCodePage = new Byte[]{0x1B,0x1D,0x74,0x05};
        this.encode = true;
      

    }
    
    /**
     *  Init without decode
     */
    public PrinterController() {
        LoadProperties();
        this.IDt = prop.getProperty("IDTerm","8");
        this.IDp = prop.getProperty("IDPark","10");
        this.pass1 = prop.getProperty("Pass1","0");
        this.pass2= prop.getProperty("Pass2","0");
        this.tekstKarte ="";
        this.counter = Integer.valueOf(prop.getProperty("counter","0"));
        this.coder = new BarcodeController(this.pass1, this.pass2, this.IDt, this.IDp);
        this.margin =  prop.getProperty("margin","1").getBytes(); 
        this.CBigFontOn = new Byte[]{0x1B,0x57,0x01,0x1B,0x68,0x01};
        this.CBigFontOff = new Byte[]{0x1B,0x14,0x14};
        this.CBoldOn = new Byte[]{0x1B,0x45};
        this.CBoldOff = new Byte[]{0x1B,0x46};
        this.CUnderOn = new Byte[]{0x1B,0x2D,0x01};
        this.CUnderOff = new Byte[]{0x1B,0x2D,0x00};
        this.CLeftM = new Byte[]{0x1B,0x6C,margin[0]};
        this.CLF = new Byte[]{0x0A};
        this.CEOT = new Byte[]{0x1B,0x0C,0x04};
        this.CReset = new Byte[]{0x1B,0x06,0x18};
        this.CSTC = new Byte[]{0x1B,0x1E,0x61,0x03};
        this.cCodePage = new Byte[]{0x1B,0x1D,0x74,0x05};
        this.encode = false;

    }
    
    /**
     * Load properties from file
     */
    public final void LoadProperties(){
         prop = new Properties();
        try {
            input = new FileInputStream("/home/pi/config.properties");
            prop.load(input);
             } catch (FileNotFoundException ex) {
          
        } catch (IOException ex) {
           
        }
    }
    
    public String getBString(){
        return this.barcodeString;
    }
     public String getDecoded(){
        return this.decodedString;
    }
     
     /**
      * Make arrayList from kta file
      * @return  arrayList
      */
    private ArrayList<Byte>  MakeText(){
       
        LocalDateTime dateTime = LocalDateTime.now();
        ArrayList<Byte> text = new ArrayList<>();
        try {
            byte txtB[] = tekstKarte.getBytes("cp852");
            
             for(int i = 0;i< txtB.length-1;i++){
                 if(txtB[i] == '%'){
                     i++;
                     switch(txtB[i]){
                        case 'V':
                            text.addAll(Arrays.asList(CBigFontOn));
                             break;
                        case 'v':
                            text.addAll(Arrays.asList(CBigFontOff));
                             break;
                        case 'R':
                           
                            text.addAll(IntToByteArray(counter));
                            
                             break;
                        case 'D':
                            text.addAll(IntToByteArray(dateTime.getDayOfMonth()));
                             break;
                        case 'M':
                            text.addAll(IntToByteArray(dateTime.getMonthValue()));
                             break;
                        case 'Y':
                            text.addAll(IntToByteArray(dateTime.getYear()%((dateTime.getYear()/1000)*1000)));
                             break;
                        case 'H':
                            text.addAll(IntToByteArray(dateTime.getHour()));
                             break;
                        case 'N':
                             text.addAll(IntToByteArray(dateTime.getMinute()));
                             break;
                        case 'B':
                            text.addAll(Arrays.asList(CBoldOn));
                             break;
                        case 'b':
                            text.addAll(Arrays.asList(CBoldOff));
                             break;
                        case 'U':
                            text.addAll(Arrays.asList(CUnderOn));
                             break;
                        case 'u':
                            text.addAll(Arrays.asList(CUnderOff));
                             break;
                           
                     }
                     
                 }
                 else{
                     text.add(txtB[i]);
                 }
             }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PrinterController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return text;
     }
    
    /**
     * Make barcode for card
     * @return 
     */
    private ArrayList<Byte>  MakeBarcode(){
          ArrayList<Byte> barcode = new ArrayList<>();
           int i = 0;
       
        barcode.addAll(Arrays.asList(CBarcodeStx));
        barcodeString = coder.Barcode();
        System.out.println(barcodeString);
        decodedString = coder.getRawdata();
       /* if(!encode){
            barcodeString = decodedString;
        }*/

        byte encodedString[] = barcodeString.getBytes();
       
        for (byte b : encodedString){
            System.out.println(" byte = " + b);
            barcode.add(b);
          
        }
        barcode.addAll(Arrays.asList(CBarcodeEtx));
          return barcode;
     }
    
    /**
     * Print card 
     * @param text
     * @return 
     */
    public byte[] PrintCard(String text,boolean first){
        
        LoadProperties();
        this.margin =  prop.getProperty("margin","1").getBytes(); 
        CLeftM = new Byte[]{0x1B,0x6C,margin[0]};
        tekstKarte = text;
        ArrayList<Byte> cardMaker = new ArrayList<>();
        cardMaker.addAll(MakeBarcode());
        cardMaker.addAll(Arrays.asList(CLF));
       // cardMaker.addAll(Arrays.asList(CLeftM));
        cardMaker.addAll(MakeText());
        cardMaker.addAll(Arrays.asList(CLF));
        cardMaker.addAll(MakeBarcode());
        cardMaker.addAll(Arrays.asList(CCut));
        //aBC.addAll(Arrays.asList(CReset));
        cardMaker.addAll(Arrays.asList(CSTC));
        byte[] card = new byte[cardMaker.size()];
        int i=0;
        for (byte b : cardMaker){
            
            card[i] = cardMaker.get(i);
             i++;
        }
        if(!first){
           counter++;
        UpdateCounter(); 
        }
        
        return card;
    }
    
    public String ReadCard(String data){
        
     
    return coder.Decript(data,encode);
     
       
        
    }
    
    private static ArrayList<Byte> IntToByteArray(int a){
        ArrayList<Byte> number = new ArrayList<>();
        String tempSt = String.format("%02d",a);
        try {
            byte numArr[] = tempSt.getBytes("cp852");
            for (byte b : numArr){
                number.add(b);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PrinterController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return number;
    }
    
    private  void UpdateCounter(){
      
        try {
            output = new FileOutputStream("/home/pi/config.properties");
            prop.setProperty("counter", ""+counter);
            prop.setProperty("IDTerm","8");
            prop.setProperty("IDPark","10");
            // save properties to project root folder
            prop.store(output, null);
           
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrinterController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrinterController.class.getName()).log(Level.SEVERE, null, ex);
        }
      

 }
}
