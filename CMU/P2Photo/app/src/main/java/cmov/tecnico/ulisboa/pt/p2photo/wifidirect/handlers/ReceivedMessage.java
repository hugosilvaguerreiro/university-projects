package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;

class ReceivedMessage {
    public InetSocketAddress remoteAddress;
    public JSONObject result;

    public ReceivedMessage(InetSocketAddress remoteAddress, JSONObject result) {
        this.remoteAddress = remoteAddress;
        this.result = result;

    }
}