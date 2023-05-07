//
//
//

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Vector;

/**
 * Xboard.java  --  Test communication with xboard GUI.
 */
class Xboard
{


    /**
     * Name of the logfile.
     */
    String filename = "/tmp/jack.log";


    /**
     * Append a line to the logfile.
     */
    void log(String str) {
	/*
	 */
	try {
	    OutputStream os = new FileOutputStream(filename, true);
	    PrintStream ps = new PrintStream(os, true);

            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm:ss");
            String dateString = sdf.format(new Date());

	    ps.println(dateString + " " + str);
	    ps.close();
	    os.close();
	} catch (IOException e) {
	    System.err.println("Error while writing to logfile.");
	    System.err.println(e);
	}
	/*
	 */
    }   // log

    /**
     * Check if we have pending input.
     */
    boolean checkinput() {
	int res = 0;
	do {
	    try {
		res = System.in.available();
	    } catch (IOException e) {
		System.err.println("Error checking stdin");
		System.err.println(e);
 log(e.toString());
		break;
	    }
	    if (res == 0) {
		try {
		    Thread.sleep(1000); 
		} catch (InterruptedException e) {
		    System.err.println("Exception e: " + e);	
 log(e.toString());
		    break;
		}
	    }
	} while (res == 0);
	return res > 0;
    }   // checkinput

    /**
     * Read one line from stdin.
     */
    String readline(InputStream is) {
	StringBuffer buf = new StringBuffer();
	int c;
	try {
	    while ((c = is.read()) >= 0) {
		char ch = (char) (c & 0xFF);
		if (ch != '\r' && ch != '\n') {
		    buf.append(ch);
		}
		if (ch == '\n') {
		    break;
		}
	    }
	} catch (IOException e) {
	    System.err.println("Error while reading from xboard.");
	    System.err.println(e);
 log(e.toString());
	}

	log("From engine: " + buf);

	return buf.toString().trim();
    }   // readline

    /**
     * Extract first number from a string
     */
    int pfd(String str) {
	int res = -999999999;
	String s = "";
	for (int i = 0; i < str.length(); i++) {
	    char c = str.charAt(i);
	    if ('0' <= c && c <= '9') {
		s = s + c;
	    } else if (s.length() > 0) {
		break;
	    }
	}
	if (s.length() > 0) {
	    res = Integer.parseInt(s);
	}
	return res;
    }   // pfd

    /**
     * See if this looks like an xboard move specification.
     * @param line the line of text received from xboard.
     * @return A Move object describing this move if found, else null.
     */
    Move parse(String line, int player, Board board) {
	Move move = null;
	if (line.length() < 4) {
	    return move;
	}

	// get row and column numbers in the range 0..7
	//	line = line.toLowerCase();
	int fromC = (line.charAt(0) & 0x7F) - 'a';
	int fromR = (line.charAt(1) & 0x7F) - '1';
	int toC =   (line.charAt(2) & 0x7F) - 'a';
	int toR =   (line.charAt(3) & 0x7F) - '1';
	int promo = 0;
	if (line.length() > 4) {
	    char pieceCode = line.charAt(4);
	    pieceCode = Character.toLowerCase(pieceCode);
	    switch (pieceCode) {
	    case 'n':
		promo = Board.KNIGHT;
		break;
	    case 'b':
		promo = Board.BISHOP;
		break;
	    case 'r':
		promo = Board.ROOK;
		break;
	    case 'q':
		promo = Board.QUEEN;
		break;
	    }
	}
	if (0 <= fromC && fromC <= 7 && 0 <= toC && toR <= 7) {
	    int from = fromR * 10 + fromC + 21;
	    int to   = toR * 10 + toC + 21;
	    int[] b = board.getBoard();
	    //	    int status = board.getStatus();
	    move = new Move(from, to, b[from], b[to], promo);
	}

	return move;
    }   // parse

    /**
     * Print our move in a format that xboard can grok.
     */
    void printMove(Move move) {
	String str = "";
	str = str + (char)(move.from % 10 + 'a' - 1);
	str = str + (char)(move.from / 10 + '1' - 2);
	str = str + (char)(move.to   % 10 + 'a' - 1);
	str = str + (char)(move.to   / 10 + '1' - 2);
	if (move.promo > 0) {
	    String s = "";
	    switch (move.promo) {
	    case Board.KNIGHT:
		s = "n";
		break;
	    case Board.BISHOP:
		s = "b";
		break;
	    case Board.ROOK:
		s = "r";
		break;
	    case Board.QUEEN:
		s = "q";
		break;
	    }
	    str = str + s;
	}
	System.out.print("move " + str);
    }   // printMove

