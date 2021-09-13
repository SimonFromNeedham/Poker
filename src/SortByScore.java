// Helper class utilized to rank Player objects by their hand scores
import java.util.Comparator;


public class SortByScore implements Comparator<Player> {
    public int compare(Player a, Player b) {
        double aScore = a.getScore();
        double bScore = b.getScore();

        boolean aFolded = a.hasFolded();
        boolean bFolded = b.hasFolded();

        /* The better player is the one that didn't fold
        If both did the same thing (fold/call), the player with the higher score is better */
        if (aFolded == bFolded) {
            return Double.compare(aScore, bScore);
        }

        else if (!aFolded) {
            return 1;
        }

        else {
            return -1;
        }
    }
}
