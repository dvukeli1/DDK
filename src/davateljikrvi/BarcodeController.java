/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package davateljikrvi;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import net._95point2.fpe.FPE;

/**
 * 
 * @author Mitja
 */

public class BarcodeController {
    
    private final byte[] key;
    private final byte[] tweak;
    private final String IDTerm;
    private final String IDPark;
    private String encoded,rawdata,yearMark;
    private final int data[] = new int[12];
    private String timeNow;
    private final String modulo = "999999999999";

    public String getTimeNow() {
        return timeNow;
    }
   
     public String getRawdata() {
        return rawdata;
    } 

    /**
     * Constructor 
     * @param key - pass phrase key 1
     * @param tweak - pass phrase key 2
     * @param IDTerm - Terminal ID
     * @param IDPark  - Parking ID
     */
    public BarcodeController(String key, String tweak, String IDTerm, String IDPark) {
        this.key = key.getBytes();
        this.tweak = tweak.getBytes();
        this.IDTerm = IDTerm;
        this.IDPark = IDPark;
        this.rawdata="";
        
    }
    
     public BarcodeController(String IDTerm, String IDPark) {
        this.key = new byte[1];
        this.tweak = new byte[1];
        this.IDTerm = IDTerm;
        this.IDPark = IDPark;
        this.rawdata="";
        
    }
    
    /**
     * Encode 12 digit number for bar code writer  
     * format [TerminalID][ParkingID][Seconds from start of the year]
     * @return encoded string
     */
    public String Encode(){
        this.rawdata = "";
        ClearArray();
        this.data[0] = Integer.valueOf(IDTerm);
        int ti= Integer.valueOf(IDPark);
       
        for ( int i = 2; i>0;i--){
          this.data[i] = ti%10;
          ti/=10;
        } 
        String minimum ="100000000000";
        String rawEncoded ="0";
        FillArray(SecondsFromYear());
        this.rawdata = MakeString(this.rawdata);
        BigInteger modulus = BigInteger.valueOf(Long.parseLong(modulo));
        BigInteger plainText = BigInteger.valueOf(Long.parseLong(this.rawdata));
        try {
            rawEncoded = String.valueOf(FPE.encrypt(modulus, plainText, this.key, this.tweak));
            ClearArray();
            FillArray(Long.valueOf(rawEncoded));
            this.encoded = MakeString(this.encoded);
                  
        } catch (Exception ex) {
            Logger.getLogger(BarcodeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return this.encoded;
    }

    /**
     *  Decript input string
     * @param input
     * @param encript 
     * @return 
     */
    public String Decript(String input,boolean encript){
        char byteDec[];
        BigInteger modulus = BigInteger.valueOf(Long.parseLong(modulo));
        BigInteger encripted = BigInteger.valueOf(Long.parseLong(input)/10);
        String decripted ="Neispravna kartica";
        String testMessage = "Podatci sa kartice :\n";
        String rawDecripted ="0";
        try {
            rawDecripted = String.valueOf(FPE.decrypt(modulus, encripted, key, tweak));
        } catch (Exception ex) {
            Logger.getLogger(BarcodeController.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("RAW Decripted  = " + rawDecripted );
        if(encript){
        byteDec = rawDecripted.toCharArray();
        }
        else{
        String temp = ""+Long.parseLong(input)/10;
        byteDec = temp.toCharArray();
        }
        String seconds ="";
        for (int i=3;i<12;i++){
            seconds+=byteDec[i];
           
        }
        System.out.println("Sekunde = " + (Long.valueOf(seconds)/10));
        System.out.println("Znamenka godine = " + (Long.valueOf(seconds)%10));
        long secondsFromStart = SecondsFromYear();
        System.out.println("Sekunde od pocetka = " + secondsFromStart);
        long second = (secondsFromStart - (Long.valueOf(seconds)/10) );
        System.out.println("Izracunate sekunde = " + second);
        if((second>0) && ((LocalDateTime.now().getYear()%10) == (Long.valueOf(seconds)%10))){
        long minute = second/60;
        long hour = minute/60;
        if(minute>0) second = second%60;
        if(hour>0) minute = minute%60;
        testMessage +="ID Terminala : " + byteDec[0] + "\n";
        testMessage +="ID Parkinga : " + byteDec[1] + byteDec[2] +"\n";
        testMessage=testMessage + String.format("Vrijeme na parkingu %02d:%02d:%02d",hour,minute,second);
        decripted = testMessage;
        }
        
        return decripted;
    }
    
    /**
     * Clean barcode to 0000000000000
     */
    private void ClearArray(){
        for (int i = 0; i < data.length-1; i++) {
            data[i] = 0;
        }
    }
    
    /**
     * Make Barcode
     * @return raw barcode string 
     */
    public String Barcode(){
      
       String barcode = this.IDTerm + this.IDPark; 
        barcode+=MakeSec(SecondsFromYear());
        barcode += yearMark;
        return barcode;
    }
    
    /**
     * Calculate seconds from year start
     * @return  long seconds 
     */
    private Long SecondsFromYear(){
        long sec;
        LocalDateTime endDate = LocalDateTime.now();
        int month = endDate.getMonthValue();
        month = month-1;
        int code = month * 32 ; code = code + endDate.getDayOfMonth() ; code = code * 86400;
        int hour = endDate.getHour();
        if(hour != 0){
            for (int i = 0; i < hour; i++) {
                code = code +3600;
            }
        }
        int temp = (endDate.getMinute()*60) + endDate.getSecond();
       sec = code + temp;
        timeNow = endDate.getDayOfMonth() + "." + endDate.getMonthValue() + "." + endDate.getYear();
        timeNow +=" " + endDate.getHour()+":"+endDate.getMinute();
        yearMark = "" + (endDate.getYear()%10);
        return sec;
    }
    
    /**
     * 
     * @param data 
     */
    private void FillArray(long data){
         int j = this.data.length-1;
         data *=10;
         data+= (LocalDateTime.now().getYear()%10);
        do {
            long temp = data % 10;
        this.data[j] = (int)temp;
            System.out.println(j+" : " + this.data[j]);
            data /= 10;
            j--;
        } while (data > 0);
 
    }
    
    private String MakeSec( Long l){
       
        String fin="";
        int dataSec[] = new int[8];
         int j = dataSec.length -1;
        for (int i = 0; i < dataSec.length; i++) {
            dataSec[i] = 0;
            
        }
        do {
            long temp = l % 10;
        dataSec[j] = (int)temp;
            System.out.println(j+" : " + this.data[j]);
           l /= 10;
            j--;
        } while (l > 0);
        
        for (int i = 0; i < dataSec.length; i++) {
            fin += dataSec[i];
            
        }
        return fin;
    }
    
    
    private String MakeString(String fs){
        fs="";
         for (int i = 0; i < this.data.length; i++) {
            // System.out.println(i+" : " + fs);
            fs += String.valueOf(this.data[i]);
        }
        
         return fs;
    }
      
    
}
