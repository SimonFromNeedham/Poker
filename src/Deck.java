// The Deck class represents a full deck of playing Cards, and includes the ability to be shuffled
import java.util.ArrayList;


public class Deck {
    // Represents a deck of cards
    private final ArrayList<Card> deck;

    // Constructor
    public Deck(int num_decks) {
        deck = new ArrayList<>();

        // Use multiple decks, like a casino
        for (int i = 0; i < num_decks; i++) {
            // Loops add each unique card to the deck
            for (int rank = 2; rank <= Constants.ACE; rank++) {
                for (int suit = 0; suit <= Constants.SPADES; suit++) {
                    deck.add(new Card(rank, suit));
                }
            }
        }
    }

    // Getter methods
    public ArrayList<Card> getDeck() {
        return deck;
    }

    // Deals a card
    public Card deal() {
        return deck.remove(0);
    }

    // Burns multiple cards
    public void burn(int amount) {
            deck.subList(0, amount).clear();
    }

    // Shuffles deck
    public void shuffle() {
        int numCards = deck.size();
        for (int i = 0; i < numCards; i++)
            swap(i, (int)(Math.random() * numCards));
    }

    // Swaps two cards, helper for shuffle method
    private void swap (int i, int j) {
        // Create new cards to avoid confusing pointers
        Card current = new Card(deck.get(i));
        deck.set(i, new Card(deck.get(j)));
        deck.set(j, current);
    }
}
