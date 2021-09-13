/* The Hand class represents a Player's hand while participating in a Game of Texas Hold 'em
It includes methods to return the player's opening (two-card) hand, their complete hand (including community cards),
as well as methods that rank the hand according to the Texas Hold 'em hierarchy (e.g. two pair > two of a kind) */
import java.util.*;


public class Hand {
    // Represents a hand of cards
    private ArrayList<Card> cards;

    // The first two cards in hand
    private Card[] openingHand;

    // A TreeMap is used to count rank frequency
    private TreeMap<Integer, Integer> hashedHand;

    // Constructor
    public Hand() {
        cards = new ArrayList<>();
        openingHand = new Card[2];

        // Sort the TreeMap in reverse to access high cards first
        // A TreeMap is used instead of a HashMap mostly for this purpose
        hashedHand = new TreeMap<>(Collections.reverseOrder());
    }

    // Getter methods
    public ArrayList<Card> getHand() {
        return cards;
    }

    // Returns the pre-flop hand
    public Card[] getOpeningHand() {
        // If the hand hasn't been determined yet
        if (openingHand[0] == null) {
            openingHand[0] = cards.get(0);
            openingHand[1] = cards.get(1);

            // Reverse sort --> order is same as firstRoundLookupTable
            if (openingHand[0].getRank() < openingHand[1].getRank()) {
                // Create new cards to avoid confusing pointers
                Card largeCard = new Card(openingHand[1]);
                openingHand[1] = new Card(openingHand[0]);
                openingHand[0] = largeCard;
            }
        }

        return openingHand;
    }

    public int getSize() {
        return cards.size();
    }

    // Setter methods
    public void newHand() {
        cards = new ArrayList<>();
        openingHand = new Card[2];
        hashedHand = new TreeMap<>(Collections.reverseOrder());
    }

    // Add a card to hand + hash it
    public void addCard(Card card) {
        cards.add(card);

        // Increment rank's value (create a new entry with val = 1 if it doesn't exist yet)
        // https://stackoverflow.com/questions/81346/most-efficient-way-to-increment-a-map-value-in-java
        hashedHand.merge(card.getRank(), 1, Integer::sum);
    }

    public void remCard(Card card) {
        cards.remove(card);

        // Safe decrement
        int rank = card.getRank();
        int curValue = hashedHand.get(rank);
        hashedHand.put(card.getRank(), curValue-1);
    }

    public void addCards(ArrayList<Card> combo) {
        // Can't simply use cards.addAll because that wouldn't hash them
        for (Card card : combo) {
            addCard(card);
        }
    }

    public void remCards(ArrayList<Card> combo) {
        // Can't simply use cards.removeAll for the same reason as above
        for (Card card : combo) {
            remCard(card);
        }
    }

    /* Generates a comparison score for a Texas Hold 'em hand, based mostly on this website's guidelines:
    https://towardsdatascience.com/poker-with-python-how-to-score-all-hands-in-texas-holdem-6fd750ef73d */
    public double getScore() {
        // Sort enables hand evaluation
        cards = reverseQuickSort(cards);

        // Set flush = true, because we're looking for a straight flush (the highest hand)
        int straightFlush = getStraight(getSize() - 1, 1, true);

        if (straightFlush != 0) {
            return 120 + straightFlush;
        }

        // Set excluded to 0 since this isn't a full house check
        int fourOfAKind = getDuplicates(4, 0);

        if (fourOfAKind != 0) {
            // Four of a kind ties are always broken by a single high card...
            return 105 + fourOfAKind + highCardScore(fourOfAKind, 1);
        }

        int[] fullHouse = getFullHouse();

        // If either == 0, full house isn't complete
        if (fullHouse[0] != 0 && fullHouse[1] != 0) {
            // Cast the two-of-a-kind to a double, enable division
            return 90 + fullHouse[0] + (double) fullHouse[1] / 100;
        }

        String flush = getFlush();

        // flush = "" --> false
        if (Boolean.parseBoolean(flush)) {
            return 75 + highCardScore(flush);
        }

        // Set flush = false, because we already know the hand doesn't contain a flush
        int straight = getStraight(getSize() - 1, 1, false);

        if (straight != 0) {
            return 60 + straight;
        }

        int threeOfAKind = getDuplicates(3, 0);

        if (threeOfAKind != 0) {
            // Set numCards = HAND_SIZE - 3, as that's the group of high cards left in hand
            return 45 + threeOfAKind + highCardScore(threeOfAKind, Constants.HAND_SIZE - 3);
        }

        int[] twoPair = getTwoPair();

        if (twoPair[1] != 0) {
            // Divide by 10^2, 10^4 to guarantee that first pair > second pair > high card
            return 30 + twoPair[0] + (double) twoPair[1] / 100 + (double) twoPair[2] / 10000;
        }

        int twoOfAKind = getDuplicates(2, 0);

        if (twoOfAKind != 0) {
            return 15 + twoOfAKind + highCardScore(twoOfAKind, Constants.HAND_SIZE - 2);
        }

        // If there's no ranked hands, use high card
        return highCardScore(0, 5);
    }

