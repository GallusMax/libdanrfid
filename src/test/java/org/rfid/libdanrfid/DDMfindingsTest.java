package org.rfid.libdanrfid;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class DDMfindingsTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void testTrivia() {
    	double delta = 1e-6;
    	DDMfindings f = new DDMfindings(new DDMData());
        assertTrue( true );
    	assertEquals(f.good, f.good(),delta);
    	assertEquals(f.threshold, f.threshold(), delta);
    	f.setthreshold(0.6);
    	assertEquals(0.6, f.threshold(), delta);
    	assertTrue(f.trusted());
    	f.good=0.5;
    	assertFalse(f.trusted());
    	assertFalse(f.reverseOnBestGuess());
    	assertEquals(1, f.report().size());
    }
}
