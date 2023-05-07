//
//
//

import java.util.Random;
import java.util.Vector;
/**
 *
 * @author Erik Bäckerud
 * @version 04-10-19
 */
class Board implements Cloneable
{
    /*
     * Constant definitions used by the Board class
     */

    /**
     * Size of the (one-dimensional) array describing the chessboard.
     */
    static final int MAXINDEX = 120;

    static final int FREE = 0;
    static final int OFFBOARD = 0xFF;

    static final int WHITE = 8;
    static final int BLACK = 16;

    static final int PAWN   = 1;
    static final int KNIGHT = 2;
    static final int BISHOP = 3;
    static final int ROOK   = 4;
    static final int QUEEN  = 5;
    static final int KING   = 6;

    /**
     * Every square of the board is represented by one byte,
     * the low-order 3 bits give the piece type.
     */
    static final int PIECE_MASK = 7;
    /**
     * The bits 3-4 in the square give the pices color.
     * Zero if empy, 0x18 if out of bounds.
     */
    static final int COLOR_MASK = 24;		// (8 | 16) 

    /**
     * The bits 0..3 are enough to give piece+color information
     * for creating the Zobrist hash keys.
     */
    static final int HASH_MASK = 0x0F;

    /**
     * The EPP mask is used to get the En passant square from the status word.
     */
    static final int EPP_MASK = 0xFF;
    /**
     * The castling abilities flags in the status word.
     */
    static final int WKCF = 0x0100;		// white kingside  castling
    static final int WQCF = 0x0200;		// white queenside castling
    static final int BKCF = 0x0400;		// white kingside  castling
    static final int BQCF = 0x0800;		// white queenside castling
    /**
     * Is white or black next to move?
     */
    static final int WHITEMOVE = 0x1000;
    static final int BLACKMOVE = 0x2000;
    /**
     * Mask out color of next player to move
     */
    static final int MOVE_MASK = 0x3000;

    /**
     * limits that can be used for narrowing search to actual board.
     */
    static final int MINCOORD = 21;		// index of a1 square
    static final int MAXCOORD = 99;		// really max + 1

    /**
     * Set up all move deltas.
     */
    static final int rook_move[]   = {-10,  -1,   1,  10};
    static final int knight_move[] = {-21, -19, -12, -8,  8,  12,  19,  21};
    static final int bishop_move[] = {-11, -9,  9,  11};
    static final int queen_move[]  = {-11, -10,  -9, -1,  1,   9,  10,  11};
    // Kings & queens have the same move deltas.

    /*
     * Instance variables.
     */

    /**
     * The playing board
     */
    private int []board;

    /**
     * The low order eight bits of the status word give the 
     * "En passant" position, 0 if not applicable,
     * else the index of the square passed by pawn
     * moving two squares ahead in previous move.
     *
     * Also the status word has four bits for recording the castling status,
     * "one" means castling is possible and a zero bit in respective 
     * position means castling is no longer possible.
     *
     * The bits 12-13 tell us if white or black is to move.
     */
    private int status = WHITEMOVE | WKCF | WQCF | BKCF | BQCF;

    /**
     * Position of white king
     */
    int wkp = 0;

    /**
     * Position of black king
     */
    int bkp = 0;


    /**
     * Table holding the Zobrist hash-numbers for this board.
     */
    long [][]zobrist = new long[MAXINDEX][16];

    /**
     * Hashcode for the current playing board.
     */
    long hash;

    /**
     * History of hash-codes going back to the creation of this Board object.
     * Useful to avoid the three-identical-positions trap.
     */
    Vector history = new Vector();


    /**
     * Set up Zobrist table.
     */
    private void setupHash() {
	Random random = new Random(196);
	for (int r = 0; r < MAXINDEX; r++) {
	    for (int c = 0; c < 16; c++) {
		zobrist[r][c] = random.nextLong();
	    }
	    zobrist[r][0] = 0;	// Empty square with black piece?
	    zobrist[r][7] = 0;	// unused black piece
	    zobrist[r][8] = 0;	// Empty square with white piece?
	    zobrist[r][15] = 0;	// unused white piece
	}
    }   // setupHash

    /**
     * Generate the complete hashcode for this object,
     * should produce the same value as member variable 'hash'.
     */
    private long genHash() {
	long res = 0;
	for (int i = MINCOORD; i < MAXCOORD; i++) {
	    int p = board[i];
	    if (p != OFFBOARD && p != FREE) {
		p = p & HASH_MASK;
		res ^= zobrist[i][p];
	    }
	}
	res ^= status;
	return res;
    }   // genHash

