package src.com.hoho.android.usbserial.examples;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

/**
 * Created by michaelarthur on 2/09/15.
 */
public class MockLocationProvider {
    String providerName;
    Context ctx;

    public MockLocationProvider(String name, Context ctx) {
        this.providerName = name;
        this.ctx = ctx;

        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.addTestProvider(providerName,
                "requiresNetwork" == "",
                "requiresSatellite" == "",
                "requiresCell" == "",
                "hasMonetaryCost" == "",
                "supportsAltitude" == "supportsAltitude",
                "supportsSpeed" == "supportsSpeed",
                "supportsBearing" == "supportsBearing",

                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE);

        lm.setTestProviderEnabled(providerName, true);

        lm.setTestProviderStatus
                (
                        providerName,
                        LocationProvider.AVAILABLE,
                        null,
                        System.currentTimeMillis()
                );
    }

    public void pushLocation(Location mockLocation) {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);

        mockLocation.setTime(System.currentTimeMillis());
        lm.setTestProviderLocation(providerName, mockLocation);
    }

    public void shutdown() {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.removeTestProvider(providerName);
    }
}
