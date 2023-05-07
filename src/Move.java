/**
 *
 * @author Erik Bäckerud
 * @version 04-10-19
 */
class Move implements Comparable
{


    /**
     * Index of squares we move from.
     */
    int from;
    /**
     * Index of squares we move to.
     */
    int to;

    /**
     * piece at position board[from].
     */
    int p1;
    /**
     * piece at position board[to].
     */
    int p2;

    /**
     * Piece value that pawn is promoted to when reaching final rank.
     * Should be in the range [KNIGTH..QUEEN].
     * Leave as zero if no promotion.
     */
    int promo;

    /**
     * Status word, contains among other things:
     * Previous value of last destination used for en-pasant moves.
     * Needed to restore full state of playing board after a move
     * is retracted.
     */
    int status;

    /**
     * Estimate of this moves goodness, used for 
     * move ordering in the search algorithm.
     */
    int estimate;


    /**
     * Constructor
     */
    public Move(int from, int to, int p1, int p2, int promo) {
	this.from   = from;
	this.to     = to;
	this.p1     = p1;
	this.p2     = p2;
	//	this.status = status;
    }   // Move


    /**
     * This is required by the Comparable interface
     */
    public int compareTo(Object o) {
	Move m = (Move) o;
	return estimate - m.estimate;
    }   // compareTo


}
