/* The Game class includes the driver method that actually initiates rounds of Texas Hold 'em
The driver uses every other class to generate players, start rounds, keep track of each player's bankroll, etc. */
import java.util.ArrayList;
import java.util.Collections;


class Game {
    public static void main (String[] args) {
        // Give the player an introductory message explaining the rules of the game
        Constants.typeText("Hi! This is a program that simulates a game of Texas Hold 'em!\n");
        Constants.typeText("The game will continue until you cash out, are eliminated, or are the last person left\n");
        Constants.typeText("This program is designed for one player; your opponents are all pre-programmed AI\n");

        Constants.typeText("Please enter your name: ");
        String name = Constants.SCAN.nextLine();

        // Use two newlines (/n) to fully separate the introduction from gameplay
        Constants.typeText("Hi " + name + "! It's time to get your game on, good luck!\n\n");

        // Generate a list of mostly AI players. The last entry represents the only real player
        ArrayList<Player> players = getPlayers(name);

        // Start the first round with a random player
        int firstPlayer = (int) (Math.random() * players.size());

        int round = 1;
        // Outside of loop because the round starts and ends before any text can be displayed
        Constants.typeText("Starting round " + round + "!\n");

        // Play the game until one of the conditions (cash out, bankruptcy, victory) is met
        while(playRound(players, firstPlayer, round)) {
            // Double newline between each round makes gameplay more clean
            Constants.typeText("You have chosen to continue playing. Onto the next round!\n\n");

            // A different player should go first each round --> increment firstPlayer
            firstPlayer++;

            round++;
            Constants.typeText("Starting round " + round + "!\n");
        }

        // If the player went bankrupt, they were removed from the round
        if (!players.get(players.size() - 1).isHuman()) {
            Constants.typeText("Oh No! You went bankrupt! Game over :(");
        }

        else {
            // Calculate and display the real player's results!
            int curBank = players.get(players.size() - 1).getBankroll();

            if (curBank > Constants.PLAYER_STARTING_BANK) {
                Constants.typeText("Congrats! You made $" + (curBank - Constants.PLAYER_STARTING_BANK) + "!");
            }

            else if (curBank == Constants.PLAYER_STARTING_BANK) {
                Constants.typeText("You broke even!");
            }

            else {
                Constants.typeText("Unfortunately, you lost $" + (Constants.PLAYER_STARTING_BANK - curBank) + " :(");
            }
        }
    }

