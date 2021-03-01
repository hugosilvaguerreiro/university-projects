package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.services;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * A structure to hold service information.
 */
public class ServiceData {
    public WifiP2pDevice device;
    public String instanceName = null;
    public String serviceRegistrationType = null;
    public String username = null;
    public int id = -1;

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof  ServiceData)) return false;
        else {
            return  this.device.equals(((ServiceData) o).device) &&
                    this.instanceName.equals(((ServiceData) o).instanceName) &&
                    this.serviceRegistrationType.equals(((ServiceData) o).serviceRegistrationType);
        }
    }
}
