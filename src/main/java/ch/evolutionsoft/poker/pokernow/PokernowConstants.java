package ch.evolutionsoft.poker.pokernow;

public final class PokernowConstants {
  
  private PokernowConstants() {
    // Hide constructor
  }

  public static final String OMAHA_GAME_TYPE = "Pot Limit Omaha Hi";
  public static final String OMAHA_HI_LO_GAME_TYPE = "Pot Limit Omaha Hi/Lo";
  public static final String TEXAS_GAME_TYPE = "No Limit Texas Hold'em";

  public static final String STARTING_HAND = "-- starting hand #";
  public static final String ENDING_HAND = "-- ending hand #";
  public static final String INTRO_TEXT_END_2 = "\"Player stacks: ";
  
  public static final String HOLE_CARD_PREFIX = "Your hand is ";
  
  public static final String PLAYER_ID_PREFIX = " @ "; 
  public static final int PLAYER_ID_PREFIX_LENGTH = PLAYER_ID_PREFIX.length(); 
  
  public static final String HAND_START = "-- starting hand #";
  public static final int HAND_START_LENGTH = HAND_START.length();
  
  public static final String HAND_END = "-- ending hand";
  
  public static final String BUTTON_PREFIX = "dealer: ";
  public static final int BUTTON_PREFIX_LENGTH = BUTTON_PREFIX.length();

  public static final String ANTE_PREFIX = " posts an ante of ";
  public static final String SMALL_BLIND_PREFIX = " posts a small blind of ";
  public static final String BIG_BLIND_PREFIX = " posts a big blind of ";
  public static final String STRADDLE_PREFIX = " posts a straddle of";
  public static final String MISSING_SMALL_BLIND_PREFIX = " posts a missing small blind of ";
  public static final String MISSING_BIG_BLIND_PREFIX = " posts a missed big blind of "; 
  
  public static final String SPADES = "♠";
  public static final String HEARTS = "♥";
  public static final String CLUBS = "♣";
  public static final String DIAMONDS = "♦";

  public static final String POKERNOW_DATETIME_PATTERN = ".+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}).+";

  public static final char SINGLE_QUOTE_CHAR = '"';
  public static final String DOUBLE_QUOTE = "\"";
  public static final String TRIPLE_DOUBLE_QUOTE = "\"\"\"";
  public static final String DOUBLE_DOUBLE_QUOTE = "\"\"";
  
  public static final String TIME_PREFIX = "T";
  
}
