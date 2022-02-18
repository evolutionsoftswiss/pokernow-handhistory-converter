package ch.evolutionsoft.poker.pokernow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ch.evolutionsoft.poker.pokernow.ConversionConstats.*;
import static ch.evolutionsoft.poker.pokernow.PokernowConstants.*;
import static ch.evolutionsoft.poker.pokernow.PokerstarsConstants.*;

public class PokernowHandHistoryConverter {
  
  public static final String IO_EXCEPTION_MESSAGE = "I/O Exception, please try again.";
  public static final String PROPERTIES_NOT_FOUND_MESSAGE =
      "Property file 'conversion.properties' not found. Make sure "
      + "you start the application from the directory containing the property files";
  
  private static final Logger converterLog = LogManager.getLogger(PokernowHandHistoryConverter.class);
  
  long handIdPrefix = 20200000;

  boolean convertOmahaHighHands = true;
  boolean convertOmahaHighLowHands = true;
  boolean convertTexasHands = false;
  double currencyFactor = 10;

  Properties nameMappingsProperties;
  
  String currentConversionFileName = StringUtils.EMPTY;
  
  String conversionErrors = StringUtils.EMPTY;
  
  PokernowSingleHandConverter singleHandConverter;
  
  public static void main(String[] args) throws IOException {

    PokernowHandHistoryConverter converter = new PokernowHandHistoryConverter();
    
    String configuredSourceFolder = PropertyHelper.readSourceFolderProperty();
    
    if (StringUtils.isEmpty(configuredSourceFolder)) {
      configuredSourceFolder = System.getProperty("user.dir");
    }
    converterLog.info("Using source folder with pokernow csv histories {}", configuredSourceFolder);
    
    Path sourceDirectory = Paths.get(configuredSourceFolder);
    
    File[] files = sourceDirectory.toFile().listFiles();
    
    List<File> sourceFiles = converter.getSourceFiles(files);

    if (!sourceFiles.isEmpty()) {
      
      converter.readConversionParameters();
      
      String configuredDestinationFolder = PropertyHelper.readDestinationFolderProperty();
      if (StringUtils.isEmpty(configuredDestinationFolder)) {
        configuredDestinationFolder = System.getProperty("user.dir");
      }
      converterLog.info("Using destination folder for converted hond histories {}", configuredDestinationFolder);

      for (File currentSourceFile : sourceFiles) {
        
        String currentHandHistory = FileUtils.readFileToString(
            currentSourceFile, 
            StandardCharsets.UTF_8);

        converter.currentConversionFileName = currentSourceFile.getName();
        
        String sortedHandHistory = converter.sortedHandHistoryLines(currentHandHistory);
        
        String convertedHandHistory = converter.convertHandHistory(sortedHandHistory, converter.currentConversionFileName);
        
        if (1 != converter.currencyFactor) {
          
          convertedHandHistory = converter.replaceBetAmounts(convertedHandHistory);
        }
        
        String destinationFileName = currentSourceFile.getName().replace(".csv", "-converted.txt"); 
        FileUtils.write(
            Paths.get(configuredDestinationFolder, destinationFileName).toFile(),
            convertedHandHistory, StandardCharsets.UTF_8);
        
        converterLog.info("{}{}{} converted successfully to {}{}{}",
            configuredSourceFolder, File.separator, converter.currentConversionFileName,
            configuredDestinationFolder, File.separator, destinationFileName);
      }
    
    } else if (!Paths.get(configuredSourceFolder).toFile().exists()) {

      converterLog.warn("Directory not found: Folder '{}' does not exist", configuredSourceFolder);
    } else {
      
      converterLog.warn("No csv file found: Folder '{}' contains no csv", configuredSourceFolder);
    }
  }

