package ch.evolutionsoft.poker.pokernow;

import static ch.evolutionsoft.poker.pokernow.ConversionConstats.*;
import static ch.evolutionsoft.poker.pokernow.PokernowConstants.*;
import static ch.evolutionsoft.poker.pokernow.PokerstarsConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PokernowSingleHandConverter {
  
  private static final Logger handConversionLog = LogManager.getLogger(PokernowSingleHandConverter.class);
  
  String lastButtonPlayerName = StringUtils.EMPTY;
  String lastButtonPlayerSeat = StringUtils.EMPTY;
  
  double smallBlindAmount = 1;
  double bigBlindAmount = 1;
  double straddleAmount = 2;
  
  double latestBetTotalAmount = 1;
  Map<String, Double> lastRaiseAmountByPlayer = new HashMap<>();

  boolean readYourHoleCards = true;
  String yourHoleCards = StringUtils.EMPTY;
  String yourUniqueName = StringUtils.EMPTY;
  String yourSeat = StringUtils.EMPTY;

  public PokernowSingleHandConverter() throws IOException {

    this.readYourHoleCards = PropertyHelper.readConvertYourHoleCards();
    this.yourUniqueName = PropertyHelper.readYourUniqueName();
    
    handConversionLog.info("Using readYourHoleCards {}", this.readYourHoleCards);
    handConversionLog.info("Using yourUniqueName {}", this.yourUniqueName);
  }

  public PokernowSingleHandConverter(boolean readYourHoleCards, String yourUniqueName) {

    this.readYourHoleCards = readYourHoleCards;
    this.yourUniqueName = yourUniqueName;
    
    handConversionLog.info("Using readYourHoleCards {}", this.readYourHoleCards);
    handConversionLog.info("Using yourUniqueName {}", this.yourUniqueName);
  }

  String convertSingleHand(String singleHandHistoryBase, long handIdPrefix) throws IOException {
    
    this.smallBlindAmount = ConversionUtils.readSmallBlind(singleHandHistoryBase, this.smallBlindAmount);
    this.bigBlindAmount = ConversionUtils.readBigBlind(singleHandHistoryBase);
    this.straddleAmount = 2 * this.bigBlindAmount;
    this.latestBetTotalAmount = this.bigBlindAmount;
    this.lastRaiseAmountByPlayer.clear();

    BufferedReader bufferedReader = new BufferedReader(new StringReader(singleHandHistoryBase));
    String singleHandHistoryLine = bufferedReader.readLine();

    String handNumber = readHandNumber(singleHandHistoryLine);
    
    String gameType = singleHandHistoryLine.substring(
        singleHandHistoryLine.indexOf('(') + 1, singleHandHistoryLine.indexOf(')'));
    String timeString = singleHandHistoryLine.substring(
        singleHandHistoryLine.indexOf(DOUBLE_QUOTE + COMMA_CHAR) + 2, singleHandHistoryLine.indexOf('.'));
    timeString = timeString.replace("-", FORWARD_SLASH);
    timeString = timeString.replace(TIME_PREFIX, StringUtils.SPACE);
    String pokerstarsHandLine1 = POKER_STARS_HAND + handIdPrefix + handNumber + DOUBLE_POINT + StringUtils.SPACE + 
        gameType + '(' + DOLLAR_CHAR + smallBlindAmount + FORWARD_SLASH + 
        DOLLAR_CHAR + bigBlindAmount + " USD" + ')' + " - " + timeString + " CET" + System.lineSeparator();

    String convertedSingleHandHistory = pokerstarsHandLine1;

    String nextSingleHandHistoryLine = bufferedReader.readLine();
    while (nextSingleHandHistoryLine.contains("joined")) {
      
      nextSingleHandHistoryLine = bufferedReader.readLine();
    }
    
    int indexOfDealerPrefix = singleHandHistoryLine.indexOf(BUTTON_PREFIX);
    String buttonPlayerName = StringUtils.EMPTY;
    if (indexOfDealerPrefix >= 0) {
      buttonPlayerName = singleHandHistoryLine.substring(
          indexOfDealerPrefix + BUTTON_PREFIX_LENGTH,
          singleHandHistoryLine.indexOf(')', indexOfDealerPrefix));
      int indexOfDealerSeat = nextSingleHandHistoryLine.indexOf(buttonPlayerName) - 3;
      String buttonPlayerSeat = nextSingleHandHistoryLine.substring(indexOfDealerSeat, indexOfDealerSeat + 3).trim();
      lastButtonPlayerSeat = buttonPlayerSeat;
      lastButtonPlayerName = buttonPlayerName;
      convertedSingleHandHistory += TABLE_FIREFOX_10_MAX_SEAT + buttonPlayerSeat + " is the button" + System.lineSeparator();
    } else {
      convertedSingleHandHistory += TABLE_FIREFOX_10_MAX_SEAT + lastButtonPlayerSeat + " is the button" + System.lineSeparator();
    }
    
    String playerSummary = nextSingleHandHistoryLine.substring(
        nextSingleHandHistoryLine.indexOf(": #") + 2, nextSingleHandHistoryLine.indexOf(COMMA_CHAR) - 1);
    playerSummary = playerSummary.replace(" | ", System.lineSeparator());
    playerSummary = playerSummary.replace("#", "Seat ");
    playerSummary = playerSummary.replaceAll("Seat (\\d+(\\.\\d{2})?)", "Seat $1:");
    playerSummary = playerSummary.replace("(", "($");
    playerSummary = playerSummary.replace(")", " in Chips)");
        
    convertedSingleHandHistory += playerSummary + System.lineSeparator();
    
    singleHandHistoryLine = bufferedReader.readLine();

    yourHoleCards = StringUtils.EMPTY;
    if (this.readYourHoleCards && playerSummary.contains(yourUniqueName)
        && singleHandHistoryLine.contains(HOLE_CARD_PREFIX)) {
      
      int indexOfYourPlayerKey = playerSummary.indexOf(yourUniqueName);
      String yourSeatNumber = playerSummary.substring(indexOfYourPlayerKey - 4, indexOfYourPlayerKey - 2);
      yourSeatNumber = yourSeatNumber.replace(StringUtils.SPACE, "");
      
      this.yourSeat = "Seat " + yourSeatNumber;
      
      int indexOfHoleCardsStart = singleHandHistoryLine.indexOf(HOLE_CARD_PREFIX) + HOLE_CARD_PREFIX.length();
      int indexOfHoleCardsEnd = singleHandHistoryLine.indexOf(DOUBLE_QUOTE, indexOfHoleCardsStart);
      yourHoleCards = singleHandHistoryLine.substring(indexOfHoleCardsStart, indexOfHoleCardsEnd);
      yourHoleCards = ConversionUtils.replaceColorsAndTens(yourHoleCards);
      yourHoleCards = OPEN_BRACKET + yourHoleCards + CLOSED_BRACKET;
      
    }
    
    if (singleHandHistoryLine.contains(HOLE_CARD_PREFIX)) {
     
      singleHandHistoryLine = bufferedReader.readLine();
    }
    
    int indexOfSmallBlindPoster = singleHandHistoryLine.indexOf(SMALL_BLIND_PREFIX);
    String smallBlindLine = StringUtils.EMPTY;
    String smallBlindPlayerName = StringUtils.EMPTY;
    if (indexOfSmallBlindPoster >= 0) {
      singleHandHistoryLine = singleHandHistoryLine.replace(AND_GO_ALL_IN + StringUtils.SPACE, StringUtils.EMPTY);
      smallBlindPlayerName = singleHandHistoryLine.
          substring(0, indexOfSmallBlindPoster);
      smallBlindLine = singleHandHistoryLine.
          replaceAll(SMALL_BLIND_PREFIX + ONE_OR_MORE_DIGITS_GROUP_REGEX, ": posts small blind \\$" + FIRST_AND_SECOND_REGEX_GROUP_MATCH);
      smallBlindLine = ConversionUtils.stripDateAndEntryOrderFromCsv(smallBlindLine);
      
      lastRaiseAmountByPlayer.put(smallBlindPlayerName, smallBlindAmount);
    }
    singleHandHistoryLine = bufferedReader.readLine();
    singleHandHistoryLine = singleHandHistoryLine.replace(AND_GO_ALL_IN + StringUtils.SPACE, StringUtils.EMPTY);
    String bigBlindPlayerName = singleHandHistoryLine.
        substring(0, singleHandHistoryLine.indexOf(BIG_BLIND_PREFIX));
    String bigBlindLine = singleHandHistoryLine.
        replaceAll(BIG_BLIND_PREFIX + ONE_OR_MORE_DIGITS_GROUP_REGEX, ": posts big blind \\$" + FIRST_AND_SECOND_REGEX_GROUP_MATCH);
    bigBlindLine = ConversionUtils.stripDateAndEntryOrderFromCsv(bigBlindLine);
    
    lastRaiseAmountByPlayer.put(bigBlindPlayerName, bigBlindAmount);
    
    singleHandHistoryLine = bufferedReader.readLine();  
    
    String missingSmallBlindsLines = StringUtils.EMPTY;
    while (singleHandHistoryLine.contains(MISSING_SMALL_BLIND_PREFIX)) {

      singleHandHistoryLine = singleHandHistoryLine.replace(AND_GO_ALL_IN + StringUtils.SPACE, StringUtils.EMPTY);
      String missingSmallBlindLine = singleHandHistoryLine.
          replaceAll(MISSING_SMALL_BLIND_PREFIX + ONE_OR_MORE_DIGITS_GROUP_REGEX, ": posts the ante \\$" + FIRST_AND_SECOND_REGEX_GROUP_MATCH);

      missingSmallBlindLine = ConversionUtils.stripDateAndEntryOrderFromCsv(missingSmallBlindLine);

      missingSmallBlindsLines += missingSmallBlindLine + System.lineSeparator();
      
      singleHandHistoryLine = bufferedReader.readLine();
      
    }
    
    String missingBigBlindLine = StringUtils.EMPTY;
    if (singleHandHistoryLine.contains(MISSING_BIG_BLIND_PREFIX)) {

      singleHandHistoryLine = singleHandHistoryLine.replace(AND_GO_ALL_IN + StringUtils.SPACE, StringUtils.EMPTY);
      String missingBigBlindName = singleHandHistoryLine.
          substring(0, singleHandHistoryLine.indexOf(MISSING_BIG_BLIND_PREFIX));
      missingBigBlindLine = singleHandHistoryLine.
          replaceAll(MISSING_BIG_BLIND_PREFIX + ONE_OR_MORE_DIGITS_GROUP_REGEX, ": posts big blind \\$" + FIRST_AND_SECOND_REGEX_GROUP_MATCH);
      missingBigBlindLine = ConversionUtils.stripDateAndEntryOrderFromCsv(missingBigBlindLine);

      lastRaiseAmountByPlayer.put(missingBigBlindName, bigBlindAmount);
      
      singleHandHistoryLine = bufferedReader.readLine();  
    }
    
    String straddlePlayerName = StringUtils.EMPTY;
    String straddleLine = StringUtils.EMPTY;
    if (singleHandHistoryLine.contains(STRADDLE_PREFIX)) {

      singleHandHistoryLine = singleHandHistoryLine.replace(AND_GO_ALL_IN + StringUtils.SPACE, StringUtils.EMPTY);
      straddlePlayerName = singleHandHistoryLine.
          substring(0, singleHandHistoryLine.indexOf(STRADDLE_PREFIX));
      straddleLine = singleHandHistoryLine.
          replaceAll(" posts a straddle of " + ONE_OR_MORE_DIGITS_GROUP_REGEX, ": posts straddle \\$" + FIRST_AND_SECOND_REGEX_GROUP_MATCH);
      straddleLine = ConversionUtils.stripDateAndEntryOrderFromCsv(straddleLine);

      lastRaiseAmountByPlayer.put(straddlePlayerName, straddleAmount);
      latestBetTotalAmount = this.straddleAmount;
      
      singleHandHistoryLine = bufferedReader.readLine();  
    }

    if (!smallBlindLine.isEmpty()) {
    
      convertedSingleHandHistory += smallBlindLine + System.lineSeparator();
      
    }
    
    convertedSingleHandHistory += bigBlindLine + System.lineSeparator();
    
    if (!missingSmallBlindsLines.isEmpty()) {
      
      convertedSingleHandHistory += missingSmallBlindsLines;
    }
    
    if (!straddleLine.isEmpty()) {
    
      convertedSingleHandHistory += straddleLine + System.lineSeparator();
    }
    
    if (!missingBigBlindLine.isEmpty()) {
    
      convertedSingleHandHistory += missingBigBlindLine + System.lineSeparator();
    }  
    
    convertedSingleHandHistory += HOLE_CARDS;

    String endBoard = StringUtils.EMPTY;
    
    do {

      if (singleHandHistoryLine.contains(SHOWS_ACTION)) {
        
        convertedSingleHandHistory = handleShowAction(singleHandHistoryLine, convertedSingleHandHistory);
        
        singleHandHistoryLine = bufferedReader.readLine();
      
      } else if (!singleHandHistoryLine.contains(COLLECTED)) {
        
        String gameAction;
        
        if (singleHandHistoryLine.contains(BETS)) {
          
          gameAction = handleBetAction(singleHandHistoryLine);
        
        } else if (singleHandHistoryLine.contains(CALLS)) {
          
          gameAction = handleCallAction(singleHandHistoryLine);
        
        } else if (singleHandHistoryLine.contains(" check")) {
          
          gameAction = singleHandHistoryLine.replace(CHECKS, DOUBLE_POINT + CHECKS);
        
        } else if (singleHandHistoryLine.contains(FOLDS)) {
          
          gameAction = singleHandHistoryLine.replace(FOLDS, DOUBLE_POINT + FOLDS);
        
        } else if (singleHandHistoryLine.contains(RAISES_ACTION)) {

          gameAction = handleRaiseAction(singleHandHistoryLine);
        
        } else if (singleHandHistoryLine.contains("Uncalled bet of ")) {
          
          gameAction = singleHandHistoryLine.replaceFirst(DOUBLE_QUOTE + "Uncalled bet of " + ONE_OR_MORE_DIGITS_GROUP_REGEX,
              "Uncalled bet (\\$" + FIRST_AND_SECOND_REGEX_GROUP_MATCH + ")");
       
        } else {
          
          gameAction = handleNextBoardCard(singleHandHistoryLine);
        }
  
        gameAction = ConversionUtils.stripDateAndEntryOrderFromCsv(gameAction);
        
        endBoard = convertBoardInfos(endBoard, gameAction);

        if (!gameAction.isEmpty()) {
          convertedSingleHandHistory += gameAction + System.lineSeparator();
        }
        
        singleHandHistoryLine = bufferedReader.readLine();
      }
      
    } while (null != singleHandHistoryLine && !singleHandHistoryLine.contains(COLLECTED));
    
    if (this.readYourHoleCards && !this.yourHoleCards.isEmpty()) { 
      
      convertedSingleHandHistory += this.yourUniqueName + DOUBLE_POINT + 
          SHOWS_ACTION + yourHoleCards + System.lineSeparator();
    }
    
    convertedSingleHandHistory += SHOWDOWN;
    
    List<String> winningAmounts = new ArrayList<>();
    List<String> winningPlayers = new ArrayList<>();
    
    do {
      
      String showdownAction = singleHandHistoryLine.replace(" shows a", SHOWS_ACTION);
      showdownAction = ConversionUtils.replaceColorsAndTens(showdownAction);
    
      showdownAction = showdownAction.replaceAll(" collected " + ONE_OR_MORE_DIGITS_GROUP_REGEX, " collected \\$" + FIRST_AND_SECOND_REGEX_GROUP_MATCH);
      
      if (showdownAction.contains(COLLECTED)) {
       
        handleCollectedFromPotAction(winningAmounts, winningPlayers, showdownAction);
      
      } else if (showdownAction.contains(SHOWS_ACTION)) {
  
        showdownAction = handleShowCardsAction(showdownAction);
      }
      
      showdownAction = ConversionUtils.stripDateAndEntryOrderFromCsv(showdownAction);
      
      if (showdownAction.contains("(")) {
        
        showdownAction = showdownAction.substring(0, showdownAction.indexOf(" ("));
      }
      
      if (!showdownAction.trim().isEmpty()) {
        
        convertedSingleHandHistory += showdownAction + System.lineSeparator();
      
      }
      
      singleHandHistoryLine = bufferedReader.readLine();
      
    } while (null != singleHandHistoryLine && !singleHandHistoryLine.contains(HAND_END));
    
    convertedSingleHandHistory += SUMMARY;
    
    String gameSummary = createConvertedHandSummary(buttonPlayerName, playerSummary, smallBlindPlayerName,
        bigBlindPlayerName, straddlePlayerName, endBoard, winningAmounts, winningPlayers);
    
    convertedSingleHandHistory += gameSummary + System.lineSeparator() +
        System.lineSeparator() + System.lineSeparator() + System.lineSeparator();
    
    return convertedSingleHandHistory;
  }

  String readHandNumber(String singleHandHistoryLine) {

    return singleHandHistoryLine.substring(singleHandHistoryLine.indexOf(HAND_NUMBER_PREFIX_CHAR) + 1, 
        singleHandHistoryLine.indexOf("  ", singleHandHistoryLine.indexOf(HAND_NUMBER_PREFIX_CHAR)));
  }

  String createConvertedHandSummary(String buttonPlayerName, String playerSummary, String smallBlindPlayerName,

      String bigBlindPlayerName, String straddlePlayerName, String endBoard, List<String> winningAmounts,
      List<String> winningPlayers) {
    
    String gameSummary = "Total pot $" + calculateTotalPotFromWinnings(winningAmounts) + " | Rake $0" + System.lineSeparator();
    
    if (!endBoard.isEmpty()) {
      gameSummary += "Board " + endBoard + System.lineSeparator();
    }
    
    gameSummary += playerSummary.replaceAll("(.+) \\(.+\\)", "$1 folded");
    
    for (int n = 0; n < winningPlayers.size(); n++) {
      
      String winningPlayer = winningPlayers.get(n);
      gameSummary = gameSummary.replace(winningPlayer + " folded",
          winningPlayer + " collected (" + winningAmounts.get(n) + ")");
      gameSummary = gameSummary.replace(" showed  and won ", COLLECTED);
    }
    gameSummary = ConversionUtils.replaceTens(gameSummary);
    
    return gameSummary;
  }
  
  double calculateTotalPotFromWinnings(List<String> winningAmounts) {
    
    double totalPot = 0;
    
    for (String winningAmount : winningAmounts) {
    
      totalPot += Double.parseDouble(winningAmount.substring(1));
    }
    
    return totalPot;
  }

  String convertBoardInfos(String endBoard, String gameAction) {

    if (gameAction.contains(FLOP)) {
      endBoard = gameAction.replace(FLOP + StringUtils.SPACE, StringUtils.EMPTY);
      endBoard = endBoard.replace(OPEN_BRACKET, StringUtils.EMPTY);
      endBoard = endBoard.replace(CLOSED_BRACKET, StringUtils.EMPTY);
      endBoard = OPEN_BRACKET + endBoard + CLOSED_BRACKET;
      lastRaiseAmountByPlayer.clear();
      latestBetTotalAmount = 0;
    }

    if (gameAction.contains(TURN)) {
      endBoard = gameAction.replace(TURN + StringUtils.SPACE, StringUtils.EMPTY);
      endBoard = endBoard.replace(OPEN_BRACKET, StringUtils.EMPTY);
      endBoard = endBoard.replace(CLOSED_BRACKET, StringUtils.EMPTY);
      endBoard = OPEN_BRACKET + endBoard + CLOSED_BRACKET;
      lastRaiseAmountByPlayer.clear();
      latestBetTotalAmount = 0;
    }

    if (gameAction.contains(RIVER)) {
      endBoard = gameAction.replace(RIVER + StringUtils.SPACE, StringUtils.EMPTY);
      endBoard = endBoard.replace(OPEN_BRACKET, StringUtils.EMPTY);
      endBoard = endBoard.replace(CLOSED_BRACKET, StringUtils.EMPTY);
      endBoard = OPEN_BRACKET + endBoard + CLOSED_BRACKET;
      lastRaiseAmountByPlayer.clear();
      latestBetTotalAmount = 0;
    }

    return endBoard;
  }

  String handleBetAction(String singleHandHistoryLine) {
    String gameAction;
    String betAmount = singleHandHistoryLine.replaceAll(ONE_OR_MORE_CHARACTERS + BETS + ONE_OR_MORE_DIGITS_AND_CHARS_REGEX, FIRST_AND_SECOND_REGEX_GROUP_MATCH);
    String bettingPlayer = singleHandHistoryLine.substring(0, singleHandHistoryLine.indexOf(BETS));

    latestBetTotalAmount = Double.parseDouble(betAmount);
    lastRaiseAmountByPlayer.put(bettingPlayer, latestBetTotalAmount);
    
    gameAction = singleHandHistoryLine.replace(BETS, DOUBLE_POINT +  BETS + DOLLAR_CHAR);
    gameAction = gameAction.replace(AND_GO_ALL_IN, StringUtils.EMPTY);
    return gameAction;
  }

  String handleShowAction(String singleHandHistoryLine, String convertedSingleHandHistory) {
    String showdownAction = singleHandHistoryLine.replace(SHOWS_ACTION + "a ", SHOWS_ACTION);
    showdownAction = showdownAction.replace(COMMA_CHAR + StringUtils.SPACE, StringUtils.SPACE);
    showdownAction = showdownAction.replace(PokernowConstants.SPADES, PokerstarsConstants.SPADES);
    showdownAction = showdownAction.replace(PokernowConstants.HEARTS, PokerstarsConstants.HEARTS);
    showdownAction = showdownAction.replace(PokernowConstants.CLUBS, PokerstarsConstants.CLUBS);
    showdownAction = showdownAction.replace(PokernowConstants.DIAMONDS, PokerstarsConstants.DIAMONDS);
 
    showdownAction = handleShowCardsAction(showdownAction);
    
    showdownAction = ConversionUtils.stripDateAndEntryOrderFromCsv(showdownAction);
    
    if (!showdownAction.trim().isEmpty()) {
      convertedSingleHandHistory += showdownAction + System.lineSeparator();
    }
    return convertedSingleHandHistory;
  }

  String handleShowCardsAction(String showdownAction) {
    String showedHand;
    showedHand = showdownAction.substring(
        showdownAction.indexOf(SHOWS_ACTION) + SHOWS_ACTION.length(),
        showdownAction.indexOf('.'));
     showedHand = OPEN_BRACKET + showedHand + CLOSED_BRACKET;
     
     showdownAction = showdownAction.substring(0, showdownAction.indexOf(SHOWS_ACTION) + SHOWS_ACTION.length()) +
         showedHand + DOUBLE_QUOTE;
     showdownAction = showdownAction.replace(SHOWS_ACTION, DOUBLE_POINT + SHOWS_ACTION);
     showdownAction = ConversionUtils.replaceTens(showdownAction);
    return showdownAction;
  }

  void handleCollectedFromPotAction(List<String> winningAmounts, List<String> winningPlayers, String showdownAction) {
    String winningPlayer = showdownAction.substring(0, showdownAction.indexOf(COLLECTED));
    
    int indexOfWinningPlayer = winningPlayers.size();
    
    if (winningPlayers.contains(winningPlayer)) {

      indexOfWinningPlayer = winningPlayers.indexOf(winningPlayer);
    
    } else {
 
      winningPlayers.add(winningPlayer);
    }
    
    String winningAmount = showdownAction.substring(
        showdownAction.indexOf(COLLECTED) + COLLECTED.length(),
        showdownAction.indexOf(StringUtils.SPACE, showdownAction.indexOf(COLLECTED) + COLLECTED.length()));
    
    if (indexOfWinningPlayer == winningAmounts.size()) {
    
      winningAmounts.add(winningAmount);
    
    } else {
      // Cumulate Wins from run it twice or hi/lo pots
      String existingWinningAmountString = winningAmounts.get(indexOfWinningPlayer).substring(1);
      double existingWinningAmount = Double.parseDouble(existingWinningAmountString);
      
      String newWinningAmountString = winningAmount.substring(1);
      double newWinningAmount = Double.parseDouble(newWinningAmountString);
      
      winningAmounts.set(indexOfWinningPlayer,
          DOLLAR_SIGN + String.valueOf(existingWinningAmount + newWinningAmount));
    }
  }

  String handleNextBoardCard(String singleHandHistoryLine) {

    String gameAction;
    gameAction = singleHandHistoryLine.replaceAll("\"[F|f]lop( \\(second run\\))?:", FLOP + FIRST_REGEX_GROUP_MATCH);
    gameAction = gameAction.replaceAll("\"[T|t]urn( \\(second run\\))?:", TURN + FIRST_REGEX_GROUP_MATCH);
    gameAction = gameAction.replaceAll("\"[R|r]iver( \\(second run\\))?:", RIVER + FIRST_REGEX_GROUP_MATCH);
    gameAction = ConversionUtils.replaceColorsAndTens(gameAction);
    
    if (gameAction.contains(FLOP) || gameAction.contains(TURN) || gameAction.contains(RIVER)) {

      lastRaiseAmountByPlayer.clear();
    }
    
    return gameAction;
  }

  String handleRaiseAction(String singleHandHistoryLine) {
    String gameAction;
    String raiseTotal = singleHandHistoryLine.replaceAll(ONE_OR_MORE_CHARACTERS + RAISES_TO + ONE_OR_MORE_DIGITS_AND_CHARS_REGEX, FIRST_AND_SECOND_REGEX_GROUP_MATCH);

    String raisingPlayer = singleHandHistoryLine.substring(0, singleHandHistoryLine.indexOf(RAISES_TO));
    
    double raiseAmount = Double.parseDouble(raiseTotal) - latestBetTotalAmount;
    double roundedRaiseAmount = roundToCents(raiseAmount);
    latestBetTotalAmount = Double.parseDouble(raiseTotal);

    if (lastRaiseAmountByPlayer.containsKey(raisingPlayer)) {
      lastRaiseAmountByPlayer.remove(raisingPlayer);
    }
    lastRaiseAmountByPlayer.put(raisingPlayer, latestBetTotalAmount);
    
    gameAction = singleHandHistoryLine.replace(RAISES_ACTION, DOUBLE_POINT + RAISES_ACTION);
    String replacement = RAISES_ACTION + DOLLAR_CHAR + roundedRaiseAmount + " to $";
    gameAction = gameAction.replace(RAISES_TO, replacement);
    gameAction = gameAction.replace(AND_GO_ALL_IN, StringUtils.EMPTY);
    return gameAction;
  }

  String handleCallAction(String singleHandHistoryLine) {
    String gameAction;
    String callTotal = singleHandHistoryLine.replaceAll(ONE_OR_MORE_CHARACTERS + CALLS + ONE_OR_MORE_DIGITS_AND_CHARS_REGEX, FIRST_AND_SECOND_REGEX_GROUP_MATCH);
    String callingPlayer = singleHandHistoryLine.substring(0, singleHandHistoryLine.indexOf(CALLS));
    double callTotalAmount = Double.parseDouble(callTotal);
    
    gameAction = singleHandHistoryLine;
    if (lastRaiseAmountByPlayer.containsKey(callingPlayer)) {
      
      double callPart = callTotalAmount - lastRaiseAmountByPlayer.get(callingPlayer);
      double roundedCallPart = roundToCents(callPart);
      gameAction = singleHandHistoryLine.replaceFirst(CALLS + ONE_OR_MORE_DIGITS_REGEX, CALLS + roundedCallPart);
    }
    
    lastRaiseAmountByPlayer.remove(callingPlayer);
    lastRaiseAmountByPlayer.put(callingPlayer, callTotalAmount);
    
    gameAction = gameAction.replace(CALLS, DOUBLE_POINT + CALLS + DOLLAR_CHAR);
    gameAction = gameAction.replace(AND_GO_ALL_IN, StringUtils.EMPTY);
    return gameAction;
  }

  double roundToCents(double calculatedDouble) {
    
    return Math.round(100 * calculatedDouble) / 100.0;
  }
  
  public boolean isReadYourHoleCards() {
    return readYourHoleCards;
  }

  public void setReadYourHoleCards(boolean readYourHoleCards) {
    this.readYourHoleCards = readYourHoleCards;
  }

  public String getYourUniqueName() {
    return yourUniqueName;
  }

  public void setYourUniqueName(String yourUniqueName) {
    this.yourUniqueName = yourUniqueName;
  }
}
