//
// åäöÅÄÖ
// 229,228,246,197,196,214
// 196-229
//
// 0-31,32-126, 192-255
//     0xC0 - 0xFF.
// 'À'-'ÿ'
// 
// 			
//     horisontal tab (^I) ande vertical tab (?)
// '"
//

/**
 *
 * See: http://www.very-best.de/pgn-spec.htm
 *
 * @author Erik Bäckerud
 * @version 04-11-06
 */
class PGN
{

    /**
     * Convert a FEN position into a Board representation
     * in the supplied Board object.
     *
     * @param fen Text string describing this board setup in FEN
     *         (Forsyth-Edwards Notation) notation.
     * @param board
     * @return Board.WHITE or Board.BLACK depending on if white
     *         or black is next to move.
     */
    static int FENToBoard(String fen, Board board) {
	int colorToMove = Board.WHITE;
	return colorToMove;
    }   // FENToBoard

    /**
     * Convert the Board position in 'board' into a String in the FEN
     * (Forsyth-Edwards Notation) standard.
     *
     * @param board The playing board we are attempting to describe.
     * @param sideToMove either Board.WHITE or Board.BLACK depending
     *        on which side is about to move.
     * @return Description of the current state of this Board as 
     *         a String formatted according to the FEN standard.
     */
    static String BoardToFEN(Board board, int sideToMove) {
	StringBuffer buf = new StringBuffer();
	return buf.toString();
    }   // BoardToFEN


}