    // Displays hand rank
    public String bestHand() {
        double score = getScore();

        // Used if/else instead of switch because it increases readability
        if (score == 134) {
            // Three exclamation points since it's really rare
            return "Royal Flush!!!";
        }

        else if (score > 120) {
            return "Straight Flush!";
        }

        else if (score > 105) {
            return "Four of a Kind!";
        }

        else if (score > 90) {
            return "Full House!";
        }

        else if (score > 75) {
            return "Flush!";
        }

        else if (score > 60) {
            return "Straight!";
        }

        else if (score > 45) {
            return "Three of a Kind!";
        }

        else if (score > 30) {
            return "Two Pair!";
        }

        else if (score > 15) {
            return "Two of a Kind!";
        }

        // No exclamation point since high card sucks
        return "High Card";
    }

    // Returns the hand's unique cards as a String
    public String toString() {
        // Calling getOpeningHand() ensures that the variable isn't empty
        return Arrays.toString(getOpeningHand());
    }

    // These private methods decide hand rank (e.g. a pair of kings, full house of sixes and twos)

    /* Returns the rank of the highest duplicate (e.g. four of a kind), 0 if not enough dupes exist
    This method assumes that hashHand is reverse sorted by key --> multiple calls are more efficient

    Parameter numDupes: How many cards of the same "kind" are needed? (i.e. 2, 3, or 4)
    Parameter excluded: cards with this rank aren't considered (helps for full house) */
    private int getDuplicates(int numDupes, int excluded) {
        // Iterate, from highest to lowest key
        for (int rank : hashedHand.keySet()) {
            // Use hashedHand.get(rank) to determine the rank frequency
            if (rank != excluded && hashedHand.get(rank) == numDupes) {
                return rank;
            }
        }

        return 0;
    }

    /* Returns an array in the form of: {high pair, low pair, high card}
    Returns an array in the form of {x, 0} if there's not enough pairs */
    private int[] getTwoPair() {
        int[] pairs = {0, 0, 0};

        for (int rank: hashedHand.keySet()) {
            if (hashedHand.get(rank) >= 2) {
                // Update pair levels
                if (rank > pairs[0]) {
                    pairs[1] = pairs[0];
                    pairs[0] = rank;
                }

                else if (rank > pairs[1]) {
                    // Update the high card if applicable
                    pairs[2] = Math.max(pairs[1], pairs[2]);
                    pairs[1] = rank;
                }
            }

            // Update the high card
            else if (rank > pairs[2]) {
                pairs[2] = rank;
            }
        }

        return pairs;
    }

    // Returns an array of the highest full house, includes a 0 if a part is missing
    private int[] getFullHouse() {
        int[] fullHouse = {0, 0};

        // Inefficient, but clean search for a big full house
        fullHouse[0] = getDuplicates(3, 0);
        fullHouse[1] = getDuplicates(2, fullHouse[0]);

        return fullHouse;
    }

