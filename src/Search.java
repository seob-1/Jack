//
//
//

import java.util.Random;
import java.util.Vector;
/**
 * Search the game tree
 */
class Search 
{

    /**
     * instance variables.
     */
    int maxDepth;
    Eval eval;

    Random random;

    long count;		// number of static evaluations
    int max;		// most number of moves from one node
    long sum;		// Sum of all moves generated
    long n;		// calls to genMoves()


    /**
     * Constructor
     */
    public Search(int n, Eval eval) {
	maxDepth = n;
	this.eval = eval;
	random = new Random(72);
    }   // Search


    /**
     * Do a simplistic min-max search of the board b.
     */
    private int minMax(int depth, int me, Board b, Vector pv) {
	if (depth >= maxDepth) {
	    int score = eval.eval(me, b);
	    count++;
	    if (b.isCheck(me)) {
		Vector v = b.genMoves(me);
		if (v.size() == 0) {
		    score = depth - 111111;
		}
	    }
	    return score;
	}
	Vector moves = b.genMoves(me);

	// int size = moves.size();
	n++;
	if (moves.size() > max) {
	    max = moves.size();
	}
	sum = sum + moves.size();

	if (moves.size() == 0) {
	    int score;
	    // check if I'm in a check mate position.
	    if (b.isCheck(me)) {
		score = depth - 111111;
	    } else {
		// what is a draw worth?
		//		score = eval.eval(me, b);
		// Continue, passing this move.
		Move move = new Move(0, 0, 0, 0, 0);
		pv.add(move);
		score = -minMax(depth+1, 24-me, b, pv);
	    }
	    return score;
	}
	int best = -1000000;
	Vector best_subline = null;
	for (int i = 0; i < moves.size(); i++) {
	    Move move = (Move) moves.get(i);
	    b.makeMove(move);
	    Vector r = new Vector();
	    r.add(move);
	    int score;
	    score = -minMax(depth+1, 24-me, b, r);
	    if (score > best) {
		best = score;
		best_subline = r;
	    } else if (score == best) {
		/*
		int rand = random.nextInt(100);
		if (rand > 50) {
		    best = score;
		    best_subline = r;
		}
		*/
	    }
	    b.retractMove(move);
	}
	for (int i = 0; i < best_subline.size(); i++) {
	    Move m = (Move) best_subline.get(i);
	    pv.add(m);
	}
	return best;
    }   // minMax

    /**
     * search for best move.
     */
    int search(int me, Board b, Vector pv) {
	long start = System.currentTimeMillis();
	count = 0;
	max = 0;
	sum = 0;
	n = 0;
	int score = minMax(0, me, b, pv);
	long stop = System.currentTimeMillis();
	long millis = stop - start;
	if (millis == 0) {
	    millis = 1;
	}
	float time = (float) (millis / 1000.0);
	float rate = count / time;
	if (n == 0) {
	    n = 1;
	}
	float avg = sum / (float)n;
	avg = sum / n;
	System.out.println("Evalu8d " + count + " nodes in " + time +
			   "s.  = " + rate + " nodes/s.  " +
			   "max = " + max +
			   "  avg = " + avg);
	return score;
    }   // search


}
