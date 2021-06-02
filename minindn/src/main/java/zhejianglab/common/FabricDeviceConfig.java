package zhejianglab.common;

import org.onlab.packet.MacAddress;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.Config;

public class FabricDeviceConfig extends Config<DeviceId> {

    public static final String CONFIG_KEY = "fabricDeviceConfig";
    private static final String MY_STATION_MAC = "myStationMac";
    private static final String IS_SPINE = "isSpine";

    @Override
    public boolean isValid() {
        return hasOnlyFields(MY_STATION_MAC, IS_SPINE) &&
                myStationMac() != null;
    }

    /**
     * Gets the MAC address of the switch.
     *
     * @return MAC address of the switch. Or null if not configured.
     */
    public MacAddress myStationMac() {
        String mac = get(MY_STATION_MAC, null);
        return mac != null ? MacAddress.valueOf(mac) : null;
    }

    /**
     * Checks if the switch is a spine switch.
     *
     * @return true if the switch is a spine switch. false if the switch is not
     * a spine switch, or if the value is not configured.
     */
    public boolean isSpine() {
        String isSpine = get(IS_SPINE, null);
        return isSpine != null && Boolean.valueOf(isSpine);
    }
}