    /**
     * Convert a character to a piece + color value,
     * utility for decoding FEN strings.
     */
    private int fenPiece(char ch) {
	int piece = 0;
	int color = BLACK;

	switch (Character.toLowerCase(ch)) {
	case 'p':
	    piece = PAWN;
	    break;
	case 'n':
	    piece = KNIGHT;
	    break;
	case 'b':
	    piece = BISHOP;
	    break;
	case 'r':
	    piece = ROOK;
	    break;
	case 'q':
	    piece = QUEEN;
	    break;
	case 'k':
	    piece = KING;
	    break;
	default:
	    throw new AssertionError ("Illegal FEN piece character: " + ch);
	    // break;
	}
	if ('A' <= ch && ch <= 'Z') {
	    color = WHITE;
	}

	return piece | color;
    }   // fenPiece

    /**
     * Alternative constructor.
     *
     * Generate a new board with pieces in the places described by the 
     * supplied FEN (or EPD) string.
     */
    public Board(String fen) {
	board = new int[MAXINDEX];
	// Make a new, completely empty board.
	for (int i = 0; i < board.length; i++) {
	    int row = i / 10 - 1;
	    int col = i % 10;
	    if (row < 1 || row > 8 || col < 1 || col > 8) {
		board[i] = OFFBOARD;
	    } else {
		board[i] = FREE;
	    }
	}

	String str;
	java.util.StringTokenizer st = new java.util.StringTokenizer(fen);
	if (st.hasMoreTokens()) {
	    str = st.nextToken();
	    int c = 0;
	    int r = 7;
	    for (int i = 0; i < str.length(); i++) {
		char ch = str.charAt(i);
		if (ch == '/') {
		    r--;
		    c = 0;
		} else if ('0' <= ch && ch <= '9') {
		    int d = ch - '0';
		    c += d;
		} else {
		    board[r * 10 + c + 21] = fenPiece(ch);
		    c++;
		}
	    }
	}

	if (st.hasMoreTokens()) {
	    str = st.nextToken();
	    if (str.equalsIgnoreCase("b")) {
		onTheMove(BLACK);
	    }
	}

	if (st.hasMoreTokens()) {
	    str = st.nextToken();
	    status &= ~(WKCF | WQCF | BKCF | BQCF);
	    //	    if (!str.equalsIgnoreCase("-")) {
	    for (int i = 0; i < str.length(); i++) {
		char ch = str.charAt(i);
		switch (ch) {
		case 'K':
		    status |= WKCF;
		    break;
		case 'Q':
		    status |= WQCF;
		    break;
		case 'k':
		    status |= BKCF;
		    break;
		case 'q':
		    status |= BQCF;
		    break;
		case '-':
		    break;
		default:
		    throw new AssertionError("Illegal FEN castling char: "+ch);
		}
	    }
	}

	if (st.hasMoreTokens()) {
	    str = st.nextToken().toLowerCase();
	    str = str + "00";
	    int c = str.charAt(0) - 'a';
	    int r = str.charAt(1) - '0' -1;
	    int epp = r * 10 + c + 21;
	    if (epp >= MINCOORD && epp < MAXCOORD) {
		status = (status & ~EPP_MASK) | epp;
	    }
	}

	for (int i = MINCOORD; i < MAXCOORD; i++) {
	    if ((board[i] & PIECE_MASK) == KING &&
		(board[i] & COLOR_MASK) == WHITE) {
		wkp = i;
	    }
	    if ((board[i] & PIECE_MASK) == KING &&
		(board[i] & COLOR_MASK) == BLACK) {
		bkp = i;
	    }
	}

	setupHash();
	hash = genHash();
	Long hobj = new Long(hash);
	history.add(hobj);
    }   // Board

