/* The Player class represents both AI and real (user) players that participate in the Game
It includes methods which determine the AI's moves, and the Scanning framework for the real player to make decisions */
import java.util.ArrayList;
import java.util.List;


public class Player extends Hand {
    // Immutable player characteristic
    private final boolean isHuman;

    // Changes if a player has a special bankroll (i.e. Lil' Tommy starts with fewer chips)
    private String name;

    // Changes frequently throughout each round
    private int bankroll;
    private int bet;

    // Changes once every round
    private boolean hasFolded;

    // If a player went all in but couldn't match the bet, they can only win a side pot Defaults to false
    private boolean canOnlyWinSidePot;

    // Constructor
    public Player(String name, int bankroll, boolean isHuman) {
        super();

        this.name = name;
        this.bankroll = bankroll;
        this.isHuman = isHuman;

        bet = 0;
        hasFolded = false;
        canOnlyWinSidePot = false;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public int getBankroll() {
        return bankroll;
    }

    public int getBet() {
        return bet;
    }

    public boolean isHuman() {
        return isHuman;
    }

    public boolean hasFolded() {
        return hasFolded;
    }

    public boolean canOnlyWinSidePot() {
        return canOnlyWinSidePot;
    }

    // Setter methods
    public void setName(String name) {
        this.name = name;
    }

    public void setBankroll(int bankroll) {
        this.bankroll = bankroll;
    }

    // Returning "amount" makes it much easier to update the Game pot
    public int addToBankroll(int amount) {
        bankroll += amount;

        return amount;
    }

    public int subFromBankroll(int amount) {
        bankroll -= amount;
        bet += amount;

        return amount;
    }

    public void setHasFolded(boolean hasFolded) {
        this.hasFolded = hasFolded;
    }

    public void setCanOnlyWinSidePot(boolean canOnlyWinSidePot) {
        this.canOnlyWinSidePot = canOnlyWinSidePot;
    }

    // Reset every player variable except isHuman, name, and bankroll (useful for starting new rounds)
    public void reset() {
        bet = 0;
        hasFolded = false;
        canOnlyWinSidePot = false;

        newHand();
    }

    // Check if the user is bankrupt / has gone all in
    public boolean isBankrupt() {
        return bankroll <= 0;
    }

    // Checks if the player can't raise or call at this time
    public boolean cannotPlay(int callCost, boolean canRaise) {
        /* If a player already went all in and then the bet was raised, they can only win a side pot
        I acknowledge this line is really weird and will probably be executed more than necessary, but
        honestly side pots are so difficult to calc that this is the most elegant solution I found */
        if (isBankrupt() && callCost > bet) {
            canOnlyWinSidePot = true;
        }

        /* Play is only possible if you haven't folded, gone all in, and have a reason to bet
        Check if a bet must be matched or can be raised to ensure that there's a reason to play */
        return hasFolded || isBankrupt() || (callCost == bet && !canRaise);
    }

    // Special code used to ante (pre-flop) because the AI needs to use a lookup table
    public int anteUp(int callCost, boolean canRaise, boolean isNotRound1) {
        // Human betting process is the same regardless of what part of the round it is
        if (isHuman) {
            return humanPlay(callCost, canRaise, isNotRound1);
        }

        // AI decision process pre-flop
        else {
            // Get pre-flop Cards and data
            Card[] openingHand = getOpeningHand();
            Integer[] openingHandRanks = {openingHand[0].getRank(), openingHand[1].getRank()};

            /* Consult the lookup table to determine the AI's next move
            Cast openingHandRanks to a list, so it can be used as a key for lookup table */
            String[] potentialMoves = Constants.preFlopLookupTable.get(List.of(openingHandRanks));

            // The hand's value depends, weather the suits match --> index 0 for match, 1 for differences
            String move = openingHand[0].getSuit() == openingHand[1].getSuit() ? potentialMoves[0] : potentialMoves[1];

            // Math.random() < Constants.AI_BLUFF --> AI bluffs at a random rate
            if (canRaise && (move.equals("RAISE") || Math.random() < Constants.AI_BLUFF)){
                return raise(callCost, Constants.AI_RAISE);
            }

            // Call if the hand is good enough, if there's no need to fold, or to randomly bluff
            else if (move.equals("CALL") || callCost == bet || Math.random() < Constants.AI_BLUFF) {
                return call(callCost);
            }

            return fold(callCost);
        }
    }

    // Lets the user and AI play a regular (post-flop) turn of Texas Hold 'em
    public int play(int callCost, boolean canRaise, int pot, int numPlayersNotFolded) {
        // Human betting process is the same regardless of what part of the round it is
        if (isHuman) {
            return humanPlay(callCost, canRaise, true);
        }

        /* AI decision process post-flop
        Set the initial value of totalScore to getScore() (and numOfScores to 1) because 
        it will be the only score if the community is full --> getCombinations = None */
        int numOfScores = 1;
        double totalScore = getScore();
        
        // Iterate through every possible remaining hand combination
        for (List<Card> combo : getCombinations(Constants.SAMPLE_DECK.getDeck(), 
                Constants.TOTAL_CARDS - getSize())) {
            addCards((ArrayList<Card>) combo);
            numOfScores++;
            
            // Calc potential hand score
            totalScore += getScore();
            remCards((ArrayList<Card>) combo);
        }

        // Expected (avg) score for not folding
        double avgScore = totalScore / numOfScores;

        // What percent of hands are worse than this one?
        double scorePercentile = getScorePercentile(avgScore);

        // If one opponent has a .95 lose rate, 2 have .95^2 = .9025...
        double probOfWinning = Math.pow(scorePercentile, numPlayersNotFolded);

        /* For a bet to make sense, pot * prob > bet --> ideal bet = pot * prob
        Also make sure that the AI doesn't try to bet more money than it has access to */
        int optimalRaise = (int) Math.min(bankroll + bet - callCost, pot * probOfWinning - bet);

        if (canRaise && optimalRaise > 0) {
            return raise(callCost, optimalRaise);
        }

        /* Remember to bluff on occasion
        Separate statement since the raise amount is different */
        else if (canRaise && Math.random() < Constants.AI_BLUFF) {
            return raise(callCost, Constants.AI_RAISE);
        }

        // If the optimal bet is lower than the current bet, the natural move is to fold if forced to do that or call
        else if (optimalRaise < 0 && bet < callCost) {
            return fold(callCost);
        }

        // Implies that it's worth matching the bet --> call
        else {
            return call(callCost);
        }
    }

    // Returns the user's name and their hand type
    public String toString() {
        if (hasFolded) {
            return getName() + " folded, but they had unique cards " + super.toString() + " and a " + bestHand();
        }

        return getName() + " has unique cards " + super.toString() + " and a " + bestHand();
    }
    
    // Private helper method time!
    // Lets the player make direct moves in the game
    private int humanPlay(int callCost, boolean canRaise, boolean isPostFlop) {
        // maxRaise = money at the start of this round - callCost
        int maxRaise = bankroll + bet - callCost;

        if (canRaise) {
            // Give the player data to make an informed decision
            Constants.typeText(name + ", your hand contains " + super.toString() +
                    ", your bankroll is $" +  bankroll + ", and you can raise up to $" + maxRaise + "\n");

            Constants.typeText("Would you like to raise? If so, type how much. If not, type 0: ");
            int raise;

            if (isPostFlop) {
                // Clear scanning cache
                Constants.SCAN.nextLine();
            }

            /* Ensure that the user types in an int, avoids InputMismatchExceptions
            Also, may sure that they don't bet more than money than they have access to
            Short-circuit the bankroll check to ensure that nextInt() doesn't throw an error
            Set raise = Constants.SCAN.nextInt() mid-loop --> avoid calling Scan.nextInt() twice
            Code from: https://stackoverflow.com/questions/2696063/java-util-scanner-error-handling */
            while (!Constants.SCAN.hasNextInt() || (raise = Constants.SCAN.nextInt()) > maxRaise) {
                Constants.typeText("Please type a number less than or equal to " + maxRaise + ": ");
                Constants.SCAN.nextLine();
            }

            if (raise > 0) {
                return raise(callCost, raise);
            }
        }

        else {
            // Slightly different data format if the player can't raise
            Constants.typeText(name + ", your hand contains " + super.toString() +
                    " and your bankroll is $" + bankroll + "\n");
        }

        if (callCost > bet) {
            Constants.typeText("You need to put in " + (callCost - bet) + " chips to call\n");
            Constants.typeText("Would you like to call or fold? (C/F) ");

            // Ensured that the user responds with one of the options provided
            String response = Constants.getResponse("C", "F");

            if (response.equalsIgnoreCase("C")) {
                return call(callCost);
            }

            else {
                return fold(callCost);
            }
        }

        // If the code reaches here, callCost == bet
        return call(callCost);
    }

    // Shorthand for raising a bet, returns updated callCost
    private int raise(int callCost, int raise) {
        // If the AI tries to raise bet too much
        if (callCost + raise > bankroll + bet) {
            return call(callCost);
        }

        Constants.typeText(name + " has decided to raise the bet by $" + raise + "!\n");

        int newCallCost = callCost + raise;

        Constants.typeText("The current bet is now set at $" + newCallCost + "\n");

        subFromBankroll(newCallCost - bet);

        if (bankroll < 0) {
            bet += bankroll;
            bankroll = 0;
        }

        return newCallCost;
    }

    // Shorthand for calling and checking, returns callCost
    private int call(int callCost) {
        if (callCost == bet) {
            // Correct poker terminology is to check when you don't need to increase your bet to match
            Constants.typeText(name + " has decided to check\n");
        }

        else if (callCost == bet + bankroll) {
            Constants.typeText(name + " has decided to go all in to call!\n");
        }

        // Slightly different wording
        else if (callCost > bet + bankroll) {
            Constants.typeText(name +  " has decided to go all in to match part of the bet!\n");

            /* Doesn't apply to going all in to match the whole bet,
            because if the bet is never raised they can win the whole pot */
            canOnlyWinSidePot = true;
        }

        else {
            Constants.typeText(name + " has decided to call\n");
        }

        subFromBankroll(callCost - bet);

        if (bankroll < 0) {
            bet += bankroll;
            bankroll = 0;
        }

        return callCost;
    }

    // Folds the player's hand and notifies the group, returns callCost
    private int fold(int callCost) {
        Constants.typeText(name + " has decided to fold!\n");

        hasFolded = true;
        return callCost;
    }
    
    /* Percents based on known seven-card probability tables
    Data comes from here: https://en.wikipedia.org/wiki/Poker_probability */
    private double getScorePercentile(double score) {
        if (score > 90) {
            return .97;
        }

        if (score > 75) {
            return .94;
        }

        if (score > 60) {
            return .9;
        }

        if (score > 45) {
            return .85;
        }

        if (score > 30) {
            return .6;
        }

        if (score > 15) {
            /* Score - 17 = high pair "index" (e.g. in 2 = lowest pair --> 0, Ace = highest pair --> 12)
            Multiply by .03 to get a range of probabilities (.2 up to .56) representing potential strength
            This is only done for two-of-a-kind because the range of probabilities (.4) is so large */
            return .2 + (score - 17) * .03;
        }

        if (score > 14) {
            return .1;
        }

        return .05;
    }

    /* Shamelessly stolen and mostly unaltered from:
    https://stackoverflow.com/questions/61029823/all-possible-k-combinations-of-list-with-size-n */
    /**
     * Creates combinations of elements of a specific combination size.
     *
     * @param <T>             the type of the elements
     * @param elements            a list of elements, which should be unique
     * @param combinationSize the size of the combinations
     * @return a list of combinations
     */
    private static <T> List<List<T>> getCombinations(List<T> elements, int combinationSize) {
        ArrayList<List<T>> fullCombinations = new ArrayList<>();
        createCombinations(elements, fullCombinations, new ArrayList<>(), 0, combinationSize);
        return fullCombinations;
    }

    /**
     * Recursive function that grows the combination size, and adds full combinations when the combination size is met.
     * To avoid duplicates, only elements that are higher in the list are added to the combination. The combination is
     * complete when <code>missing == 0</code>.
     *
     * @param <T>              the type of the elements
     * @param elements             the elements to create combinations from, all elements should be unique
     * @param fullCombinations the final result array of the combinations, shared among all recursive calls
     * @param combination      the current combination that needs to get <code>missing<code> members
     * @param index            the index of the element one higher than the last element in the combination
     * @param missing          the amount of elements needed to complete the combination
     */
    private static <T> void createCombinations(List<T> elements, List<List<T>> fullCombinations, List<T> combination,
                                               int index, int missing) {
        if (missing == 0) {
            fullCombinations.add(combination);
            return;
        }

        // We don't need to go over elements.size() - missing because then the combination cannot be completed, too few left
        for (int i = index; i <= elements.size() - missing; i++) {
            List<T> newCombination;
            if (i == elements.size() - missing) {
                // Optimization: avoid de-referencing the final combination, reuse
                newCombination = combination;
            } else {
                newCombination = new ArrayList<>(combination);
            }
            newCombination.add(elements.get(i));
            createCombinations(elements, fullCombinations, newCombination, i + 1, missing - 1);
        }
    }
}