  private void readConversionParameters() {

    try {
      
      this.currencyFactor = PropertyHelper.readCurrencyFactor();
      this.convertOmahaHighHands = PropertyHelper.readConvertOmahaHighHands();
      this.convertOmahaHighLowHands = PropertyHelper.readConvertOmahaHighLowHands();
      this.convertTexasHands = PropertyHelper.readConvertTexasHands();

      this.singleHandConverter = new PokernowSingleHandConverter();
      
      converterLog.info("Using currencyFactor {}", this.currencyFactor);
      converterLog.info("Using convertOmahaHighHands {}", this.convertOmahaHighHands);
      converterLog.info("Using convertOmahaHiLoHands {}", this.convertOmahaHighLowHands);
      converterLog.info("Using convertTexasHands {}", this.convertTexasHands);
      
    } catch (FileNotFoundException e) {

      converterLog.error(PROPERTIES_NOT_FOUND_MESSAGE);
    } catch (IOException e) {

      converterLog.error(IO_EXCEPTION_MESSAGE);
    }
  }
  
  List<File> getSourceFiles(File[] files) {

    List<File> sourceFiles = new LinkedList<>();
    for (File currentFile : files) {
      
      String currentFileName = currentFile.getName();
      
      if (currentFileName.endsWith(".csv")) {
        sourceFiles.add(currentFile);
      }
    }
    return sourceFiles;
  }

  String replaceBetAmounts(String convertedHandHistory) throws IOException {
    
    BufferedReader bufferedReader = new BufferedReader(new StringReader(convertedHandHistory));
    
    String currentLine = bufferedReader.readLine();
    
    String betFactorConvertedHandHistory = StringUtils.EMPTY;
    
    while (null != currentLine) {
      
      if (currentLine.contains(DOLLAR_SIGN) && !currentLine.contains(POKER_STARS_HAND)) {
        
        betFactorConvertedHandHistory = handleBetOrRaiseCurrencyAmount(currentLine, betFactorConvertedHandHistory);
      
      } else if (currentLine.contains(POKER_STARS_HAND)) {
      
        betFactorConvertedHandHistory = replacePokerstarsHandHeaderCurrencyAmounts(currentLine,
            betFactorConvertedHandHistory);
        
      } else {
        
        betFactorConvertedHandHistory += currentLine + System.lineSeparator();
      }
      
      currentLine = bufferedReader.readLine();
      
    };
    
    return betFactorConvertedHandHistory;
  }

  String handleBetOrRaiseCurrencyAmount(String currentLine, String betFactorConvertedHandHistory) {

    int indexOfNextSpace = currentLine.indexOf(StringUtils.SPACE, currentLine.indexOf(PokerstarsConstants.DOLLAR_CHAR));
    int indexOfNextParanthese = currentLine.indexOf(')', currentLine.indexOf(PokerstarsConstants.DOLLAR_CHAR));
    
    if (currentLine.contains(RAISES_ACTION)) {
      
      String firstAmount = currentLine.substring(currentLine.indexOf(PokerstarsConstants.DOLLAR_CHAR) + 1, indexOfNextSpace);
      String secondAmount = currentLine.substring(currentLine.lastIndexOf(PokerstarsConstants.DOLLAR_CHAR) + 1, currentLine.length());

      Double convertedAmount1 = Double.parseDouble(firstAmount) / currencyFactor;
      Double convertedAmount2 = Double.parseDouble(secondAmount) / currencyFactor; 
      
      String betFactorConvertedLine = currentLine.replaceFirst(CURRENCY_AMOUNT_REGEX, ESCAPED_CURRENCY + convertedAmount1);
      betFactorConvertedLine = betFactorConvertedLine.replace(DOLLAR_CHAR + secondAmount, DOLLAR_CHAR + String.valueOf(String.format(AMOUNT_DOUBLE_FORMAT, convertedAmount2)));
      
      betFactorConvertedHandHistory += betFactorConvertedLine + System.lineSeparator();
      
    } else {

      String amount;
    
      if (indexOfNextSpace >= 0 && !currentLine.contains("Uncalled")) {
        
        amount = currentLine.substring(currentLine.indexOf(PokerstarsConstants.DOLLAR_CHAR) + 1, indexOfNextSpace);
     
      } else if (indexOfNextParanthese >= 0) {
        
        amount = currentLine.substring(currentLine.indexOf(PokerstarsConstants.DOLLAR_CHAR) + 1, indexOfNextParanthese);
      } else {
        
        amount = currentLine.substring(currentLine.indexOf(PokerstarsConstants.DOLLAR_CHAR) + 1, currentLine.length());
      }
      
      Double convertedAmount = Double.parseDouble(amount) / currencyFactor; 
      
      String betFactorConvertedLine = currentLine.replaceFirst(CURRENCY_AMOUNT_REGEX, ESCAPED_CURRENCY + String.format(AMOUNT_DOUBLE_FORMAT, convertedAmount));
      
      betFactorConvertedHandHistory += betFactorConvertedLine + System.lineSeparator();
    }
    return betFactorConvertedHandHistory;
  }