    /**
     * Constructor.
     *
     * Generate a new board with all pieces in the standard start position.
     */
    public Board() {
	board = new int[MAXINDEX];
	// Make a new, completely empty board.
	for (int i = 0; i < board.length; i++) {
	    int row = i / 10 - 1;
	    int col = i % 10;
	    if (row < 1 || row > 8 || col < 1 || col > 8) {
		board[i] = OFFBOARD;
	    } else {
		board[i] = FREE;
	    }
	}
	// Now fill it with pieces.
	int r, c;
	for (c = 0; c < 8; c++) {
	    int i;
	    i = 1 * 10 + c + 21;
	    board[i] = WHITE | PAWN;
	    i = 6 * 10 + c + 21;
	    board[i] = BLACK | PAWN;
	}
	r = 7; c = 0; board[r * 10 + c + 21] = BLACK | ROOK;
	r = 7; c = 7; board[r * 10 + c + 21] = BLACK | ROOK;
	r = 0; c = 0; board[r * 10 + c + 21] = WHITE | ROOK;
	r = 0; c = 7; board[r * 10 + c + 21] = WHITE | ROOK;
	r = 7; c = 1; board[r * 10 + c + 21] = BLACK | KNIGHT;
	r = 7; c = 6; board[r * 10 + c + 21] = BLACK | KNIGHT;
	r = 0; c = 1; board[r * 10 + c + 21] = WHITE | KNIGHT;
	r = 0; c = 6; board[r * 10 + c + 21] = WHITE | KNIGHT;
	r = 7; c = 2; board[r * 10 + c + 21] = BLACK | BISHOP;
	r = 7; c = 5; board[r * 10 + c + 21] = BLACK | BISHOP;
	r = 0; c = 2; board[r * 10 + c + 21] = WHITE | BISHOP;
	r = 0; c = 5; board[r * 10 + c + 21] = WHITE | BISHOP;
	r = 7; c = 3; board[r * 10 + c + 21] = BLACK | QUEEN;
	r = 7; c = 4; board[r * 10 + c + 21] = BLACK | KING;
	r = 0; c = 3; board[r * 10 + c + 21] = WHITE | QUEEN;
	r = 0; c = 4; board[r * 10 + c + 21] = WHITE | KING;
	for (int i = MINCOORD; i < MAXCOORD; i++) {
	    if ((board[i] & PIECE_MASK) == KING &&
		(board[i] & COLOR_MASK) == WHITE) {
		wkp = i;
	    }
	    if ((board[i] & PIECE_MASK) == KING &&
		(board[i] & COLOR_MASK) == BLACK) {
		bkp = i;
	    }
	}

	setupHash();
	hash = genHash();
	Long hobj = new Long(hash);
	history.add(hobj);
    }   // Board

    /**
     * The evaluator wants to see our internal representation...
     */
    int [] getBoard() {
	return board;
    }   // getBoard

    /**
     * Change the side to move.
     *
     * @param player Color of the player to make the next move,
     *        WHITE or BLACK.
     */
    public void onTheMove(int player) {
	hash ^= status;
	int bits = WHITEMOVE;
	if (player == BLACK) {
	    bits = BLACKMOVE;
	}
	status = (status & ~MOVE_MASK) | bits;
	hash ^= status;
	if (history.size() == 1) {
	    history.remove(0);
	    Long hobj = new Long(hash);
	    history.add(hobj);
	}
    }   // onTheMove

