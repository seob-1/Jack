//
// Eriks attempt at JUnit testing.
//

import junit.framework.*;

public class TestBoard extends TestCase {


    // Constructor, compulsory.
    public TestBoard(String name) {
	super(name);
    }   // TestBoard


    // Create a testsuite...
    public static Test suite() {
	/*
	 * the dynamic way
	 */
	return new TestSuite(TestBoard.class);
    }   // suite


    protected void setUp() {
    }   // setUp


    protected void tearDown() {
    }   // tearDown


    public void test1() {
    }   // test1


    /**
     * main
     */
    public static void main(String args[]) { 
	junit.textui.TestRunner.run(suite());
    }   // main


}