    /* Returns true if the player wants to keep playing and can afford another round, false otherwise
    This function simulates an entire round of Texas Hold 'em based on the game constants defined above

    Parameter players: a list of players still remaining in the game, where player[-1] = the human player
    Parameter firstRound = index of the player who should bet first this time, changes each round */
    public static boolean playRound(ArrayList<Player> players, int firstPlayer, int round) {
        int callCost = Constants.BIG_BLIND;
        int pot = 0;

        int totalPlayers = players.size();

        // Decrements each time a player folds
        int numPlayersInRound = totalPlayers;

        /* Increment each time a player goes all in
        Useful for checking if a player should raise */
        int numPlayersAllIn = 0;

        /* Initialize a new deck to not run out of cards
        Beat the card counters with instant shuffling */
        Deck deck = new Deck(Constants.NUM_CASINO_DECKS);
        deck.shuffle();

        // Community cards = cards anyone can use
        ArrayList<Card> community = new ArrayList<>();

        // Add "..." at the end to make it seem like dealing cards takes time
        Constants.typeText("Dealing cards...\n");

        // Reset a player's hand, folding status, etc. each round (everything except bankroll)
        for (Player player : players) {
            player.reset();

            // Each player starts with two unique cards
            for (int j = 0; j < 2; j++) {
                player.addCard(deck.deal());
            }
        }

        /* Real player = last entry in list --> "your hand" is at index totalPlayers - 1
        Use getHand() instead of getOpeningHand() because it's simpler to use in this context */
        Constants.typeText("Your starting hand is " + players.get(totalPlayers - 1).getHand() + "\n");

        /* Second for loop so that the user's opening hand can be displayed directly after "dealing cards..."
        Iterate until i < totalPlayers * 2 + 1 so that the small blind can match the big blind's bets after ante
        Use a standard for loop instead of a for-each so that the first and second players (blinds) can be detected
        The standard loop also lets us iterate through "players" twice --> force players to call or fold when raised */
        for (int i = 0; i < totalPlayers * 2 + 1; i++) {
            // If there's one player left, end round
            if (numPlayersInRound == 1) {
                break;
            }

            // Add firstPlayer so the starting player changes each round
            Player player = players.get((i + firstPlayer) % totalPlayers);

            if (i < totalPlayers) {
                Constants.typeText(player.getName() + " has entered the round with a bankroll of $"
                        + player.getBankroll() + "\n");
            }

            if (i == 0) {
                Constants.typeText("They're small blind and ante " + Constants.SMALL_BLIND + " chips\n");
                pot += player.subFromBankroll(Constants.SMALL_BLIND);
                continue;
            }

            if (i == 1) {
                Constants.typeText("They're big blind and ante " + Constants.BIG_BLIND + " chips\n");
                pot += player.subFromBankroll(Constants.BIG_BLIND);
                continue;
            }

            /* Add two for big the and small blinds
            If numPlayersInRound - numPlayersAllIn == 1, there's no point in raising */
            boolean canRaise =  i < totalPlayers + 2 && numPlayersInRound - numPlayersAllIn > 1;

            // If the player can't raise/call...
            if (player.cannotPlay(callCost, canRaise)) {
                continue;
            }

            // Pot increase = updated calCost - initial bet, but since bet might change create a new variable
            int initBet = player.getBet();

            // Only reached when betting is possible
            callCost = player.anteUp(callCost, canRaise, round > 1);

            // Only decrement the first time (right after) a player folds
            if (player.hasFolded()) {
                numPlayersInRound--;
            }

            else {
                pot += player.getBet() - initBet;
            }

            // Same thing for players going all in
            if (player.isBankrupt()) {
                numPlayersAllIn++;
            }
        }

        // Turn order: burn cards, deal cards, players place bets
        for (int numCards : Constants.COMMUNITY_CARDS) {
            // If there's one player left, end round
            if (numPlayersInRound == 1) {
                break;
            }

            // Extra newline to separate every turn
            Constants.typeText("\nTime to reveal new cards!\n");

            // Burn one card each turn (standard)
            Constants.typeText("Burning one card...\n");
            Constants.typeText("Now, adding " + numCards + " new card(s) to the community...\n");

            deck.burn(numCards);

            // Deal out community cards
            for (int i = 0; i < numCards; i++) {
                Card newCard = deck.deal();
                community.add(newCard);

                // Scoring is easier when the hand and community cards are all in one place
                for (Player player : players) {
                    player.addCard(newCard);
                }
            }

            // Shows the user what the new community cards are
            Constants.typeText("The community is now comprised of: " + community + "\n");

            /* Iterate through "players" twice --> force every single player to call or fold when raised
            Iterate until i < totalPlayers * 2 + 1 so that the small blind can match the big blind's bets
            Initialize i = 2 because the big and small blinds can't bet on the first time around the table
            IMPORTANT: WHY IS THERE NO METHOD FOR THIS REPETITIVE CODE? IT'D BE CONVOLUTED + ONLY USED TWICE
            FOR INSTANCE: IT'S TOUGH TO CHANGE THE VALUES OF numPlayersInRound, numPlayersAllIn IN A METHOD */
            for (int i = 2; i < totalPlayers * 2 + 1; i++) {
                // If there's one player left, end round
                if (numPlayersInRound == 1) {
                    break;
                }

                // Add firstPlayer so the starting player changes each round
                Player player = players.get((firstPlayer + i) % totalPlayers);

                /* Add two for big the and small blinds
                If numPlayersInRound - numPlayersAllIn == 1, there's no point in raising */
                boolean canRaise =  i < totalPlayers + 2 && numPlayersInRound - numPlayersAllIn > 1;

                // If the player can't raise/call...
                if (player.cannotPlay(callCost, canRaise)) {
                    continue;
                }

                // Pot increase = updated calCost - initial bet, but since bet might change create a new variable
                int initBet = player.getBet();

                // Only reached when betting is possible
                callCost = player.play(callCost, canRaise, pot, numPlayersInRound);

                // Only decrement the first time (right after) a player folds
                if (player.hasFolded()) {
                    numPlayersInRound--;
                }

                else {
                    pot += player.getBet() - initBet;
                }

                // Same thing for players going all in
                if (player.isBankrupt()) {
                    numPlayersAllIn++;
                }
            }
        }

        // Extra newline to separate betting from a display of each player's hand + winner
        Constants.typeText("\nBetting has concluded. Everyone must now show their hands!\n");

        for (Player player : players) {
            Constants.typeText(player.toString() + "\n");
        }

        Constants.typeText("The main pot winner(s) of this round are: " +
                playersListToString(getWinners(new ArrayList<>(players), numPlayersInRound, pot)) + "!\n");

        // Safe remove bankrupt players from the game
        int i = 0;

        while (i < players.size()) {
            Player player = players.get(i);

            if (player.isBankrupt()) {
                Constants.typeText(player.getName() + " is bankrupt and has been removed from the game!\n");
                players.remove(player);
            }

            else {
                i++;
            }
        }

        // If the real player was removed or there's only one player left,
        // return false before asking them to keep playing 'cuz they can't
        if (!players.get(players.size()-1).isHuman() || players.size() == 1) {
            return false;
        }

        // Conclude the round and possibly the game
        Constants.typeText("Do you want to continue playing? (Y/N) ");

        // Ensure that the user responds with either "Y" or "N"
        String response = Constants.getResponse("Y", "N");

        // Check conditions for ending the game:
        // Real player wants to cash out and end the game
        // Real player is bankrupt (already checked a few lines above)
        // Real player is the only one not bankrupt (also already checked), wins by default
        return response.equalsIgnoreCase("Y");
    }