    /**
     * make a move in our board.
     */
    public void makeMove(Move m) {
	hash ^= status;
	hash ^= zobrist[m.from][m.p1 & HASH_MASK];
	if (m.p2 != 0) {
	    hash ^= zobrist[m.to][m.p2 & HASH_MASK];
	}
	// the basics
	m.status = status;
	int color = m.p1 & COLOR_MASK;
	int piece = m.p1 & PIECE_MASK;

	// Sanity check
	if (board[m.from] == FREE || board[m.from] == OFFBOARD) {
	    throw new AssertionError ("Illegal move; " + m.from);
	}
	if (color == WHITE) {
	    if (piece == KING && wkp != m.from) {
		throw new AssertionError ("White king at unexpected pos.");
	    }
	    if ((status & MOVE_MASK) != WHITEMOVE) {
		throw new AssertionError ("White moved, expected black.");
	    }
	} else {
	    if (piece == KING && bkp != m.from) {
		throw new AssertionError ("Black king at unexpected pos.");
	    }
	    if ((status & MOVE_MASK) != BLACKMOVE) {
		throw new AssertionError ("Black moved, expected white.");
	    }
	}

	board[m.from] = FREE;
	board[m.to] = m.p1;
	hash ^= zobrist[m.to][m.p1 & HASH_MASK];

	// Do king-specific stuff...
	if (piece == KING) {
	    // update wkp, bkp
	    if (color == WHITE) {
		wkp = m.to;
	    } else {
		bkp = m.to;
	    }
	    // Check if this was a castling move.
	    if (m.from == 25 && m.to == 27) {
		hash ^= zobrist[28][board[28] & HASH_MASK];
		board[26] = board[28];
		board[28] = FREE;
		hash ^= zobrist[26][board[26] & HASH_MASK];

		if ((status & WKCF) == 0) {
		    throw new AssertionError ("Bad white kingside castling");
		}
	    } else if (m.from == 25 && m.to == 23) {
		hash ^= zobrist[21][board[21] & HASH_MASK];
		board[24] = board[21];
		board[21] = FREE;
		hash ^= zobrist[24][board[24] & HASH_MASK];

		if ((status & WQCF) == 0) {
		    throw new AssertionError ("Bad white queenside castling");
		}
	    } else if (m.from == 95 && m.to == 97) {
		hash ^= zobrist[98][board[98] & HASH_MASK];
		board[96] = board[98];
		board[98] = FREE;
		hash ^= zobrist[96][board[96] & HASH_MASK];

		if ((status & BKCF) == 0) {
		    throw new AssertionError ("Bad black kingside castling");
		}
	    } else if (m.from == 95 && m.to == 93) {
		hash ^= zobrist[91][board[91] & HASH_MASK];
		board[94] = board[91];
		board[91] = FREE;
		hash ^= zobrist[94][board[94] & HASH_MASK];

		if ((status & BQCF) == 0) {
		    throw new AssertionError ("Bad black queenside castling");
		}
	    }
	}
	// Check if we must update castling status flags
	if (piece == ROOK) {
	    if (color == WHITE) {
		if (m.to == 28 ||
		    (board[28] & COLOR_MASK) != WHITE ||
		    (board[28] & PIECE_MASK) != ROOK) {
		    status &= ~WKCF;
		}
		if (m.to == 21 ||
		    (board[21] & COLOR_MASK) != WHITE ||
		    (board[21] & PIECE_MASK) != ROOK) {
		    status &= ~WQCF;
		}
	    } else {
		if (m.to == 98 ||
		    (board[98] & COLOR_MASK) != BLACK ||
		    (board[98] & PIECE_MASK) != ROOK) {
		    status &= ~BKCF;
		}
		if (m.to == 91 ||
		    (board[91] & COLOR_MASK) != BLACK ||
		    (board[91] & PIECE_MASK) != ROOK) {
		    status &= ~BQCF;
		}
	    }
	} else if (piece == KING) {
	    if (color == WHITE) {
		status &= ~(WKCF | WQCF);
	    } else {
		status &= ~(BKCF | BQCF);
	    }
	}
	// Set the en passant status of this move
	int epp = 0;	// assume none, safe most of the time.
	if (piece == PAWN && m.p2 == FREE) {
	    // Check if this was an "en passant" capture.
	    if (m.from % 10 != m.to % 10) {
		// Calculate the position of the captured pawn.
		// System.out.println("made an en passant capture @" + m.to);
		int pp;
		if (color == WHITE) {
		    pp = m.to - 10;
		} else {
		    pp = m.to + 10;
		}

		// Some checks...
		int you = COLOR_MASK - color;
		if ((board[pp] & PIECE_MASK) != PAWN ||
		    (board[pp] & COLOR_MASK) != you) {
		    throw new AssertionError ("Illegal en passant move");
		}
		if ((status & EPP_MASK) != m.to) {
		    throw new AssertionError ("En passant flag error");
		}

		hash ^= zobrist[pp][board[pp] & HASH_MASK];
		board[pp] = FREE;
	    }

	    // Check if pawn moves two squares forward.
	    // Check could be stronger...
	    if (m.from / 10 == 3 &&  m.to / 10 == 5) {
		epp = m.from + 10;		// white pawn
	    } else if (m.from / 10 == 8 && m.to / 10 == 6) {
		epp = m.from - 10;		// black pawn
	    }
	}
	status = (status & ~EPP_MASK) | epp;

	// Check if we must promote a pawn.
	if (piece == PAWN && m.to / 10 == 9) {
	    // If not set, promote to queen, CHEATING, FIXME !!!
	    if ((m.promo) <= PAWN || m.promo >= KING) {
		m.promo = QUEEN;
	    }
	    // White pawn reached final row
	    board[m.to] = m.promo | WHITE;
	    // System.out.println("Promoted white pawn at file " + m.to % 10);
	    if ((m.promo) <= PAWN || m.promo >= KING) {
		throw new AssertionError ("Malformed white pawn promotion");
	    }
	    hash ^= zobrist[m.to][m.p1 & HASH_MASK];
	    hash ^= zobrist[m.to][board[m.to] & HASH_MASK];
	} else	if (piece == PAWN && m.to / 10 == 2) {
	    // If not set, promote to queen, CHEATING, FIXME !!!
	    if ((m.promo) <= PAWN || m.promo >= KING) {
		m.promo = QUEEN;
	    }
	    // Black pawn reached row 1
	    board[m.to] = m.promo | BLACK;
	    // System.out.println("Promoted black pawn at file " + m.to % 10);
	    if ((m.promo) <= Board.PAWN || m.promo >= Board.KING) {
		throw new AssertionError ("Malformed white pawn promotion");
	    }
	    hash ^= zobrist[m.to][m.p1 & HASH_MASK];
	    hash ^= zobrist[m.to][board[m.to] & HASH_MASK];
	}

	// Update the next to move indication.
	//	status ^= (WHITEMOVE | BLACKMOVE);
	status ^= MOVE_MASK;
	hash ^= status;
	// if (hash != genHash()) {
	// System.out.println(toString2());
	// throw new AssertionError ("hash error: " + m.from + " to " + m.to);
	// }
	Long hobj = new Long(hash);
	history.add(hobj);
    }   // makeMove

