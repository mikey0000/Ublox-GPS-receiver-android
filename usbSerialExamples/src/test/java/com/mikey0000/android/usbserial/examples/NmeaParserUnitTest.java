package com.mikey0000.android.usbserial.examples;

import org.junit.Test;
import com.mikey0000.android.usbserial.examples.NMEAParser;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class NmeaParserUnitTest {

    NMEAParser nmeaParser;

    public NmeaParserUnitTest() {
        this.nmeaParser = new NMEAParser();
    }

    @Test
    public void testNmeaParser_GPGSA() {

        NMEAParser.GPSPosition position = this.nmeaParser.parse( "$GPGSA,A,3,04,06,10,12,13,14,15,17,18,,,,1.73,0.95,1.44*06" );

        // not yet supported - should be:
        //HDOP 0.95
        //VDOP 1.44
        //PDOP 1.73
    }

    @Test
    public void testNmeaParser_GPRMC() {

        NMEAParser.GPSPosition position = this.nmeaParser.parse( "$GPRMC,023003.10,A,5044.91818,N,00605.21390,E,0.000,29,040217,,,,D*57" );
        assertEquals( (double) position.lon, 6.0868983, 0.0000000999 );
    }

    @Test
    public void testNmeaParser_GPVTG() {

        NMEAParser.GPSPosition position = this.nmeaParser.parse( "$GPVTG,29,T,,M,0.000,N,0.000,K,D*2D" );
        // not supported - should be:
        // Course 29°
    }

    @Test
    public void testNmeaParser_GPGGA() {

        NMEAParser.GPSPosition position = this.nmeaParser.parse( "$GPGGA,023004.10,5044.91818,N,00605.21390,E,2,09,0.95,204.4,M,46.5,M,,*5D" );
        assertEquals( position.altitude, 204.4, 0.09 );

        //Also
        //HDOP 0.95
        //Satellites 9
    }

    @Test
    public void testNmeaParser_GPGLL() {

        NMEAParser.GPSPosition position = this.nmeaParser.parse( "$GPGLL,5044.91818,N,00605.21390,E,023004.10,A,D*6E" );
        // not supported - should be:
        //UTC time 2017-02-06T02:30:04.1Z
        //lat 50°44'55.09''N
        //lng 6°5'12.83''E
    }

    @Test
    public void testNmeaParser_GPGSV() {

        NMEAParser.GPSPosition position = this.nmeaParser.parse( "$GPGSV,4,1,16,04,,,,06,07,097,18,10,14,276,36,12,66,241,28*45" );
        // not supported - should be:
        //Satellites 16
    }

}