    // Private helper methods!
    /* Returns an ArrayList of AI competitors and one real player (user)
    The AI names and bankrolls are generated based on values in the Constants file
    The final entry in the returned list is the real player (helps with generating user input)

    Parameter name: The scanned-in name of the real player */
    private static ArrayList<Player> getPlayers(String name) {
        ArrayList<Player> players = new ArrayList<>();

        for (int i = 0; i < Constants.NUM_OPPONENTS; i++) {
            int playerIndex = (int) (Math.random() * Constants.NAMES.size());

            // Remove the player's name from NAMES so that it can't be used to create another player
            players.add(new Player(Constants.NAMES.remove(playerIndex), 100, false));
        }

        // Choose random players to give special bankrolls
        int randomMin = (int) (Math.random() * players.size());

        // Gets a different random player to make "big"
        int randomMax = randomHelper(players.size(), randomMin);

        Player minPlayer = players.get(randomMin);
        Player maxPlayer = players.get(randomMax);

        // Give the players special names for bankroll
        minPlayer.setName("Lil' " + minPlayer.getName());
        minPlayer.setBankroll(Constants.MIN_OPP_BANKROLL);

        maxPlayer.setName("Big " + maxPlayer.getName());
        maxPlayer.setBankroll(Constants.MAX_OPP_BANKROLL);

        // Add the human player with custom naming to the game
        players.add(new Player(name, 100, true));

        return players;
    }

    /* Returns the round's winner by hand score + distributes side pots
    This method is convoluted as heck, but I still think I did an ok job (side pots suck) */
    private static ArrayList<Player> getWinners(ArrayList<Player> players, int numPlayersInRound, int pot) {
        /* Why did I use Collections.sort here but a hand-crafted method to sort out the Cards in Hand?
        It's annoying to generalize the approach with a Comparator parameter, and I just wanted to show off once
        Clone players to avoid affecting the original List's order, which helps determine if the user is bankrupt */
        ArrayList<Player> cloned = new ArrayList<>(players);
        cloned.sort(new SortByScore());

        // Sort in reverse to get the best players first
        Collections.reverse(cloned);

        // Initialize top players list, accounts for ties
        ArrayList<Player> bestPlayers = new ArrayList<>();

        while (pot > 0) {
            // Sorted --> index 0 = best
            Player bestPlayer = cloned.get(0);
            double bestScore = bestPlayer.getScore();

            // Reset list
            bestPlayers.clear();
            bestPlayers.add(bestPlayer);

            // Check for players with tied scores
            for (int i = 1; i < cloned.size(); i++) {
                // If two top players have the same score...
                if (cloned.get(i).getScore() == bestScore) {
                    bestPlayers.add(cloned.get(i));
                }

                // Sorted list --> all other players have worse scores
                else {
                    break;
                }
            }

            /* All-in max winnings = bet * number of other players
            Multiply by the number of bestPlayers because they might need to split a pot
            If the went all in and didn't match the whole bet, they only get to take home the side pot max */
            int totalWinnings = bestPlayer.canOnlyWinSidePot() ?
                    Math.min(bestPlayer.getBet() * numPlayersInRound * bestPlayers.size(), pot) : pot;

            /* Calculate individual winnings per player, accounts for potential ties
            Round up in case individualWinnings * numPlayers is slightly < totalWinnings
            In that case, pot -= winnings would be slightly > 0 --> wrong winner printed out */
            int individualWinnings = (int) Math.ceil((double) totalWinnings / bestPlayers.size());

            for (Player player : bestPlayers) {
                if (totalWinnings < pot) {
                    // Display a message for players that can only win a side pot
                    Constants.typeText(player.getName() + " wins $" + individualWinnings + " in a side pot!\n");
                }

                pot -= player.addToBankroll(individualWinnings);

                // Lets other players win the remaining pot
                cloned.remove(player);
            }
        }

        return bestPlayers;
    }

    // Returns a random int that's < maxVal and != excluded
    private static int randomHelper(int maxVal, int excluded) {
        int num = (int) (Math.random() * maxVal);

        if (num == excluded) {
            return randomHelper(maxVal, excluded);
        }

        return num;
    }

    /* Returns the winners of a round in a more "readable" format:
    "Name 1", "Name 2", "Name 3" or simply "Name 1" if size() == 1 */
    private static String playersListToString(ArrayList<Player> players) {
        if (players.size() == 1) {
            return players.get(0).getName();
        }

        // Recur for every player, adding a comma between each
        // It's ok to remove players because the list argument is always a copy
        return players.remove(0).getName() + ", " + playersListToString(players);
    }
}