    /**
     * retract a previously made move.
     */
    public void retractMove(Move m) {
	hash ^= status;
	hash ^= zobrist[m.to][board[m.to] & HASH_MASK];
	// the basics
	int color = m.p1 & COLOR_MASK;
	int piece = m.p1 & PIECE_MASK;
	board[m.from] = m.p1;
	board[m.to]   = m.p2;
	status = m.status;
	hash ^= zobrist[m.from][m.p1 & HASH_MASK];
	if (m.p2 != 0) {
	    hash ^= zobrist[m.to][m.p2 & HASH_MASK];
	}

	// Do king stuff..
	if (piece == KING) {
	    // update wkp, bkp
	    if (color == WHITE) {
		wkp = m.from;
	    } else {
		bkp = m.from;
	    }
	    // Check if this was a castling move.
	    if (m.from == 25 && m.to == 27 && piece == KING) {
		hash ^= zobrist[26][board[26] & HASH_MASK];
		board[28] = board[26];
		board[26] = FREE;
		hash ^= zobrist[28][board[28] & HASH_MASK];
	    } else if (m.from == 25 && m.to == 23 && piece == KING) {
		hash ^= zobrist[24][board[24] & HASH_MASK];
		board[21] = board[24];
		board[24] = FREE;
		hash ^= zobrist[21][board[21] & HASH_MASK];
	    } else if (m.from == 95 && m.to == 97 && piece == KING) {
		hash ^= zobrist[96][board[96] & HASH_MASK];
		board[98] = board[96];
		board[96] = FREE;
		hash ^= zobrist[98][board[98] & HASH_MASK];
	    } else if (m.from == 95 && m.to == 93 && piece == KING) {
		hash ^= zobrist[94][board[94] & HASH_MASK];
		board[91] = board[94];
		board[94] = FREE;
		hash ^= zobrist[91][board[91] & HASH_MASK];
	    }
	}

	// Check if we need to restore an en passant capture
	if (piece == PAWN && m.p2 == FREE) {
	    // Check if this was an "en passant" capture.
	    if (m.from % 10 != m.to % 10) {
		// Calculate the position of the captured pawn.
		int pp;
		if (color == WHITE) {
		    pp = m.to - 10;
		} else {
		    pp = m.to + 10;
		}

		// Some checks...
		if (board[pp] != FREE) {
		    throw new AssertionError ("Illegal en passant restore");
		}

		int you = COLOR_MASK - color;
		board[pp] = PAWN | you;
		hash ^= zobrist[pp][board[pp] & HASH_MASK];
	    }
	}
	hash ^= status;
	int last = history.size() - 1;
	history.remove(last);
    }   // retractMove

    /**
     * Has the current position occured 3 times in a row?
     */
    public boolean repeat3() {
	Long hobj = (Long) history.get(history.size() - 1);
	int count = 0;
	for (int i = 0; i < history.size(); i++) {
	    Long ref = (Long) history.get(i);
	    if (ref.equals(hobj)) {
		count++;
	    }
	}
	if (count > 2) {
	    //	    System.out.println("Three repetitions: " + hash);
	}
	return count >= 3;
    }   // repeat3

    /**
     * Can player 'me' make this move (from, to)
     * without getting king in check?
     */
    private boolean safe_to_move(int me, int from, int to) {
	int res = 0;
	// previous piece @ square moved to, if any.
	int oldd = board[to];
	int olds = board[from];
	board[from] = FREE;
	board[to] = olds;
	int king_pos = wkp;
	if (me == BLACK) {
	    king_pos = bkp;
	}
	if ((olds & PIECE_MASK) == KING) {
	    king_pos = to;
	}

	int you = 24 - me;
	int p;
	// 1st we check to see if the king is threatened by a pawn.
	if (me == WHITE) {
	    p = board[king_pos+9];
	    if ((p & COLOR_MASK) == you && (p & PIECE_MASK) == PAWN) {
		res++;
	    }
	    p = board[king_pos+11];
	    if ((p & COLOR_MASK) == you && (p & PIECE_MASK) == PAWN) {
		res++;
	    }
	} else {
	    p = board[king_pos-9];
	    if ((p & COLOR_MASK) == you && (p & PIECE_MASK) == PAWN) {
		res++;
	    }
	    p = board[king_pos-11];
	    if ((p & COLOR_MASK) == you && (p & PIECE_MASK) == PAWN) {
		res++;
	    }
	}
	// 2_nd check if we are threatened by a knight.
	for (int d = 0; d < 8; d++) {
	    int u = king_pos + knight_move[d];
	    if ((board[u] & COLOR_MASK) == you &&
		(board[u] & PIECE_MASK) == KNIGHT) {
		res++;
	    }
	}
	// Check if we are threatened by a rook or queen (horizontal, vertical)
	for (int d = 0; d < 4; d++) {
	    int delta = rook_move[d];
	    int k = king_pos + delta;
	    while (board[k] == FREE) {
		k += delta;
	    }
	    p = board[k];
	    if ((p & COLOR_MASK) == you &&
		((p & PIECE_MASK) == ROOK || (p & PIECE_MASK) == QUEEN)) {
		res++;
	    }
	}
	// Check if we are threatened by a bishop or queen (diagonal)
	for (int d = 0; d < 4; d++) {
	    int delta = bishop_move[d];
	    int k = king_pos + delta;
	    while (board[k] == FREE) {
		k += delta;
	    }
	    p = board[k];
	    if ((p & COLOR_MASK) == you &&
		((p & PIECE_MASK) == BISHOP || (p & PIECE_MASK) == QUEEN)) {
		res++;
	    }
	}
	// Check if we are getting too close to the enemy king!
	for (int d = 0; d < queen_move.length; d++) {
	    int k = king_pos + queen_move[d];
	    //	    if ((board[k] & COLOR_MASK) == you &&
	    //		(board[k] & PIECE_MASK) == KING) {
	    if ((board[k] & PIECE_MASK) == KING) {
		res++;
		break;
	    }
	}

	// Restore the playing board to its previous state.
	board[from] = olds;
	board[to]   = oldd;
	return res == 0;
    }   /// safe_to_move

