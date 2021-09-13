This is a java project which utilizes object oriented programming to simulate a game of Texas Hold’em.
The program allows the user to play games against four AI opponents until all of the opponent are bankrupt, the player is bankrupt, or they choose to cash out.
If you want to play for yourself, follow these directions:
  Fork and download the repository
  Navigate to Poker/src in the terminal or command line
  Compile with “Javac Game.java” and run with “Java Game”
  If you don’t have a JDK, you can download one here: https://www.oracle.com/java/technologies/javase-downloads.html


The following classes are used:
  Card.java: Simulates a normal playing card with rank and suit attributes
  Deck.java: Utilizes the card class the simulate a standard 52 playing card deck
  Hand.java: Each instance represents a Player hand, and includes functions to evaluate those hands (e.g. hasTwoPair())
  Player.java: Each instance represents a Player (either real or AI) with attributes such as bankroll, bet, and an individual Hand
  Game.java: The driver class, implements every other class to simulate a full game with user input and AI opponents. 
  SortByScore: A helper class used to sort Players by their hand scores and whether or not they’ve folded
  Constants: The “control center” of the project, includes variables, constants, and methods used frequently in other classes 

The AI’s play style works as follows:
1. If it’s the first round of betting (pre-flop) consult a lookup table to determine weather or not it should call, fold, or raise
2. Else, use an exhaustive approach to calculate the average potential value of the hand, and call/raise only when the probabilities give the AI a slight edge
3. In both cases, the AI will choose to bluff (call/raise) at a random rate determined by constant “AI_BLUFF” in the Constants file (set to .25 currently)

The disadvantage of this procedure is that the AI is susceptible to the user continuously raising the bet by substantial margins, as it re-decides whether or not to fold each round and only bluffs 1/4th of the time.
This means that the AI will likely fold before betting has concluded if the player raises enough, even if the former has a good hand. While this isn’t great, my goal wasn’t to create a super strong AI, but to make the framework for humans + AI to play a simulated games.
In the future, I might design a poker AI with machine learning that fixes this problem and others. I think it would be very feasible given the project’s current OOP structure to simulate a ton of games without human input and learn off of them to create a powerful AI.

If you find any bugs in the program, please let me know at traubsimon0@gmail.com, I am always looking to improve!
