/* The Constants file serves as a control center for the rest of the module,
as it includes constants, variables, and methods used throughout all the other classes */
import java.util.*;
import static java.util.Map.entry;


public final class Constants {
    /* These constants control typing speed
    They represent sleep times in milliseconds */
    public static final int CHAR_DELAY = 50;
    public static final int PUNCTUATION_DELAY = 250;
    public static final int NEW_LINE_DELAY = 500;

    // These constants control game mechanics:
    public static final int NUM_OPPONENTS = 4;
    public static final int BIG_BLIND = 5;
    public static final int SMALL_BLIND = 2;

    // Number of cards dealt to the community each turn
    public static final int[] COMMUNITY_CARDS = {3, 1, 1};

    // Used to check scoring probabilities in the Player class
    public static final Deck SAMPLE_DECK = new Deck(Constants.NUM_SAMPLE_DECKS);

    // The number of cards in a player's best hand
    public static final int HAND_SIZE = 5;

    // Individual cards + community = 7 cards total
    public static final int TOTAL_CARDS = 7;

    // Should be 6-8, set to 1 for simplicity
    public static final int NUM_CASINO_DECKS = 1;

    // The number of decks necessary for a probability sample
    public static final int NUM_SAMPLE_DECKS = 1;

    // These constants control each player's bankroll
    public static final int PLAYER_STARTING_BANK = 100;
    public static final int MIN_OPP_BANKROLL = 80;
    public static final int MAX_OPP_BANKROLL = 120;

    // The following constants control AI behavior:
    // AI bets during pre-flop + post-flop bluffs
    public static final int AI_RAISE = 5;

    // How often should the AI randomly bluff?
    public static final double AI_BLUFF = .25;

    // The following are utility constants/variables, used to supplement other class methods:
    // Associated values of face-cards
    public static final int JACK = 11;
    public static final int QUEEN = 12;
    public static final int KING = 13;
    public static final int ACE = 14;

    // Associated indices of suits
    public static final int CLUBS = 0;
    public static final int DIAMONDS = 1;
    public static final int HEARTS = 2;
    public static final int SPADES = 3;

    // These array allows us to cast a Card's integer attributes to their string equivalents
    public static final String[] RANKS = {"X", "X", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                                          "Jack", "Queen", "King", "Ace"};
    public static final String[] SUITS = {"Clubs", "Diamonds", "Hearts", "Spades"};

    // Global scanner, gets the user's name, bets, etc.
    public static final Scanner SCAN = new Scanner(System.in);

    /* List of AI player names used to randomly create opponents
    Variable is not final because .remove() is a convenient way to avoid repeating names
    Start with an array because it's easier to cast that to a list than to add each name individually
    Names mostly taken from this website: https://www.beatthefish.com/poker-players/poker-player-nicknames/ */
    public static ArrayList<String> NAMES = new ArrayList<> (Arrays.asList("Anne", "Antonio", "Barry", "Bobby", "Brian",
            "Carlos", "Chad", "Chris", "Dan", "Darrell", "Dave", "Erick", "E-Dog", "Greg", "Howard", "Hoyt", "Humberto",
            "Jack", "Jimmy", "John", "Kenny", "Lady Linda", "Marcel", "Mike", "Paul", "Phil", "Randy", "Scott", "Stu",
            "Tommy", "Viktor", "Walter"));

    /* Mathematically optimal lookup table which the AI uses to raise, match, or fold during the ante phase
    Each key represents an opening hand, and each value represents decisions for: {same suit, different suit}
    The lookup table is taken from here: https://steamcommunity.com/sharedfiles/filedetails/?id=142456801 */
    public static final Map<List<Integer>, String[]> preFlopLookupTable = Map.<List<Integer>, String[]>ofEntries(
            entry(Arrays.asList(ACE, ACE), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(ACE, KING), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(ACE, QUEEN), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(ACE, JACK), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(ACE, 10), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(ACE, 9), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(ACE, 8), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(ACE, 7), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(ACE, 6), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(ACE, 5), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(ACE, 4), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(ACE, 3), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(ACE, 2), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(KING, KING), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(KING, QUEEN), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(KING, JACK), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(KING, 10), new String[]{"RAISE", "CALL"}),
            entry(Arrays.asList(KING, 9), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(KING, 8), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(KING, 7), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(KING, 6), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(KING, 5), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(KING, 4), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(KING, 3), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(KING, 2), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(QUEEN, QUEEN), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(QUEEN, JACK), new String[]{"RAISE", "CALL"}),
            entry(Arrays.asList(QUEEN, 10), new String[]{"RAISE", "CALL"}),
            entry(Arrays.asList(QUEEN, 9), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(QUEEN, 8), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(QUEEN, 7), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(QUEEN, 6), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(QUEEN, 5), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(QUEEN, 4), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(QUEEN, 3), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(QUEEN, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(JACK, JACK), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(JACK, 10), new String[]{"RAISE", "CALL"}),
            entry(Arrays.asList(JACK, 9), new String[]{"RAISE", "CALL"}),
            entry(Arrays.asList(JACK, 8), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(JACK, 7), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(JACK, 6), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(JACK, 5), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(JACK, 4), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(JACK, 3), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(JACK, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(10, 10), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(10, 9), new String[]{"RAISE", "CALL"}),
            entry(Arrays.asList(10, 8), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(10, 7), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(10, 6), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(10, 5), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(10, 4), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(10, 3), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(10, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(9, 9), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(9, 8), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(9, 7), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(9, 6), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(9, 5), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(9, 4), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(9, 3), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(9, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(8, 8), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(8, 7), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(8, 6), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(8, 5), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(8, 4), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(8, 3), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(8, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(7, 7), new String[]{"RAISE", "RAISE"}),
            entry(Arrays.asList(7, 6), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(7, 5), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(7, 4), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(7, 3), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(7, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(6, 6), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(6, 5), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(6, 4), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(6, 3), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(6, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(5, 5), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(5, 4), new String[]{"CALL", "FOLD"}),
            entry(Arrays.asList(5, 3), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(5, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(4, 4), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(4, 3), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(4, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(3, 3), new String[]{"CALL", "CALL"}),
            entry(Arrays.asList(3, 2), new String[]{"FOLD", "FOLD"}),
            entry(Arrays.asList(2, 2), new String[]{"CALL", "CALL"})
    );

    // The following are utility methods used in multiple other files
    // Ensures a viable user response to multiple-choice questions
    public static String getResponse(String optOne, String optTwo) {
        String response = SCAN.next();

        while (!response.equalsIgnoreCase(optOne) && !response.equalsIgnoreCase(optTwo)) {
            // Clear scanning cache
            SCAN.nextLine();

            typeText("You didn't pick " + optOne + " or " + optTwo + "! Please try again: ");
            response = SCAN.next();
        }

        return response;
    }

    // Types out game text slowly for style
    public static void typeText(String text) {
        for (char c : text.toCharArray()) {
            System.out.print(c);

            // Spaces move at same speed as letters
            if (Character.isLetter(c) || c == ' ') {
                delay(CHAR_DELAY);
            }

            else {
                delay(PUNCTUATION_DELAY);
            }
        }

        delay(NEW_LINE_DELAY);
    }

    // Shorthand for handled sleep()
    public static void delay(int time) {
        try {
            Thread.sleep(time);
        }

        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
