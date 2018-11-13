package org.rfid.libdanrfid;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    	DDMfindings f = new DDMfindings(new DDMData());
        assertTrue( true );
    	assertEquals(f.good, f.good());
    	assertEquals(f.threshold, f.threshold());
    	f.setthreshold(0.6);
    	assertEquals(0.6, f.threshold());
    	assertTrue(f.trusted());
    	f.good=0.5;
    	assertFalse(f.trusted());
    	assertFalse(f.reverseOnBestGuess());
    	assertEquals(1, f.report().size());
    }
}
