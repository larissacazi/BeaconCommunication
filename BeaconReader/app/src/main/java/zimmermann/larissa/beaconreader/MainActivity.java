package zimmermann.larissa.beaconreader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.scanner.CycledLeScanCallback;
import org.altbeacon.beacon.service.scanner.CycledLeScannerForLollipop;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, BootstrapNotifier {
    protected static final String TAG = MainActivity.class.getName();
    public static final byte MANUFACTURER_DATA_PDU_TYPE = (byte) 0xff;

    private BeaconManager beaconManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        //AltBeacon
        //beaconManager.setDebug(true);
        beaconManager.setBackgroundBetweenScanPeriod(30000);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(this);
        beaconManager.addRangeNotifier(this);

        try {
            //Identifier i = Identifier.parse("4d43c8e0-b24e-11e6-9598-0800200c9a66");
            //Identifier i2 = Identifier.parse("02");
            beaconManager.startMonitoringBeaconsInRegion(new Region("all-beacon-region", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, "ERROR:: " + e.getMessage());
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.v(TAG, "Beacons.size = " + beacons.size());
        Log.i(TAG, "Beacons.size = " + beacons.size());
        if (beacons.size() > 0) {
            Log.v(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
            for(Beacon beacon : beacons)
            {
                Log.v(TAG, "Beacon detected with id1: " + beacon.getId1() + " id2:" + beacon.getId2() + " id3: " + beacon.getId3() + " distance: " + beacon.getDistance());
                Log.v(TAG, "BluetoothName:" + beacon.getBluetoothName() + " BluetoothAddress:" + beacon.getBluetoothAddress());
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void didEnterRegion(Region region) {
        Log.v(TAG, "I just saw an beacon for the first time!");
        Log.v(TAG, "Region:: BluetoothAddress: " + region.getBluetoothAddress()
                + " Id1:" + region.getId1() + " Id2:" + region.getId2()
                + " Id3:" + region.getId3());

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG, "Error start ranging region: " + e.getMessage());
        }
    }

    @Override
    public void didExitRegion(Region region) {
        Log.v(TAG, "I no longer see an beacon");
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG, "Error stop ranging region: " + e.getMessage());
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.v(TAG, "I have just switched from seeing/not seeing beacons: "+state);
        Log.d(TAG, "Region State  " + state + " region " + region);
    }
}