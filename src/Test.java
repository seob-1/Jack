//
import java.util.Vector;
/**
 *
 */
class Test
{

    /**
     *
     */
    public static void main(String[] args) {
	Board b = new Board();
	System.out.println(b.toString2());
	Eval e = new E2();
	// Eval e = new Eval();

	// Check static evaluation function
	System.out.println();
	int white = e.eval(Board.WHITE, b);
	int black = e.eval(Board.BLACK, b);
	System.out.println("static scores (" + white + ", " + black + ")");

	// Remove all black pawns...
	int [] board = b.getBoard();
	for (int i = 81; i < 89; i++) {
	    board[i] = Board.FREE;
	}
	System.out.println();
	System.out.println(b.toString2());
	System.out.println();
	white = e.eval(Board.WHITE, b);
	black = e.eval(Board.BLACK, b);
	System.out.println("static scores (" + white + ", " + black + ")");
 
	// Look at all moves white can make.
	b = new Board();
	Vector vec = b.genMoves(Board.WHITE);
	for (int i = 0; i < vec.size(); i++) {
	    Move m = (Move) vec.get(i);
	    b.makeMove(m);
	    int score = e.eval(Board.WHITE, b);
	    int erocs = e.eval(Board.BLACK, b);
	    System.out.println();
	    System.out.println("Move # " + (i+1) + "  of " + vec.size() +
			       "  static score = " + score + 
			       "  (" + erocs + ")");
	    if (b.isCheck(Board.WHITE)) {
		System.out.println("white is in check");
	    }
	    if (b.isCheck(Board.BLACK)) {
		System.out.println("black is in check");
	    }
	    System.out.println();
	    System.out.println(b.toString2());
	    b.retractMove(m);
	}

	// Look at all moves black can make.
	//	b = new Board();
	b.onTheMove(Board.BLACK);
	vec = b.genMoves(Board.BLACK);
	for (int i = 0; i < vec.size(); i++) {
	    Move m = (Move) vec.get(i);
	    b.makeMove(m);
	    int score = e.eval(Board.BLACK, b);
	    int erocs = e.eval(Board.WHITE, b);
	    System.out.println();
	    System.out.println("Move # " + (i+1) + "  of " + vec.size() +
			       "  static score = " + score + 
			       "  (" + erocs + ")");
	    if (b.isCheck(Board.WHITE)) {
		System.out.println("white is in check");
	    }
	    if (b.isCheck(Board.BLACK)) {
		System.out.println("black is in check");
	    }
	    System.out.println();
	    System.out.println(b.toString2());
	    b.retractMove(m);
	}

	System.out.println();
	System.out.println("Before:");
	vec = b.genMoves(Board.WHITE);
	for (int i = 0; i < vec.size(); i++) {
	    Move m = (Move) vec.get(i);
	    System.out.println(i + " :  (" + m.estimate + ")  " +
			       b.cor(m.from) + b.cor(m.to) + "  val:" +
			       m.p1 + ", " + m.p2 + "  status:" + m.status);
	}
	AlphaBeta ab = new AlphaBeta(4, null);
	ab.sortMoves(vec);
	System.out.println();
	System.out.println("After:");
	for (int i = 0; i < vec.size(); i++) {
	    Move m = (Move) vec.get(i);
	    System.out.println(i + " :  (" + m.estimate + ")  " +
			       b.cor(m.from) + b.cor(m.to) + "  val:" +
			       m.p1 + ", " + m.p2 + "  status:" + m.status);
	}

	b.onTheMove(Board.WHITE);
	int from = 35;		// e2 - e4
	int to = from + 20;
	Move m = new Move(from, to, b.getBoard()[from], b.getBoard()[to], 0);
	b.makeMove(m);
	System.out.println();
	String fen = b.toString();
	System.out.println("FEN1= " + fen);
	Board b2 = new Board(fen);
	//	System.out.println();
	System.out.println(b2.toString2());
	//	System.out.println();
	System.out.println("FEN2= " + b2.toString());

   }   // main


}
