/**
 * The static evaluator,
 * called when we have reached a leaf node of the search tree.
 *
 * This class is intended to be subclassed by more capable searchers,
 * only does very simple scoring based on material.
 */
class Eval
{

    /**
     * a parameterless constructor.
     */
    public Eval() {
    }   // Eval


    /**
     * Determine the static goodness of this position.
     *
     * This is the default evaluator, using the simplest
     * textbook material score only, subclass and override
     * this method to get more advanced scoring functions.
     *
     * Note! this version depends on knowledge of the internal 
     * representation of boards in class Board.
     *
     * @param player either WHITE or BLACK.
     * @param b the current playing board to evaluate.
     */
    public int eval(int player, Board b) {
	int [] board = b.getBoard();
	// How much material has each side got?
	int white = 0;
	int black = 0;

	int piece, color;
	for (int i = Board.MINCOORD; i < Board.MAXCOORD; i++) {
	    if (board[i] != Board.FREE && board[i] != Board.OFFBOARD) {
		piece = board[i] & Board.PIECE_MASK;
		color = board[i] & Board.COLOR_MASK;
		int v = 0;
		switch (piece) {
		case Board.PAWN:	v = 100;	break;
		case Board.KNIGHT:	v = 300;	break;
		case Board.BISHOP:	v = 300;	break;
		case Board.ROOK:	v = 500;	break;
		case Board.QUEEN:	v = 900;	break;
		    // case Board.KING:	v = 999;	break;
		default:
		    v = 0;
		    break;
		}
		if (color == Board.WHITE) {
		    white += v;
		} else {
		    black += v;
		}
	    }
	}
	int res = white - black;
	if (player == Board.BLACK) {
	    res = -res;
	}
	return res;
    }   // eval

}
