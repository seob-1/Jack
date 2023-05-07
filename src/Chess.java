//
//
//
import java.util.Vector;
/**
 *
 */
class Chess
{

    /**
     * Instance variables.
     */
    Board  b;
    Eval   e1;
    Search s1;
    Eval   e2;
    Search s2;
    int depth = 4;


    /**
     * Constructor
     */
    public Chess() {
	b = new Board();
	e1 = new Eval();
 e1 = new E2();
	e2 = new E3();
	s1 = new AlphaBeta(depth+0, e1, false);
	s2 = new AlphaBeta(depth-0, e2);
    }   // Chess


    /**
     * Return color name for player as String.
     */
    String clr(int player) {
	return player == Board.WHITE ? "white" : "black";
    }   // clr

    /**
     * Return player name for piece as String.
     */
    String pcs(int piece) {
	String r = "x-+!&%_";
	switch (piece) {
	case Board.PAWN:
	    r = "pawn";
	    break;
	case Board.KNIGHT:
	    r = "knight";
	    break;
	case Board.BISHOP:
	    r = "bishop";
	    break;
	case Board.ROOK:
	    r = "rook";
	    break;
	case Board.QUEEN:
	    r = "queen";
	    break;
	case Board.KING:
	    r = "king";
	    break;
	}
	return r;
    }   // pcs

    /**
     * Print nice formatted move
     */
    String nice(Move m) {
	String str = "_abcdefgh";
	int row, col;
	row = m.from / 10 -1;
	col = m.from % 10;
	String res = str.charAt(col) + String.valueOf(row);
	res = res + " to ";
	row = m.to / 10 -1;
	col = m.to % 10;
	res = res + str.charAt(col) + String.valueOf(row);
	return res;
    }  // nice

    /**
     * Play an interesting game of chess...
     */
    void autoPlay() {
	int player = Board.WHITE;
	boolean goon = true;
	int moveno = 0;
	//	Search s3 = new AlphaBeta(depth, e2);
	do {
	    moveno++;
	    String col = clr(player);
	    System.out.println("move " + moveno + ":  " + col + " to play");
	    Vector pv = new Vector();
	    int score;
	    //	    int score3 = 0;
	    if (player == Board.BLACK) {
		score  = s2.search(player, b, pv);
		//		Vector pv3 = new Vector();
		//		score3 = s3.search(player, b, pv3);
	    } else {
		score  = s1.search(player, b, pv);
	    }
	    System.out.println(col + "  score = " + score);
	    if (score == 100000) {
		System.out.println("Game ends after 3 repetitions.");
		goon = false;
	    }
	    boolean check = b.isCheck(player);
	    if (pv.size() == 0) {
		if (check) {
		    String c = clr(24-player);
		    System.out.println("Player " + c + " won by check mate.");
		    goon = false;
		}
	    } else {
		Move move = (Move) pv.get(0);
		b.makeMove(move);
		System.out.println("Static score = " + e1.eval(player, b) + 
				   "  :  " + e2.eval(player, b));
		System.out.println(col + " " + 
				   pcs(move.p1 & Board.PIECE_MASK) +
				   " moved: " + nice(move));
		if (move.p2 != Board.FREE) {
		    int piece = move.p2 & Board.PIECE_MASK;
		    int color = move.p2 & Board.COLOR_MASK;
		    String opp = clr(color);
		    System.out.println(col + " " + 
				       pcs(move.p1 & Board.PIECE_MASK) +
				       " captured " + opp +
				       " " + pcs(piece));
		}
	    }
	    player = 24 - player;
	    System.out.println();
	    System.out.println(b.toString2());
	} while (goon);
    }   // autoPlay

    /**
     * main program
     */
    public static void main(String [] args) {
	Chess obj = new Chess();
	obj.autoPlay();
    }   // main


}
