// The Card class represents a standard playing card, including a rank, suit, and a bunch of useful methods


public class Card {
    // Value of card
    private final int rank;

    // Suit of card
    private final int suit;

    // Constructor
    public Card(int rank, int suit) {
        this.rank = rank;
        this.suit = suit;
    }

    // Second constructor, used to copy cards
    public Card(Card copy) {
        rank = copy.getRank();
        suit = copy.getSuit();
    }

    // Getter methods
    public int getRank() {
        return rank;
    }

    public int getSuit() {
        return suit;
    }

    public String getRankAsString() {
        return Constants.RANKS[rank];
    }

    public String getSuitAsString() {
        return Constants.SUITS[suit];
    }

    // Returns name of card (i.e. "Jack of Hearts")
    public String toString() {
        return getRankAsString() + " of " + getSuitAsString();
    }
}