    /**
     * Run a session against xboard (or WinBoard).
     */
    void xboard() {
	Board board = new Board();
	int depth = 8;
	int player = Board.WHITE;
	int me = Board.BLACK;
	int protover = 1;
	boolean force = false;
	boolean random = false;

	Eval eval = new E2();
	eval = new Eval();
	// eval = new E3();
	Search search = new AlphaBeta(depth, eval, false);

	for (;;) {
	    System.out.print("\n");
	    System.out.flush();
	    //	    checkinput();
	    String line = readline(System.in);
	    if (line.startsWith("quit")) {
		return;
	    } else if (line.startsWith("xboard")) {
	    } else if (line.startsWith("protover")) {
		protover = pfd(line);
	    } else if (line.startsWith("new")) {
		player = Board.WHITE;
		me = Board.BLACK;
		force = false;
		random = false;
		board = new Board();
	    } else if (line.startsWith("variant")) {
		System.out.print("Error (unknown command): variant");
	    } else if (line.startsWith("force")) {
		force = true;
	    } else if (line.startsWith("go")) {
		force = false;

		// Make the computers move.
		Vector pv = new Vector();
		int score;
		score  = search.search(me, board, pv);
		Move mymove = null;
		if (pv.size() > 0) {
		    mymove = (Move)pv.get(0);
		    printMove(mymove);
		    board.makeMove(mymove);
		}
	    } else if (line.startsWith("random")) {
		random = !random;
	    } else if (line.startsWith("white")) {
		player = Board.BLACK;
		me = Board.WHITE;
		force = true;
	    } else if (line.startsWith("black")) {
		player = Board.WHITE;
		me = Board.BLACK;
		force = true;
	    } else if (line.startsWith("level")) {
		// set time controls
		// !!! TODO !!!
	    } else if (line.startsWith("st")) {
		// set time control
		// !!! TODO !!!
	    } else if (line.startsWith("sd")) {
		depth = pfd(line);
	    } else if (line.startsWith("time")) {
		// my time
	    } else if (line.startsWith("otim")) {
		// opponents time
	    } else if (line.startsWith("?")) {
		// move now
	    } else if (line.startsWith("draw")) {
		// opponent offers to draw
	    } else if (line.startsWith("result")) {
	    } else if (line.startsWith("edit")) {
	    } else if (line.startsWith("hard")) {
	    } else if (line.startsWith("easy")) {
	    } else if (line.startsWith("hint")) {
	    } else if (line.startsWith("undo")) {
	    } else if (line.startsWith("remove")) {
	    } else if (line.startsWith("bk")) {
	    } else if (line.startsWith("post")) {
	    } else if (line.startsWith("nopost")) {
	    } else if (line.startsWith("analyze")) {
		System.out.print("Error (unknown command): analyze");
	    } else if (line.startsWith("name")) {
	    } else if (line.startsWith("rating")) {
	    } else if (line.startsWith("computer")) {
	    } else {
		// try to parse input as a move.
		Move move = parse(line, player, board);
		if (move != null) {
		    // Did the bastard put me in check mate?
		    board.makeMove(move);
		    if (board.isCheck(me) &&
			board.genMoves(me).size() == 0) {
			if (me == Board.BLACK) {
			    System.out.print("1-0 {White mates}");
			} else {
			    System.out.print("0-1 {Black mates}");
			}
		    }
		    
		    if (!force) {
			// Make the computers move.
			Vector pv = new Vector();
			int score;
			score  = search.search(me, board, pv);
			Move mymove = null;
			if (pv.size() > 0) {
			    mymove = (Move)pv.get(0);
			    printMove(mymove);
			    board.makeMove(mymove);
			}
			// Did I check-mate the bastard?
			if (board.isCheck(player) &&
			    board.genMoves(player).size() == 0) {
			    System.out.print("\n");
			    if (me == Board.BLACK) {
				System.out.print("0-1 {Black mates}");
			    } else {
				System.out.print("1-0 {White mates}");
			    }
			}
		    }
		} else {
		    System.out.print("Error (unknown command): " + line);
		}
	    }
	}
    }   // xboard

    /**
     * Main program.
     */
    public static void main(String []args) {
	Xboard obj = new Xboard();
	obj.xboard();
    }   // main


}