  String replacePokerstarsHandHeaderCurrencyAmounts(String currentLine, String betFactorConvertedHandHistory) {

    String originalSmallBlindAmount = currentLine.substring(
        currentLine.indexOf("($") + 2,
        currentLine.indexOf(FORWARD_SLASH + DOLLAR_SIGN)
    );
   
    String originalBigBlindAmount = currentLine.substring(
        currentLine.indexOf(SLASH_DOLLAR) + 2,
        currentLine.indexOf(StringUtils.SPACE, currentLine.indexOf(SLASH_DOLLAR))
    );

    Double convertedSmallBlindAmount = Double.parseDouble(originalSmallBlindAmount) / currencyFactor;
    Double convertedBigBlindAmount = Double.parseDouble(originalBigBlindAmount) / currencyFactor; 
    
    String handHeaderBlindsConvertedLine = currentLine.replaceFirst(
        "\\(" + CURRENCY_AMOUNT_REGEX,
        "(" + ESCAPED_CURRENCY + String.format(AMOUNT_DOUBLE_FORMAT, convertedSmallBlindAmount));
    handHeaderBlindsConvertedLine = handHeaderBlindsConvertedLine.replaceFirst(
        FORWARD_SLASH + CURRENCY_AMOUNT_REGEX, FORWARD_SLASH + ESCAPED_CURRENCY + String.format(AMOUNT_DOUBLE_FORMAT, convertedBigBlindAmount));
    
    betFactorConvertedHandHistory += handHeaderBlindsConvertedLine + System.lineSeparator();
    return betFactorConvertedHandHistory;
  }
  
