
package davateljikrvi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author mitja@elmas.hr 2015
 */
public class DavateljiKrvi {

     private static final DBController db = new DBController();
     private static GpioController gpio;
     private static GpioPinDigitalInput  tipka1;
     private static GpioPinDigitalOutput led;
     private static PrinterController print;
     private static String karta ="";
     public static boolean isPrinterReady = true;
     public static boolean  isPrinting = false;
     private static Serial serial ;
     private static boolean ucitana = false;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        print = new PrinterController();
        
        gpio = GpioFactory.getInstance();
        serial= SerialFactory.createInstance();
        tipka1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, PinPullResistance.PULL_UP);
        led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, PinState.HIGH);
        tipka1.setDebounce(100);
       
        tipka1.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
         
         
            if (event.getState().isLow()) {
              
                System.out.println("Tipka 1 - ON");
                if(isPrinterReady && !isPrinting){
                     isPrinting = true;
                      isPrinterReady = false;
                    SerialSend();
                    int counter = print.GetCounter();
                     db.DBInsert(counter-1);
                }
               
            }
            if (event.getState().isHigh()) {
             
                System.out.println("Tipka 1 - OFF");
                        isPrinting = false;
                      isPrinterReady = true;
            }

        });
       UcitajKartu();
       while (true) {     
           if(ucitana) {
                led.blink(1000);
                ucitana = !ucitana;
           }
           
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                Logger.getLogger(DavateljiKrvi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    private static boolean UcitajKartu(){
        BufferedReader br = null;
        
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader("/home/pi/karta.kta"));

			while ((sCurrentLine = br.readLine()) != null) {
                             karta += sCurrentLine +"\n";
				//System.out.println(karta);
                             
			}
               
                ucitana = true;
		} catch (IOException e) {
                    ucitana = false;
		} finally {
			try {
				if (br != null)br.close();
                                 
			} catch (IOException ex) {
			}
		}
                return ucitana;
    }
    
    private static boolean SerialSend(){
        
        if(serial.isClosed()){
                         serial.open("/dev/usb/lp0", 9600);
                    }
                   
                    byte data[] = print.PrintCard(karta);
                    serial.write(data);
                    
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(DavateljiKrvi.class.getName()).log(Level.SEVERE, null, ex);
         }
        return isPrinterReady; 
    }
}