    /**
     * Is player 'me' in check?
     * <p>
     * Silly implementation, but what the...
     */
    boolean isCheck(int me) {
	int king_pos = wkp;
	if (me == BLACK) {
	    king_pos = bkp;
	}
	boolean res = !safe_to_move(me, king_pos, king_pos);
	return res;
    }   // isCheck

    /**
     * Print nice formatted position
     */
    String cor(int i) {
	String str = "_abcdefgh";
	String res = str.charAt(i % 10) + String.valueOf(i / 10 -1);
	return res;
    }  // cor

    /**
     * Find all legal moves in this board for player 'me'.
     * Return a Vector containing all legal moves out from this
     * position in any order.
     *
     */
    public Vector genMoves(int me) {
	Vector vec = new Vector();
	int p;
	int to;
	int delta;
	int you = 24 - me;

	int king_pos = wkp;
	if (me == BLACK) {
	    king_pos = bkp;
	}

	for (int i = MINCOORD; i < MAXCOORD; i++) {
	    if ((board[i] & COLOR_MASK) == me) {
		p = board[i] & PIECE_MASK;
		switch (p) {
		case PAWN:
 // !!! !!! FIXME !!! promotion on reaching final row !!!
		    // These variables needed to check for en passant capture.
		    int my_row;
		    int neighbour;
		    // Can I capture an enemy piece (right)?
		    if (me == WHITE) {
			to = i + 11;
			my_row = 5;
			neighbour = i + 1;
		    } else {
			to = i - 11;
			my_row = 4;
			neighbour = i - 1;
		    }
		    if ((board[to] & COLOR_MASK) == you &&
			(board[to] & PIECE_MASK) != KING &&
			safe_to_move(me, i, to)) {
			vec.add(new Move(i, to, board[i], board[to], 0));
		    }
		    // Check for en-passant capture (right).
		    if ((status & EPP_MASK) == to &&
			board[to] == FREE &&
			(i / 10 - 1) == my_row &&
			(board[neighbour] & COLOR_MASK) == you &&
			(board[neighbour] & PIECE_MASK) == PAWN) {
			int old = board[neighbour];
			board[neighbour] = FREE;
			if (safe_to_move(me, i, to)) {
			    vec.add(new Move(i, to, board[i], board[to], 0));
			    // System.out.println("generated an en passant capture (r) " + to);
			}
			board[neighbour] = old;
		    }

		    // Can I capture an enemy piece (left)?
		    if (me == WHITE) {
			to = i + 9;
			my_row = 5;
			neighbour = i - 1;
		    } else {
			to = i - 9;
			my_row = 4;
			neighbour = i + 1;
		    }
		    if ((board[to] & COLOR_MASK) == you &&
			(board[to] & PIECE_MASK) != KING &&
			safe_to_move(me, i, to)) {
			vec.add(new Move(i, to, board[i], board[to], 0));
		    }
		    // Check for en-passant capture (left).
		    if ((status & EPP_MASK) == to &&
			board[to] == FREE &&
			(i / 10 - 1) == my_row &&
			(board[neighbour] & COLOR_MASK) == you &&
			(board[neighbour] & PIECE_MASK) == PAWN) {
			int old = board[neighbour];
			board[neighbour] = FREE;
			if (safe_to_move(me, i, to)) {
			    vec.add(new Move(i, to, board[i], board[to], 0));
			    // System.out.println("generated an en passant capture (l) " + to);
			}
			board[neighbour] = old;
		    }

		    // Can I take one step forward?
		    if (me == WHITE) {
			to = i + 10;
		    } else {
			to = i - 10;
		    }
		    if (board[to] == FREE &&
			safe_to_move(me, i, to)) {
			vec.add(new Move(i, to, board[i], board[to], 0));
			// System.out.println("One step forward\n");
		    }
		    // Can I take two steps forward?
		    if (me == WHITE && (i / 10) == 3 && board[i+10] == FREE) {
			to = i + 20;
		    } else if ((i / 10) == 8 && board[i-10] == FREE) {
			to = i - 20;
		    } else {
			// two steps impossible
			break;
		    }
		    if (board[to] == FREE &&
			safe_to_move(me, i, to)) {
			vec.add(new Move(i, to, board[i], board[to], 0));
			// System.out.println("Two steps @ a time\n");
		    }
		    break;
		case KNIGHT:
		    for (int d = 0; d < 8; d++) {
			to = i + knight_move[d];
			if ((board[to] == FREE ||
			     ((board[to] & COLOR_MASK) == you &&
			      (board[to] & PIECE_MASK) != KING))) {
			    if (safe_to_move(me, i, to)) {
				vec.add(new Move(i, to,
						 board[i], board[to], 0));
			    }
			}
		    }
		    break;
		case BISHOP:
		    for (int d = 0; d < 4; d++) {
			delta = bishop_move[d];
			to = i + delta;
			while (board[to] == FREE) {
			    if (safe_to_move(me, i, to)) {
				vec.add(new Move(i, to, board[i], board[to], 0));
			    }
			    to += delta;
			}
			// can we make a capturing move?
			if ((board[to] & COLOR_MASK) == you &&
			    (board[to] & PIECE_MASK) != KING) {
			    if (safe_to_move(me, i, to)) {
				vec.add(new Move(i, to, board[i], board[to], 0));
			    }
			}
		    }
		    break;
		case ROOK:
		    for (int d = 0; d < 4; d++) {
			delta = rook_move[d];
			to = i + delta;
			while (board[to] == FREE) {
			    if (safe_to_move(me, i, to)) {
				vec.add(new Move(i, to, board[i], board[to], 0));
			    }
			    to += delta;
			}
			// can we make a capturing move?
			if ((board[to] & COLOR_MASK) == you &&
			    (board[to] & PIECE_MASK) != KING) {
			    if (safe_to_move(me, i, to)) {
				vec.add(new Move(i, to, board[i], board[to], 0));
			    }
			}
		    }
		    break;
		case QUEEN:
		    for (int d = 0; d < 8; d++) {
			delta = queen_move[d];
			to = i + delta;
			while (board[to] == FREE) {
			    if (safe_to_move(me, i, to)) {
				vec.add(new Move(i, to, board[i], board[to], 0));
			    }
			    to += delta;
			}
			// can we make a capturing move?
			if ((board[to] & COLOR_MASK) == you &&
			    (board[to] & PIECE_MASK) != KING) {
			    if (safe_to_move(me, i, to)) {
				vec.add(new Move(i, to, board[i], board[to], 0));
			    }
			}
		    }
		    break;
		case KING:
		    if (i != king_pos) {
			throw new AssertionError ("King pos. disagree; " +
						  " have: " + i +
						  " expected: " + king_pos);
		    }
		    for (int d = 0; d < 8; d++) {
			// king uses same move deltas as queen
			to = i + queen_move[d];
			if (board[to] == FREE ||
			    ((board[to] & COLOR_MASK) == you &&
			     (board[to] & PIECE_MASK) != KING)) {
			    if (safe_to_move(me, i, to)) {
				vec.add(new Move(i, to, board[i], board[to], 0));
			    }
			}
		    }
		    // Check if I can do King-side castling.
		    boolean ok;
		    int rook, square;
		    ok = true;
		    square = i + 1;		// final destination of rook.
		    to = i + 2;			// final position of king.
		    if (me == WHITE) {
			ok = (status & WKCF) != 0 && (i == 25);
			rook = 28;		// initial position of rook.
		    } else {
			ok = (status & BKCF) != 0 && (i == 95);
			rook = 98;
		    }
		    if (ok &&
			board[to] == FREE &&
			board[square] == FREE &&
			(board[rook] & COLOR_MASK) == me &&
			(board[rook] & PIECE_MASK) == ROOK ) {
			board[square] = board[rook];
			board[rook] = FREE;
			if (safe_to_move(me, i, i) &&
			    safe_to_move(me, i, to) &&
			    safe_to_move(me, i, square)) {
			    vec.add(new Move(i, to, board[i], board[to], 0));
			}
			board[rook] = board[square];
			board[square] = FREE;
		    }
		    // Check if I can do Queen-side castling.
		    ok = true;
		    square = i - 1;		// final destination of rook.
		    to = i - 2;			// final position of king
		    if (me == WHITE) {
			ok = (status & WQCF) != 0 && (i == 25);
			rook = 21;
		    } else {
			ok = (status & BQCF) != 0 && (i == 95);
			rook = 91;
		    }
		    if (ok &&
			board[to] == FREE &&
			board[square] == FREE &&
			board[rook+1] == FREE &&	// square next to rook!
			(board[rook] & COLOR_MASK) == me &&
			(board[rook] & PIECE_MASK) == ROOK ) {
			board[square] = board[rook];
			board[rook] = FREE;
			if (safe_to_move(me, i, i) &&
			    safe_to_move(me, i, to) &&
			    safe_to_move(me, i, square)) {
			    vec.add(new Move(i, to, board[i], board[to], 0));
			}
			board[rook] = board[square];
			board[square] = FREE;
		    }
		    break;
		default:
		    throw new AssertionError ("Unknown piece: " + board[i]);
		    //		    break;
		}
	    }
	}
	return vec;
    }   // genMoves

