package ch.evolutionsoft.poker.pokernow;

public final class ConversionConstats {

  private ConversionConstats() {
    // Hide constructor
  }

  public static final String TABLE_FIREFOX_10_MAX_SEAT = "Table 'Poker Now' 10-max Seat ";
  public static final String AND_GO_ALL_IN = " and go all in";
  public static final char HAND_NUMBER_PREFIX_CHAR = '#';
  public static final String FORWARD_SLASH = "/";
  public static final String ONE_OR_MORE_CHARACTERS = ".+";
  public static final String ONE_OR_MORE_DIGITS_REGEX = "\\d+(\\.\\d{1,2})?";
  public static final String ONE_OR_MORE_DIGITS_GROUP_REGEX = "(\\d+)(\\.\\d{1,2})?";
  public static final String ONE_OR_MORE_DIGITS_AND_CHARS_REGEX = "(\\d+)(\\.\\d{1,2})?( and go all in)?\".+";
  public static final String NEWLINE = "\n";
  public static final String DOUBLE_POINT = ":";
  public static final String FIRST_REGEX_GROUP_MATCH = "$1";
  public static final String FIRST_AND_SECOND_REGEX_GROUP_MATCH = "$1$2";
  public static final char COMMA_CHAR = ',';
  public static final String CLOSED_BRACKET = "]";
  public static final String OPEN_BRACKET = "[";

  public static final String AMOUNT_DOUBLE_FORMAT = "%.1f";

  public static final String DOLLAR_SIGN = "$";

  public static final String CURRENCY_AMOUNT_REGEX = "\\$" + ONE_OR_MORE_DIGITS_REGEX;
  public static final String ESCAPED_CURRENCY = "\\$";

  public static final int SMALL_CAPACITY = 9;
}
