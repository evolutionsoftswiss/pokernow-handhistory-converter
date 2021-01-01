package ch.evolutionsoft.poker.pokernow;

import static ch.evolutionsoft.poker.pokernow.ConversionConstats.*;
import static ch.evolutionsoft.poker.pokernow.PokernowConstants.*;

import org.apache.commons.lang3.StringUtils;

public class ConversionUtils {

  private ConversionUtils() {
    // Hide constructor
  }
  
  static long readIdPrefixFromDatetime(String handHistory) {
    
    String handHistoryLine = handHistory.substring(0, handHistory.indexOf(NEWLINE));
    
    String datetime = handHistoryLine.replaceAll(POKERNOW_DATETIME_PATTERN, FIRST_REGEX_GROUP_MATCH);
    
    String datetimeDigits = datetime.replace("-", StringUtils.EMPTY);
    datetimeDigits = datetimeDigits.replace(TIME_PREFIX, StringUtils.EMPTY);
    datetimeDigits = datetimeDigits.replace(String.valueOf(DOUBLE_POINT), StringUtils.EMPTY);
    
    return Long.parseLong(datetimeDigits);
  }
  
  static double readSmallBlind(String handHistory, double lastSmallBlind) {
    
    handHistory = handHistory.replace(AND_GO_ALL_IN, StringUtils.EMPTY);
    
    int indexOfFirstSmallBlindPrefix = handHistory.indexOf(SMALL_BLIND_PREFIX);
    int indexOfNextNewLine = handHistory.indexOf(NEWLINE, indexOfFirstSmallBlindPrefix);

    if (indexOfFirstSmallBlindPrefix < 0) {
      
      return lastSmallBlind;
    }
    
    String smallBlindLinePart = handHistory.substring(indexOfFirstSmallBlindPrefix, indexOfNextNewLine);
    String smallBlindAmount = smallBlindLinePart.replaceFirst(SMALL_BLIND_PREFIX + ONE_OR_MORE_DIGITS_AND_CHARS_REGEX, FIRST_AND_SECOND_REGEX_GROUP_MATCH);
  
    return Double.parseDouble(smallBlindAmount);
  }
  
  static double readBigBlind(String handHistory) {
    
    handHistory = handHistory.replace(AND_GO_ALL_IN + StringUtils.SPACE, StringUtils.EMPTY);
    
    int indexOfFirstBigBlindPrefix = handHistory.indexOf(BIG_BLIND_PREFIX);
    int indexOfNextNewLine = handHistory.indexOf(NEWLINE, indexOfFirstBigBlindPrefix);
    
    String bigBlindLinePart = handHistory.substring(indexOfFirstBigBlindPrefix, indexOfNextNewLine);
    String bigBlindAmount = bigBlindLinePart.replaceFirst(BIG_BLIND_PREFIX + ONE_OR_MORE_DIGITS_AND_CHARS_REGEX, FIRST_AND_SECOND_REGEX_GROUP_MATCH);
  
    return Double.parseDouble(bigBlindAmount);
  }

  static String stripDateAndEntryOrderFromCsv(String gameAction) {

    return gameAction.substring(0, gameAction.indexOf(SINGLE_QUOTE_CHAR));
  }

  static String replaceColorsAndTens(String showdownAction) {

    showdownAction = showdownAction.replace(COMMA_CHAR + StringUtils.SPACE, StringUtils.SPACE);
    showdownAction = showdownAction.replace(PokernowConstants.SPADES, PokerstarsConstants.SPADES);
    showdownAction = showdownAction.replace(PokernowConstants.HEARTS, PokerstarsConstants.HEARTS);
    showdownAction = showdownAction.replace(PokernowConstants.CLUBS, PokerstarsConstants.CLUBS);
    showdownAction = showdownAction.replace(PokernowConstants.DIAMONDS, PokerstarsConstants.DIAMONDS);
    showdownAction = replaceTens(showdownAction);
    return showdownAction;
  }

  static String replaceTens(String gameAction) {

    gameAction = gameAction.replace("10s", "Ts");
    gameAction = gameAction.replace("10h", "Th");
    gameAction = gameAction.replace("10c", "Tc");
    gameAction = gameAction.replace("10d", "Td");
    return gameAction;
  }
}
