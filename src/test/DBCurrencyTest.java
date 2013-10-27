package test;

import static org.junit.Assert.assertEquals;
import infrastructure.DBCurrency;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Foo}.
 *
 * @author user@example.com (John Doe)
 */
@RunWith(JUnit4.class)
public class DBCurrencyTest {

    @Test
    public void thisAlwaysPasses() {
    	try {
    		DBCurrency dbCurrency = DBCurrency.getInstance();
    		Double expected = dbCurrency.getExchangeRate("EUR", "JPY");
    		Double actual = 675.6989214698231;
    		assertEquals("EUR to JPY", expected, actual);
    	} catch(Exception e) {
    		assert(false);
    	}
    }

    @Test
    @Ignore
    public void thisIsIgnored() {
    }
}