  String convertHandHistory(String handHistory, String currentFileName) throws IOException {

    conversionErrors = StringUtils.EMPTY;
    int indexOfHandEnd = handHistory.indexOf(STARTING_HAND);

    if (indexOfHandEnd < 0) {

      converterLog.info("No hands to convert found yet in {}", currentFileName);
      return StringUtils.EMPTY;
    }
    
    String strippedHandHistory = handHistory.substring(indexOfHandEnd);
    String initialSummaryInfo = strippedHandHistory.substring(0, strippedHandHistory.indexOf(StringUtils.LF));
    String convertedHandHistoryBase = initialSummaryInfo + StringUtils.LF + handHistory.substring(handHistory.indexOf(INTRO_TEXT_END_2));
    
    convertedHandHistoryBase = normalizePlayerNames(convertedHandHistoryBase);
    
    if (singleHandConverter.isReadYourHoleCards() &&
        !nameMappingsProperties.keySet().contains(singleHandConverter.getYourUniqueName())) {
      
      converterLog.warn("For handhistory file {}", currentConversionFileName);
      converterLog.warn("  Given yourUniqueName={} was not found with mappings "
          + "from 'name-mappings.properties' and present player nicknames in the current handhistory.",
          singleHandConverter.getYourUniqueName());
      converterLog.info("  To read all your hole cards you should extend 'name-mappings.properties'"
          + " or adjoust yourUniqueName to your name from 'name-mappings.properties'");
    }
    
    String convertedHandHistory = StringUtils.EMPTY;
    int indexOfSingleHandBegin = 0;
    int indexOfSingleHandEnd = convertedHandHistoryBase.indexOf(ENDING_HAND, indexOfSingleHandBegin);
    
    this.handIdPrefix = ConversionUtils.readIdPrefixFromDatetime(strippedHandHistory);
    
    do {
      String singleHandHistoryBase = convertedHandHistoryBase.substring(indexOfSingleHandBegin, indexOfSingleHandEnd);

      if ( readSingleHand(singleHandHistoryBase) ) {
        
        try {
          convertedHandHistory += singleHandConverter.convertSingleHand(singleHandHistoryBase, handIdPrefix);
        
        } catch (NumberFormatException | StringIndexOutOfBoundsException conversionException) {
          
          converterLog.error("Error converting hand {}", singleHandConverter.readHandNumber(singleHandHistoryBase));
          conversionErrors += singleHandHistoryBase + System.lineSeparator() + System.lineSeparator();
        }
      }
        
      indexOfSingleHandBegin = convertedHandHistoryBase.indexOf(STARTING_HAND, indexOfSingleHandEnd);
      indexOfSingleHandEnd = convertedHandHistoryBase.indexOf(ENDING_HAND, indexOfSingleHandBegin);
        
    } while (indexOfSingleHandBegin > 0 && indexOfSingleHandEnd > 0);
    

    if (!conversionErrors.isEmpty()) {
      
      String errorHandsFilename = "ErrorHands-" + handIdPrefix + ".txt";
      FileUtils.write(new File(errorHandsFilename), conversionErrors, StandardCharsets.UTF_8);
      converterLog.info("Write error file {}", errorHandsFilename);
    }

    return convertedHandHistory;
  }

  boolean readSingleHand(String singleHandHistoryBase) {

    return (convertTexasHands && singleHandHistoryBase.contains(TEXAS_GAME_TYPE) ||
        convertOmahaHighHands && singleHandHistoryBase.contains(OMAHA_GAME_TYPE) || 
        convertOmahaHighLowHands && singleHandHistoryBase.contains(OMAHA_HI_LO_GAME_TYPE) );
  }

  String normalizePlayerNames(String handHistory) throws IOException, FileNotFoundException {

    Map<String, Set<String>> playerNamesById = collectNamesByPlayerId(handHistory);
    
    nameMappingsProperties = PropertyHelper.readNamesProperties();
    
    String convertedHandHistory = handHistory;
    
    for (Map.Entry<String, Set<String>> playerEntry : playerNamesById.entrySet()) {
      
      boolean mappedPlayerName = false;
      
      for (Map.Entry<Object,Object> entry : nameMappingsProperties.entrySet()){
          
        for (String currentPlayerName : playerEntry.getValue()) {

          String playerNameOccurance1 = TRIPLE_DOUBLE_QUOTE + currentPlayerName + PLAYER_ID_PREFIX +  playerEntry.getKey() + DOUBLE_DOUBLE_QUOTE;
          String playerNameOccurance2 = DOUBLE_DOUBLE_QUOTE + currentPlayerName + PLAYER_ID_PREFIX +  playerEntry.getKey() + DOUBLE_DOUBLE_QUOTE;
          List<String> usedNames = Arrays.asList(StringUtils.split(entry.getValue().toString(), COMMA_CHAR));
          if (usedNames.contains(currentPlayerName)) {
              
             convertedHandHistory = convertedHandHistory.
                 replace(playerNameOccurance1, (String) entry.getKey());
             convertedHandHistory = convertedHandHistory.
                 replace(playerNameOccurance2, (String) entry.getKey());
             mappedPlayerName = true;
          }
        }
      }
      
      if (!mappedPlayerName) {

        for (String currentPlayerName : playerEntry.getValue()) {
          String playerNameOccurance1 = TRIPLE_DOUBLE_QUOTE + currentPlayerName + PLAYER_ID_PREFIX +  playerEntry.getKey() + DOUBLE_DOUBLE_QUOTE;
          String playerNameOccurance2 = DOUBLE_DOUBLE_QUOTE + currentPlayerName + PLAYER_ID_PREFIX +  playerEntry.getKey() + DOUBLE_DOUBLE_QUOTE;
          convertedHandHistory = convertedHandHistory.replace(playerNameOccurance1, currentPlayerName);
          convertedHandHistory = convertedHandHistory.replace(playerNameOccurance2, currentPlayerName);
        }
      }
      
    }
    return convertedHandHistory;
  }
  