    /* Returns the first index of a straight (five in a row), zero (false) if none exists
    Recurs until: a straight is identified OR there is no more possibility of one existing

    Parameter curIndex: The index of the highest card in the straight being currently checked
    Parameter numInStraight: The number of cards included in the current straight (starts at 1)
    Parameter flush: Check for a straight flush (straight of one suit) instead of a straight */
    private int getStraight(int curIndex, int numInStraight, boolean flush) {
        // If there's enough cards in sequential order...
        if (numInStraight == Constants.HAND_SIZE) {
            return curIndex;
        }

        // Not enough cards left for a straight...
        if (curIndex < Constants.HAND_SIZE - 1) {
            return 0;
        }

        /* Is it inefficient to create two new Card instances each iteration? Yes
        But it's also clean, concise, and not prohibitively expensive, so... */
        Card curCard = cards.get(curIndex - numInStraight);

        // The function descends to find the highest straight first --> subtract 1
        Card nextCard = cards.get(curIndex - numInStraight - 1);

        // The straight descends --> cur == next + 1
        if (curCard.getRank() == nextCard.getRank() + 1) {
            // In a royal flush, all card suits must be the same
            if (!flush || curCard.getSuit() == nextCard.getSuit()) {
                return getStraight(curIndex, numInStraight + 1, flush);
            }
        }

        // Start checking for a new straight: reset curIndex, numInStraight
        return getStraight(curIndex - 1, 1, flush);
    }

    /* Returns the suit of a flush (five of the same suit), zero (false) if none exists
    With runtime O(n + 4), this method isn't the absolute best, but it's good enough */
    private String getFlush() {
        // Create a HashMap that will store each suit and its frequency
        HashMap<String, Integer> suits = new HashMap<>();

        for (Card card : cards) {
            suits.merge(card.getSuitAsString(), 1, Integer::sum);
        }

        for (String suit : suits.keySet()) {
            if (suits.get(suit) >= Constants.HAND_SIZE) {
                return suit;
            }
        }

        return "";
    }

    /* Returns score based on the Towards Data Science guide linked above
    Parameter excluded: ranks not considered for high card, typically the value of the duplicate
    Parameter numCards: The max number of high cards needed to beak a tie (e.g. two-of-a-kind needs 3 cards) */
    private double highCardScore(int excluded, int numCards) {
        double score = 0;
        int i = getSize() - 1;

        while (numCards > 0 && i > 0) {
            int rank = cards.get(i).getRank();

            if (rank != excluded) {
                // Math.pow ensures that early high cards trump later ones
                score += rank / Math.pow(100, numCards);

                numCards--;
            }

            i--;
        }

        return score;
    }

    // Overloaded method for flushes
    private double highCardScore(String flush) {
        double score = 0;
        int i = getSize() - 1;

        // Minimum cards in a flush
        int numCards = Constants.HAND_SIZE;

        while (numCards > 0 && i > 0) {
            Card curCard = cards.get(i);

            if (curCard.getSuitAsString().equals(flush)) {
                // Math.pow ensures that early high cards trump later ones
                score += curCard.getRank() / Math.pow(100, numCards);

                numCards--;
            }

            i--;
        }

        return score;
    }

    /* This quick sort uses the last card as a pivot and partitions around it
    Using built-in Collections.sort would have been faster, but this is cooler */
    private ArrayList<Card> reverseQuickSort(ArrayList<Card> input) {
        // 1 card left --> sorted
        if (input.size() < 2)
            return input;

        // Arbitrarily use the last card as pivot
        Card pivot = input.get(input.size() - 1);

        // Smaller = cards with lower ranks than pivot
        // Larger = cards with higher ranks than pivot
        ArrayList<Card> small = new ArrayList<>();
        ArrayList<Card> large = new ArrayList<>();

        // Partition each card into the correct list
        for (int i = 0; i < input.size() - 1; i++) {
            Card cur = input.get(i);

            if (cur.getRank() < pivot.getRank())
                small.add(cur);

            else
                large.add(cur);
        }

        // Recursively sort each list
        input = new ArrayList<>();

        // Sort large first, so it's reversed
        input.addAll(reverseQuickSort(large));

        input.add(pivot);

        input.addAll(reverseQuickSort(small));

        return input;
    }
}
