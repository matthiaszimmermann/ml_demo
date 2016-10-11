package org.ece16.dl4j;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MnistReaderTest extends TestCase {
	
    public MnistReaderTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(MnistReaderTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }
}