    /**
     * generate a clone of this object as required by 
     * <code>Cloneable</code> interface.
     */
    public Object clone() {
	Board copy = null;
	try {
	    copy = (Board) super.clone();
	} catch (CloneNotSupportedException e) {
	    // Should not happen
	    throw new InternalError(e.toString());
	}
	copy.board = (int[]) board.clone();
	return copy;
    }   // clone

    /**
     * Generate a String representation of the current board.
     *
     * @return Representation of board in the FEN format.
     */
    public String toString() {
	StringBuffer buf = new StringBuffer();
	for (int r = 7; r >= 0; r--) {
	    int emptypos = 0;
	    for (int c = 0; c < 8; c++) {
		int i = r * 10 + c + 21;
		int piece = board[i] & 7;
		int color = board[i] & 24;
		char ch = ' ';
		switch (piece) {
		case PAWN:	ch = 'P';	break;
		case ROOK:	ch = 'R';	break;
		case KNIGHT:	ch = 'N';	break;
		case BISHOP:	ch = 'B';	break;
		case QUEEN:	ch = 'Q';	break;
		case KING:	ch = 'K';	break;
		default:
		    break;
		}
		if (ch != ' ') {
		    if (emptypos > 0) {
			buf.append(emptypos);
		    }
		    if (color == BLACK) {
			ch = Character.toLowerCase(ch);
		    }
		    buf.append(ch);
		    emptypos = 0;
		} else {
		    emptypos++;
		}
	    }
	    if (emptypos > 0) {
		buf.append(emptypos);
	    }
	    if (r > 0) {
		buf.append("/");
	    }
	}
	// add in player on the move letter.
	if ((status & MOVE_MASK) == WHITEMOVE) {
	    buf.append(" w");
	} else {
	    buf.append(" b");
	}
	buf.append(" ");
	boolean cflag = false;
	// add in castling abilities
	if ((status & WKCF) != 0) {
	    buf.append("K");
	    cflag = true;
	}
	if ((status & WQCF) != 0) {
	    buf.append("Q");
	    cflag = true;
	}
	if ((status & BKCF) != 0) {
	    buf.append("k");
	    cflag = true;
	}
	if ((status & BQCF) != 0) {
	    buf.append("q");
	    cflag = true;
	}
	if (!cflag) {
	    buf.append("-");
	}
	// add info on en-passant square
	if ((status & EPP_MASK) != 0) {
	    char file = (char) ('a' + (status & EPP_MASK) % 10 - 1);
	    char rank = (char) ('0' + (status & EPP_MASK) / 10 - 1);
	    buf.append(" ");
	    buf.append(file);
	    buf.append(rank);
	} else {
	    buf.append(" ");
	    buf.append("-");
	}
	return buf.toString();
    }   // toString

    /**
     * Generate a String representation of the current board.
     *
     * @return graphic representation of board as ASCII art.
     */
    public String toString2() {
	StringBuffer buf = new StringBuffer();
	for (int r = 7; r >= 0; r--) {
	    buf.append((r+1) + "  ");
	    for (int c = 0; c < 8; c++) {
		int i = r * 10 + c + 21;
		int piece = board[i] & 7;
		int color = board[i] & 24;
		char ch = ' ';
		switch (piece) {
		case FREE:	ch = '.';	break;
		case PAWN:	ch = 'P';	break;
		case ROOK:	ch = 'R';	break;
		case KNIGHT:	ch = 'N';	break;
		case BISHOP:	ch = 'B';	break;
		case QUEEN:	ch = 'Q';	break;
		case KING:	ch = 'K';	break;
		default:
		    break;
		}
		if (ch != ' ') {
		    if (color == BLACK) {
			ch = Character.toLowerCase(ch);
		    }
		    buf.append(" " + ch);
		}
	    }
	    buf.append("\n");
	}
	buf.append("\n");
	buf.append("    A B C D E F G H\n");
	return buf.toString();
    }   // toString2

}
