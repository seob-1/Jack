/**
 * The static evaluator.
 * Called when we have reached the bottom of the search tree.
 *
 * @author Erik Bäckerud
 * @version 060601
 */
class E3 extends Eval
{


    /**
     * Score given to a square not threatened by one side.
     */
    static final int UNUSED = 100;

    /**
     * Score the pieces
     */
    static final int [] material = {0, 100, 300, 325, 500, 900, 0};

    // int [] wthr = new int [Board.MAXINDEX];	// # of threats from white
    // int [] bthr = new int [Board.MAXINDEX];
    int [] wmin = new int [Board.MAXINDEX];	// Cheapest piece threatening
    int [] bmin = new int [Board.MAXINDEX];
    boolean [] pin = new boolean[Board.MAXINDEX];

    /**
     * Coordinates of the center squares.
     */
    static final int [] center = {54, 55, 64, 65};


    /**
     * Return player name for piece as String.
     */
    private String pcs(int piece) {
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
     * format position
     */
    private String alg(int p) {
	String str = "_abcdefgh";
	int row = p / 10 -1;
	int col = p % 10;
	String res = str.charAt(col) + String.valueOf(row);
	return res;
    }  // alg

    /**
     * Mark all pinned pieces in the array 'pin'.
     *
     * @param me
     * @param king_pos
     * @param board
     */
    private void pinned(int me, int king_pos, int[] board) {
	int you = 24 - me;
	int p;  // The piece supplying the threat (bishop, rook or queen).

	// Check if we are pinned by a rook or queen (horizontal, vertical)
	for (int d = 0; d < 4; d++) {
	    int delta = Board.rook_move[d];
	    int k = king_pos + delta;
	    int n = 0;	// number of own pieces passed
	    int ego = 0; // position of my pinned piece
	    while (board[k] != Board.OFFBOARD &&
		   (board[k] & Board.COLOR_MASK) != you) {
		if ((board[k] & Board.COLOR_MASK) == me) {
		    n++;
		    ego = k;
		}
		k += delta;
	    }
	    p = board[k];
	    if (n == 1 &&
		(p & Board.COLOR_MASK) == you &&
		((p & Board.PIECE_MASK) == Board.ROOK ||
		 (p & Board.PIECE_MASK) == Board.QUEEN)) {
		int pp = board[ego] & Board.PIECE_MASK;
		if (true || pp > Board.PAWN) {
		    pin[ego] = true;
 // System.out.println(pcs(pp)+" at "+alg(ego)+" pinned by "+
 // pcs(p & Board.PIECE_MASK) +" at "+ alg(k));
		}
	    }
	}
	// Check if we are pinned by a bishop or queen (diagonal)
	for (int d = 0; d < 4; d++) {
	    int delta = Board.bishop_move[d];
	    int k = king_pos + delta;
	    int n = 0;	// number of own pieces passed
	    int ego = 0; // position of my pinned piece
	    while (board[k] != Board.OFFBOARD &&
		   (board[k] & Board.COLOR_MASK) != you) {
		if ((board[k] & Board.COLOR_MASK) == me) {
		    n++;
		    ego = k;
		}
		k += delta;
	    }
	    p = board[k];
	    if (n == 1 &&
		(p & Board.COLOR_MASK) == you &&
		((p & Board.PIECE_MASK) == Board.BISHOP ||
		 (p & Board.PIECE_MASK) == Board.QUEEN)) {
		int pp = board[ego] & Board.PIECE_MASK;
		if (true || pp > Board.PAWN) {
		    pin[ego] = true;
		}
	    }
	}
    }   // pinned

    /**
     * See if player 'me' is in check at this point in the game.
     */
    private boolean isCheck(int me, int[] board, int king_pos) {
	int you = 24 - me;
	int p;
	// 1st we check to see if the king is threatened by a pawn.
	if (me == Board.WHITE) {
	    p = board[king_pos+9];
	    if ((p & Board.COLOR_MASK) == you &&
		(p & Board.PIECE_MASK) == Board.PAWN) {
		return true;
	    }
	    p = board[king_pos+11];
	    if ((p & Board.COLOR_MASK) == you &&
		(p & Board.PIECE_MASK) == Board.PAWN) {
		return true;
	    }
	} else {
	    p = board[king_pos-9];
	    if ((p & Board.COLOR_MASK) == you &&
		(p & Board.PIECE_MASK) == Board.PAWN) {
		return true;
	    }
	    p = board[king_pos-11];
	    if ((p & Board.COLOR_MASK) == you &&
		(p & Board.PIECE_MASK) == Board.PAWN) {
		return true;
	    }
	}
	// 2_nd check if we are threatened by a knight.
	for (int d = 0; d < 8; d++) {
	    int u = king_pos + Board.knight_move[d];
	    if ((board[u] & Board.COLOR_MASK) == you &&
		(board[u] & Board.PIECE_MASK) == Board.KNIGHT) {
		return true;
	    }
	}
	// Check if we are threatened by a rook or queen (horizontal, vertical)
	for (int d = 0; d < 4; d++) {
	    int delta = Board.rook_move[d];
	    int k = king_pos + delta;
	    while (board[k] == Board.FREE) {
		k += delta;
	    }
	    p = board[k];
	    if ((p & Board.COLOR_MASK) == you &&
		((p & Board.PIECE_MASK) == Board.ROOK ||
		 (p & Board.PIECE_MASK) == Board.QUEEN)) {
		return true;
	    }
	}
	// Check if we are threatened by a bishop or queen (diagonal)
	for (int d = 0; d < 4; d++) {
	    int delta = Board.bishop_move[d];
	    int k = king_pos + delta;
	    while (board[k] == Board.FREE) {
		k += delta;
	    }
	    p = board[k];
	    if ((p & Board.COLOR_MASK) == you &&
		((p & Board.PIECE_MASK) == Board.BISHOP ||
		 (p & Board.PIECE_MASK) == Board.QUEEN)) {
		return true;
	    }
	}
	return false;
    }   // isCheck

    /**
     * Compute the score for threats, the threat-matrix.
     * Computed from the perspective of white,
     * so positive if white is ahead, else negative.
     */
    private int threat(int [] board) {
	int piece, color;
	int to;
	int delta;
	int check = 0;

	// initiate minimum threats to impossible values.
	for (int i = Board.MINCOORD; i < Board.MAXCOORD; i++) {
	    wmin[i] = UNUSED;
	    bmin[i] = UNUSED;
	}

	int mtrl = 0;
	// Set up the threat matrix.
	// Disregard en passant captures for now.
	for (int i = Board.MINCOORD; i < Board.MAXCOORD; i++) {
	    if (board[i] != Board.FREE &&
		board[i] != Board.OFFBOARD &&
		!pin[i]) {
		piece = board[i] & Board.PIECE_MASK;
		color = board[i] & Board.COLOR_MASK;

		if (color == Board.WHITE) {
		    mtrl += material[piece];
		} else {
		    mtrl -= material[piece];
		}
		switch (piece) {
		case Board.PAWN:
		    if (color == Board.WHITE) {
			// Threat enemy piece (right)?
			to = i + 11;
			//			wthr[to]++;
			wmin[to] = Board.PAWN;
			// Threat enemy piece (left)?
			to = i + 9;
			//			wthr[to]++;
			wmin[to] = Board.PAWN;
		    } else {
			to = i - 11;
			bmin[to] = Board.PAWN;
			to = i - 9;
			bmin[to] = Board.PAWN;
		    }
		    break;
		case Board.KNIGHT:
		    for (int d = 0; d < 8; d++) {
			to = i + Board.knight_move[d];
			if (color == Board.WHITE) {
			    if (wmin[to] > Board.KNIGHT) {
				wmin[to] = Board.KNIGHT;
			    }
			} else {
			    if (bmin[to] > Board.KNIGHT) {
				bmin[to] = Board.KNIGHT;
			    }
			}
		    }
		    break;
		case Board.BISHOP:
		    for (int d = 0; d < 4; d++) {
			delta = Board.bishop_move[d];
			to = i + delta;
			while (board[to] == Board.FREE) {
			    if (color == Board.WHITE) {
				if (wmin[to] > Board.BISHOP) {
				    wmin[to] = Board.BISHOP;
				}
			    } else {
				if (bmin[to] > Board.BISHOP) {
				    bmin[to] = Board.BISHOP;
				}
			    }
			    to += delta;
			}
			if (color == Board.WHITE) {
			    if (wmin[to] > Board.BISHOP) {
				wmin[to] = Board.BISHOP;
			    }
			} else {
			    if (bmin[to] > Board.BISHOP) {
				bmin[to] = Board.BISHOP;
			    }
			}
		    }
		    break;
		case Board.ROOK:
		    for (int d = 0; d < 4; d++) {
			delta = Board.rook_move[d];
			to = i + delta;
			while (board[to] == Board.FREE) {
			    if (color == Board.WHITE) {
				if (wmin[to] > Board.ROOK) {
				    wmin[to] = Board.ROOK;
				}
			    } else {
				if (bmin[to] > Board.ROOK) {
				    bmin[to] = Board.ROOK;
				}
			    }
			    to += delta;
			}
			if (color == Board.WHITE) {
			    if (wmin[to] > Board.ROOK) {
				wmin[to] = Board.ROOK;
			    }
			} else {
			    if (bmin[to] > Board.ROOK) {
				bmin[to] = Board.ROOK;
			    }
			}
		    }
		    break;
		case Board.QUEEN:
		    for (int d = 0; d < 8; d++) {
			delta = Board.queen_move[d];
			to = i + delta;
			while (board[to] == Board.FREE) {
			    if (color == Board.WHITE) {
				if (wmin[to] > Board.QUEEN) {
				    wmin[to] = Board.QUEEN;
				}
			    } else {
				if (bmin[to] > Board.QUEEN) {
				    bmin[to] = Board.QUEEN;
				}
			    }
			    to += delta;
			}
			if (color == Board.WHITE) {
			    if (wmin[to] > Board.QUEEN) {
				wmin[to] = Board.QUEEN;
			    }
			} else {
			    if (bmin[to] > Board.QUEEN) {
				bmin[to] = Board.QUEEN;
			    }
			}
		    }
		    break;
		case Board.KING:
		    for (int d = 0; d < 8; d++) {
			to = i + Board.queen_move[d];
			if ((board[to] & Board.PIECE_MASK) != Board.KING) {
			    if (color == Board.WHITE) {
				if (wmin[to] > Board.KING) {
				    wmin[to] = Board.KING;
				}
			    } else {
				if (bmin[to] > Board.KING) {
				    bmin[to] = Board.KING;
				}
			    }
			}
		    }
		    if (isCheck(color, board, i)) {
 // System.out.println(pcs(piece)+" at "+alg(i)+" is in check");
			if (color == Board.WHITE) {
			    check -= 35;
			} else {
			    check += 35;
			}
		    }
		    break;
		default:
		    throw new AssertionError ("Unknown piece: " + board[i]);
		}
	    }
	}

	// Now scan the threat matrix assigning scores.
	int white = 0;
	int black = 0;

	if (true) {
	// Check for center control.
	for (int i = 0; i < center.length; i++) {
	    int pos = center[i];
	    if (board[pos] == Board.FREE) {
		if (wmin[pos] < UNUSED && bmin[pos] >= UNUSED) {
		    white += 1;
		} else if (bmin[pos] < UNUSED && wmin[pos] >= UNUSED) {
		    black += 1;
		}
	    } else if ((board[pos] & Board.COLOR_MASK) == Board.WHITE) {
		white += 2;
	    } else {
		black += 2;
	    }
	}
	}

	for (int i = Board.MINCOORD; i < Board.MAXCOORD; i++) {
	    if (board[i] == Board.FREE) {
		// Threatening empty squares?
 //		if (wmin[i] < UNUSED) {
 //		    white += 1;
 //		}
 //		if (bmin[i] < UNUSED) {
 //		    black += 1;
 //		}
	    } else if (board[i] != Board.OFFBOARD) {
		piece = board[i] & Board.PIECE_MASK;
		color = board[i] & Board.COLOR_MASK;
		if (piece == Board.KING) {
		    // Check for threats around the king
		    if (color == Board.WHITE) {
			for (int d = 0; d < 8; d++) {
			    to = i + Board.queen_move[d];
			    if (board[to] != Board.OFFBOARD &&
				bmin[to] < UNUSED) {
				white -= 2;
			    }
			}
		    } else {	// color == Board.BLACK
			for (int d = 0; d < 8; d++) {
			    to = i + Board.queen_move[d];
			    if (board[to] != Board.OFFBOARD && 
				wmin[to] < UNUSED) {
				black -= 2;
			    }
			}
		    }
		} else {
		    if (color == Board.WHITE) {
			if (bmin[i] < UNUSED && wmin[i] >= UNUSED) {
			    // this piece is left en prise.
			    white -= 20 + 2 * piece - bmin[i];
			} else if (piece > bmin[i]) {
			    // an under-defended piece, gain for opponent.
			    white -= 10 + (piece - bmin[i]);
			} else if (piece >= bmin[i] ) {
			    // trading equal for equal, not always a win.
			    if (mtrl < 0) {
				white -= 3;
			    }
			}
		    } else {	// color == Board.BLACK
			if (wmin[i] < UNUSED && bmin[i] >= UNUSED) {
			    black -= 20 + 2 * piece - wmin[i];
			} else if (piece > wmin[i]) {
			    black -= 10 + (piece - wmin[i]);
			} else if (piece >= wmin[i]) {
			    if (mtrl > 0) {
				black -= 3;
			    }
			}
		    }
		}
	    }
	}

	int res = white - black;
	res = res + check;
	return res;
    }   // threat

    /**
     * Determine the static goodlihood of this position.
     *
     * Note! this version depends on knowledge of the internal 
     * representation of boards in class Board.
     *
     * @param player either WHITE or BLACK.
     * @param b the current playing board to evaluate.
     */
    public int eval(int player, Board b) {
	// System.out.println(b.toString2());

	for (int i = Board.MINCOORD; i < Board.MAXCOORD; i++) {
	    pin[i]  = false;
	}

	int [] board = b.getBoard();
	// How much material has each side got?
	int white = 0;
	int black = 0;

	int piece, color;
	int ap = 0;	// bonus for advancing pawns.
	for (int i = Board.MINCOORD; i < Board.MAXCOORD; i++) {
	    int v = 0;		// Material score.
	    int bonus = 0;
	    if (board[i] != Board.FREE && board[i] != Board.OFFBOARD) {
		piece = board[i] & Board.PIECE_MASK;
		color = board[i] & Board.COLOR_MASK;
		v = material[piece];
		switch (piece) {
		case Board.PAWN:
		    // Calculate bonus for pawn advancement
		    int delta, rowix;
		    if (color == Board.WHITE) {
			rowix = (i / 10) - 3;
			delta =  10;
		    } else {
			rowix = 8 - (i / 10);
			delta = -10;
		    }
		    //		    bonus = (rowix * rowix);
		    bonus += rowix * 2;		// 0 .. 12 depending on rank.
		    int pos = i + delta;
		    while (board[pos] != Board.OFFBOARD &&
			   (board[pos] & Board.PIECE_MASK) != Board.PAWN) {
			pos += delta;
		    }
 		    if ((board[pos] & Board.PIECE_MASK) == piece &&
			(board[pos] & Board.COLOR_MASK) == color) {
			// same colored pawn on same file (rank?) 
			/*
			System.out.println("POS " + b.cor(i) +
					   "  doubled pawn @ " +
					   b.cor(pos));
			 */
			bonus -= 20;
		    }

		    pos = i + delta;
		    while (board[pos] == Board.FREE) {
			pos += delta;
		    }
		    if (board[pos] == Board.OFFBOARD) {
			// potential promotion bonus, (open file to end).
			// System.out.println("promobon: " + b.cor(i));
			bonus += 10 + rowix * rowix;
		    }

		    // Small bonus for pawns on same rank
		    if ((board[i+1] & Board.PIECE_MASK) == piece &&
			(board[i+1] & Board.COLOR_MASK) == color) {
			bonus += 3;
		    }

		    break;
		case Board.KNIGHT:
		    // Calculate penalty for "a knight on the rim is dim".
		    int row = i / 10 - 1;
		    int col = i % 10;
		    if (row == 1 || row == 8) {
			bonus = -10;
		    }
		    else
		    if (col == 1 || col == 8) {
			bonus = -10;
		    }

		    break;
		case Board.BISHOP:
		    break;
		case Board.ROOK:
		    break;
		case Board.QUEEN:
		    break;
		case Board.KING:
		    pinned(color, i, board);
		    break;
		default:
		    break;
		}
		if (color == Board.WHITE) {
		    white += v + bonus;
		} else {
		    black += v + bonus;
		}
	    }
	}

	// Compute material score and position bonus.
	int res = white - black;

	// Compute the threat (positive if white is ahead).
	int t = threat(board);
	res += t;

	if (player == Board.BLACK) {
	    res = -res;
	}

	return res;
    }   // eval


}
