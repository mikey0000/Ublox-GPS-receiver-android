package com.mikey0000.android.usbserial.examples;

/**
 * Created by michaelarthur on 2/09/15.
 */

import android.location.Location;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;


public class NMEAParser {

    // java interfaces
    interface SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position);
    }

    // utils
    static float Latitude2Decimal(String lat, String NS) {

        float med = Float.parseFloat(lat.substring(2)) / 60.0f;
        med += Float.parseFloat(lat.substring(0, 2));
        if (NS.startsWith("S")) {
            med = -med;
        }
        return med;
    }

    static float Longitude2Decimal(String lon, String WE) {
        float med = Float.parseFloat(lon.substring(3)) / 60.0f;
        med += Float.parseFloat(lon.substring(0, 3));
        if (WE.startsWith("W")) {
            med = -med;
        }
        return med;
    }

    // parsers
    class GPGGA implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.time = Float.parseFloat(tokens[1]);
            position.lat = Latitude2Decimal(tokens[2], tokens[3]);
            position.lon = Longitude2Decimal(tokens[4], tokens[5]);
            position.quality = Integer.parseInt(tokens[6]);
            position.numberOfSatellites = Integer.parseInt(tokens[7]);
            position.hdop = Float.parseFloat(tokens[8]);
            position.altitude = Float.parseFloat(tokens[9]);
            return true;
        }
    }

    class GPGGL implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.lat = Latitude2Decimal(tokens[1], tokens[2]);
            position.lon = Longitude2Decimal(tokens[3], tokens[4]);
            position.time = Float.parseFloat(tokens[5]);
            return true;
        }
    }

    class GPRMC implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.time = Float.parseFloat(tokens[1]);
            position.lat = Latitude2Decimal(tokens[3], tokens[4]);
            position.lon = Longitude2Decimal(tokens[5], tokens[6]);
            position.velocity = Float.parseFloat(tokens[7]);
            position.dir = Float.parseFloat(tokens[8]);
            return true;
        }
    }

    class GPVTG implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.dir = Float.parseFloat(tokens[3]);
            return true;
        }
    }

    class GPRMZ implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.altitude = Float.parseFloat(tokens[1]);
            return true;
        }
    }

    public class GPSPosition {
        public float time = 0.0f;
        public float lat = 0.0f;
        public float lon = 0.0f;
        public int quality = 0;
        public float dir = 0.0f;
        public float altitude = 0.0f;
        public float velocity = 0.0f;
        public int numberOfSatellites = 0;
        // See: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation)
        public float hdop = 0.0f;

        public boolean fixed = false;

        public void updatefix() {
            fixed = quality > 0;
        }

        public String toString() {
            return String.format(
                    "POSITION: lat: %f, lon: %f, time: %f, Q: %d, hdop: %f, dir: %f, alt: %f, vel: %f, # of sat: %d",
                    lat, lon, time, quality, hdop, dir, altitude, velocity, numberOfSatellites
            );
        }
    }

    GPSPosition position = new GPSPosition();

    private static final Map<String, SentenceParser> sentenceParsers = new HashMap<String, SentenceParser>();

    /**
     * Maps a NMEA quality identifier to the best possible accuracy value (in meters)
     */
    protected static final Map<Integer,Float> qualityAccuracy = new HashMap<Integer, Float>();

    public NMEAParser() {
        sentenceParsers.put("GPGGA", new GPGGA());
        sentenceParsers.put("GPGGL", new GPGGL());
        sentenceParsers.put("GPRMC", new GPRMC());
        sentenceParsers.put("GPRMZ", new GPRMZ());
        //only really good GPS devices have this sentence but ...
        //sentenceParsers.put("GPVTG", new GPVTG());

        // concrete accuracy values (in meters) per NMEA quality level
        // this is used to calculate the position accuracy in NMEAParser.calculateAccuracy
        qualityAccuracy.put( 0, 0.0f ); // invalid
        qualityAccuracy.put( 1, 2.0f ); // GPS 2d/3d
        qualityAccuracy.put( 2, 0.7f ); // DGNSS
        qualityAccuracy.put( 4, 0.04f ); // RTK fixed
        qualityAccuracy.put( 5, 0.04f ); // RTK fixed
    }

    public GPSPosition parse(String line) {

        if (line.startsWith("$")) {
            String nmea = line.substring(1);
            String[] tokens = nmea.split(",");
            String type = tokens[0];
            //TODO check crc
            if (sentenceParsers.containsKey(type)) {
                try {
                    for (int c = 0; c < tokens.length; c++) {
                        String token = tokens[c];
                        if(token == "") {
                            tokens[c] = "0";
                        }
                    }
                    sentenceParsers.get(type).parse(tokens, position);
                } catch (Exception e) {
                }

            }
            position.updatefix();
        }

        return position;
    }

    public Location location(String str) {
        Location localLocation = null;
        parse( str );

        if( position.quality > 0 ) { // quality 0 is an invalid entry
            localLocation = new Location("gps");
            localLocation.setLongitude(position.lon);
            localLocation.setLatitude(position.lat);
            localLocation.setAltitude(position.altitude);
            localLocation.setSpeed(position.velocity);
            localLocation.setBearing(position.dir);
            localLocation.setTime(System.currentTimeMillis());
            localLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            localLocation.setAccuracy(calculateAccuracy());

            position = new GPSPosition();
        }

        return localLocation;
    }

    protected float calculateAccuracy() {

        return qualityAccuracy.get( position.quality ) * position.hdop;
    }
}