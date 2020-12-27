package ch.evolutionsoft.poker.pokernow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertyHelper {

  public static final String SOURCE_FOLDER_PROPERTY = "folderWithLatestCsv";
  public static final String DESTINATION_FOLDER_PROPERTY = "folderOfConvertedCsv";
  public static final String CURRENCY_FACTOR = "currencyReductionFactor";
  public static final String CONVERT_OMAHA_HIGH_HANDS = "convertOmahaHiHands";
  public static final String CONVERT_OMAHA_HIGH_LOW_HANDS = "convertOmahaHiLoHands";
  public static final String CONVERT_TEXAS_HANDS = "convertTexasHands";
  public static final String READ_YOUR_HOLE_CARDS = "readYourHoleCards";
  public static final String YOUR_UNIQUE_NAME = "yourUniqueName";

  static Properties readNamesProperties() throws IOException {
    
    Properties nameMappingsProperties = new Properties();
   
    nameMappingsProperties.load(new FileInputStream("./name-mappings.properties"));
    
    return nameMappingsProperties;
  }
  
  static String readSourceFolderProperty() throws IOException {
    
    Properties conversionProperties = readConversionProperties();
    
    return conversionProperties.getProperty(SOURCE_FOLDER_PROPERTY);
  }
  
  static String readDestinationFolderProperty() throws IOException {
    
    Properties conversionProperties = readConversionProperties();
    
    return conversionProperties.getProperty(DESTINATION_FOLDER_PROPERTY);
  }
  
  static double readCurrencyFactor() throws FileNotFoundException, IOException {
    
    Properties conversionProperties = readConversionProperties();
    
    return Double.parseDouble(conversionProperties.getProperty(CURRENCY_FACTOR));
  }
  
  static boolean readConvertOmahaHighHands() throws IOException {

    Properties conversionProperties = readConversionProperties();
    
    return Boolean.parseBoolean(conversionProperties.getProperty(CONVERT_OMAHA_HIGH_HANDS));
  }
  
  static boolean readConvertOmahaHighLowHands() throws IOException {

    Properties conversionProperties = readConversionProperties();
    
    return Boolean.parseBoolean(conversionProperties.getProperty(CONVERT_OMAHA_HIGH_HANDS));
  }
  
  static boolean readConvertTexasHands() throws IOException {

    Properties conversionProperties = readConversionProperties();
    
    return Boolean.parseBoolean(conversionProperties.getProperty(CONVERT_TEXAS_HANDS));
  }
  
  static boolean readConvertYourHoleCards() throws IOException {

    Properties conversionProperties = readConversionProperties();
    
    return Boolean.parseBoolean(conversionProperties.getProperty(READ_YOUR_HOLE_CARDS));
  }
  
  static String readYourUniqueName() throws FileNotFoundException, IOException {

    Properties conversionProperties = readConversionProperties();
    
    return conversionProperties.getProperty(YOUR_UNIQUE_NAME);
  }

  static Properties readConversionProperties() throws IOException {

    Properties conversionProperties = new Properties();
    
    conversionProperties.load(new FileInputStream("./conversion.properties"));
    return conversionProperties;
  }
}