  Map<String, Set<String>> collectNamesByPlayerId(String handHistory) throws IOException {
    
    String handHistoryLine = StringUtils.EMPTY;
    Map<String, Set<String>> playerNamesByPlayerId = new HashMap<String, Set<String>>(SMALL_CAPACITY);
    BufferedReader bufferedReader = new BufferedReader(new StringReader(handHistory));
      
      do {
        
        String playerId = readPlayerId(handHistoryLine);
        
        if (null != playerId) {
          
          String playerName = readPlayerName(handHistoryLine);
          
          if (playerNamesByPlayerId.containsKey(playerId)) {
            
            playerNamesByPlayerId.get(playerId).add(playerName);
          
          } else {
            
            Set<String> playerNames = new HashSet<String>();
            playerNames.add(playerName);
            
            playerNamesByPlayerId.put(playerId, playerNames);
          }
        }
        
        handHistoryLine = bufferedReader.readLine();
        
      } while (null != handHistoryLine && !handHistoryLine.isEmpty());
    
    return playerNamesByPlayerId;
  }
  
  String readPlayerId(String handHistoryLine) {
    
    if (handHistoryLine.startsWith(TRIPLE_DOUBLE_QUOTE) && handHistoryLine.contains(PokernowConstants.PLAYER_ID_PREFIX)) {
      
      int indexOfIdStart = handHistoryLine.indexOf(PLAYER_ID_PREFIX) + PLAYER_ID_PREFIX_LENGTH;
      int indexOfIdEnd = handHistoryLine.indexOf(DOUBLE_QUOTE, indexOfIdStart);
      
      return handHistoryLine.substring(indexOfIdStart, indexOfIdEnd);
    }
    
    return null;
  }
  
  /**
   * There is a player name on this line assumption.
   * The player Id was already read before
   * 
   * @param handHistoryLine
   * @return
   */
  String readPlayerName(String handHistoryLine) {
    
    int indexOfPlayerName = handHistoryLine.indexOf(TRIPLE_DOUBLE_QUOTE);
    int indexOfPlayerNameStart = -1;
    if (indexOfPlayerName >= 0) {
      
      indexOfPlayerNameStart = indexOfPlayerName + 3;
    
    } else {
      
      indexOfPlayerNameStart = handHistoryLine.indexOf(DOUBLE_DOUBLE_QUOTE) + 2;
    }
    
    int indexOfPlayerNameEnd = handHistoryLine.indexOf(PLAYER_ID_PREFIX);
    
    return handHistoryLine.substring(indexOfPlayerNameStart, indexOfPlayerNameEnd);
  }
  
  String sortedHandHistoryLines(String handHistory) throws IOException {
    
    SortedMap<Long, String> sortedHandHistoryLines = new TreeMap<>();

    BufferedReader reader = new BufferedReader(new StringReader(handHistory));
    
    reader.readLine();
    
    String currentLine = reader.readLine();
    do {
      String entryKey = currentLine.substring(currentLine.lastIndexOf(
          COMMA_CHAR) + 1, currentLine.length());
      sortedHandHistoryLines.put(Long.parseLong(entryKey), currentLine);
      currentLine = reader.readLine();
      
    } while (currentLine != null && !currentLine.isEmpty());
    
    String sortedHandHistory = "";
    
    for (Map.Entry<Long, String> mapEntry : sortedHandHistoryLines.entrySet()) {
      
      sortedHandHistory += mapEntry.getValue() + POKERNOW_NEWLINE;
    }
    
    return sortedHandHistory;
  }
}
