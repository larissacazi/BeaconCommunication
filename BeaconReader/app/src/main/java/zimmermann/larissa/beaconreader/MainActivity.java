package zimmermann.larissa.beaconreader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
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
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.scanner.CycledLeScanCallback;
import org.altbeacon.beacon.service.scanner.CycledLeScannerForLollipop;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BootstrapNotifier, RangeNotifier {
    protected static final String TAG = MainActivity.class.getName();
    public static final byte MANUFACTURER_DATA_PDU_TYPE = (byte) 0xff;

    private BeaconManager beaconManager;
    private RegionBootstrap mRegionBootstrap;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setDebug(true);
        beaconManager.getBeaconParsers().clear();
        //beaconManager.getBeaconParsers().add(new BeaconParser("animalltag")
        //        .setBeaconLayout("m:4-5=beac,i:6-21,i:22-23,i:24-25,p:26-26,d:27-27"));



        initBeaconManager();
        enableBackgroundScan(true);

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("all-beacon-region", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void initBeaconManager() {
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));

        //konkakt?
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setBackgroundScanPeriod(10000L);          // default is 10000L
        beaconManager.setForegroundBetweenScanPeriod(0L);      // default is 0L
        beaconManager.setForegroundScanPeriod(1100L);          // Default is 1100L
        beaconManager.addRangeNotifier(this);

        try {
            if (beaconManager.isAnyConsumerBound()) {
                beaconManager.updateScanPeriods();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "update scan periods error", e);
        }
    }

    public void enableBackgroundScan(boolean enable) {
        if (enable) {
            Log.d(TAG, "Enable Background Scan");
            enableRegions();
            //loadTrackedBeacons();
        } else {
            Log.d(TAG, "Disable Background Scan");
            disableRegions();
        }
    }

    private void disableRegions() {
        if (mRegionBootstrap != null) {
            mRegionBootstrap.disable();
        }
    }

    private void enableRegions() {
        mRegionBootstrap = new RegionBootstrap(this, new Region("all-beacon-region", null, null, null));
    }

    @Override
    public void didEnterRegion(Region region) {
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("all-beacon-region", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, "Error start ranging region: "+ e.getMessage());
        }
    }

    @Override
    public void didExitRegion(Region region) {
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(TAG, "Error stop ranging region: " + e.getMessage());
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        Log.d(TAG, "Region State  " + i + " region " + region);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (beacons != null && beacons.size() > 0 && region != null) {
            Iterator<Beacon> iterator = beacons.iterator();
            while (iterator.hasNext()) {
                Beacon beacon = iterator.next();
                Log.i(TAG, "Beacon - BluetoothAddress: " + beacon.getBluetoothAddress() + " BluetoothName:" + beacon.getBluetoothName()
                        + " ParserIdentifier:" + beacon.getParserIdentifier());
            }
        }
    }
}