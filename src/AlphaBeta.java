//
//
//

import java.util.Random;
import java.util.Vector;
/**
 * Search the game tree
 */
class AlphaBeta extends Search
{

    /**
     * instance variables.
    int maxDepth;
    Eval eval;

    Random random;

    long count;		// number of static evaluations
    int max;		// most number of moves from one node
    long sum;		// Sum of all moves generated
    long n;		// calls to genMoves()
     */

    final int BIG = 1000000000;

    boolean extend;

    Move [] ref;

    /**
     * Constructor
     */
    public AlphaBeta(int n, Eval eval) {
	super(n, eval);
	random = new Random(818);
	random = new Random();
	ref = new Move[1];
    }   // AlphaBeta

    /**
     * Alternative constructor.
     */
    public AlphaBeta(int n, Eval eval, boolean extend) {
	super(n, eval);
	random = new Random(818);
	random = new Random();
	ref = new Move[1];
	this.extend = extend;
    }   // AlphaBeta


    /**
     * Attempt to sort moves to shorten runtime of program.
     */
    void sortMoves(Vector moves) {
	int est;
	for (int i = 0; i < moves.size(); i++) {
	    Move m = (Move) moves.get(i);
	    if (m.p2!= 0) {
		est = 100 + (m.p2 & Board.PIECE_MASK);
	    } else {
		est = (m.p1 & Board.PIECE_MASK);
		if (est == Board.PAWN) {
		    int col = m.from % 10;
		    if (col == 4 || col == 5) {
			est++;
		    }
		}
	    }
	    int rand = random.nextInt(20);
	    est += rand;
	    m.estimate = -est;
	}
	java.util.Collections.sort(moves);
    }   // sortMoves

    /**
     * Do a simplistic min-max search of the board b.
     */
    private int alphabeta(int depth, int me, Board b, Vector pv,
			  int alpha, int beta) {
	if (depth > 0 && b.repeat3()) {
	    return -100000;
	}

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
		//		count++;
		score = 0;
	    }
	    return score;
	}
	
	sortMoves(moves);
	int best = -10000000;
	Vector best_subline = null;
	for (int i = 0; i < moves.size(); i++) {
	    Move move = (Move) moves.get(i);
	    b.makeMove(move);
	    Vector r = new Vector();
	    r.add(move);
	    int score;
	    score = -alphabeta(depth+1, 24-me, b, r, -beta, -alpha);
	    if (score > best) {
		best = score;
		best_subline = r;
	    }
	    /*
	    else if (score == best) {
		int rand = random.nextInt(100);
		if (rand > 50) {
		    best = score;
		    best_subline = r;
		}
	    }
	     */
	    b.retractMove(move);
	    if (best > alpha) {
		alpha = best;
	    }
	    if (alpha >= beta) {

		/*
 if (Math.abs(alpha) < BIG && Math.abs(beta) < BIG) {
     System.out.println("AB-cutoff;  depth = " + depth +
			"  alpha = " + alpha + "  beta = " + beta);
 }
		*/
		for (int k = 0; k < best_subline.size(); k++) {
		    Move m = (Move) best_subline.get(k);
		    pv.add(m);
		}
		return alpha;
	    }
	}
	for (int k = 0; k < best_subline.size(); k++) {
	    Move m = (Move) best_subline.get(k);
	    pv.add(m);
	}
	return best;
    }   // alphabeta

    /**
     * Do a simplistic min-max search of the board b.
     */
    private int alpha2(int depth, int me, Board b, Vector pv,
			  int alpha, int beta, boolean ext) {
	if (depth > 0 && b.repeat3()) {
	    return -100000;
	}

	if ((depth >= maxDepth && !ext && !b.isCheck(me)) ||
	    (depth >= (maxDepth + 4))) {
	    int score = eval.eval(me, b);
	    count++;
	    return score;
	}
	Vector moves = b.genMoves(me);

	n++;
	if (moves.size() > max) {
	    max = moves.size();
	}
	sum = sum + moves.size();

	if (moves.size() == 0) {
	    int score = 0;
	    // Am I in a check mate position?
	    if (b.isCheck(me)) {
		score = depth - 111111;
	    }
	    return score;
	}

	sortMoves(moves);
	int best = -10000000;
	Vector best_subline = null;
	for (int i = 0; i < moves.size(); i++) {
	    Move move = (Move) moves.get(i);
	    b.makeMove(move);
	    Vector r = new Vector();
	    r.add(move);

	    // Extend search on captures and pawn promotions.
	    boolean exx = move.p2 != 0;
	    if ((move.p1 & Board.PIECE_MASK) == Board.PAWN) {
		int rank = move.to / 10 - 1;
		if (rank == 1 || rank == 8) {
		    exx = true;
		}
	    }
	    if (depth >= (maxDepth + 2)) {
		exx = false;
	    }

	    int score = -alpha2(depth+1, 24-me, b, r, -beta, -alpha, exx);
	    if (score > best) {
		best = score;
		best_subline = r;
	    }
	    b.retractMove(move);
	    if (best > alpha) {
		alpha = best;
	    }
	    if (alpha >= beta) {
		for (int k = 0; k < best_subline.size(); k++) {
		    Move m = (Move) best_subline.get(k);
		    pv.add(m);
		}
		return alpha;
	    }
	}
	for (int k = 0; k < best_subline.size(); k++) {
	    Move m = (Move) best_subline.get(k);
	    pv.add(m);
	}
	return best;
    }   // alpha2

    /**
     * search for best move.
     */
    int search(int me, Board b, Vector pv) {
	long start = System.currentTimeMillis();
	count = 0;
	max = 0;
	sum = 0;
	n = 0;
	int score;
	if (extend) {
	    score = alpha2(0, me, b, pv, -BIG, BIG, false);
	} else {
	    score = alphabeta(0, me, b, pv, -BIG, BIG);
	}
	long stop = System.currentTimeMillis();
	long millis = stop - start;
	if (millis == 0) {
	    millis = 1;
	}
	float time = (float) (millis / 1000.0);
	int rate = (int) (count / time);
	if (n == 0) {
	    n = 1;
	}
	float avg = sum / (float)n;
	avg = sum / n;
	/*
	 * /
	System.out.println("Evalu8d " + count + " nodes in " + time +
			   "s.  = " + rate + " nodes/s.  " +
			   "max = " + max +
			   "  avg = " + avg);
	/*
	 */
	System.out.println("Eval'd " + count + " nodes in " + time +
			   "s. [" + rate + " n/s" +
			   "] score= " + score);
	return score;
    }   // search


}
