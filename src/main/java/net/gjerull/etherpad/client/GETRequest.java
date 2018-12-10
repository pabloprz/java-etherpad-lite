package net.gjerull.etherpad.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;

/**
 * A class for easily executing an HTTP GET request.<br />
 * <br />
 * Example:<br />
 * <br />
 * <code>
 * Request req = new GETRequest(url_object);<br />
 * String resp = req.send();<br />
 * </code>
 */
public class GETRequest implements Request {
    /** The url. */
    private final URL url;

    /** The Constant ETM_MONITOR. */
    private static final EtmMonitor ETM_MONITOR = EtmManager
            .getEtmMonitor();

    /**
     * Instantiates a new GETRequest.
     *
     * @param nUrl the URL object
     */
    public GETRequest(final URL nUrl) {
        this.url = nUrl;
    }

    /**
     * Sends the request and returns the response.
     *
     * @return String
     * @throws Exception the exception
     */
    public final String send() throws Exception {

        EtmPoint point = ETM_MONITOR
                .createPoint("Monitor point in GETRequest.send");

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            StringBuilder response = new StringBuilder();
            String buffer;

            while ((buffer = in.readLine()) != null) {
                response.append(buffer);
            }
            in.close();

            return response.toString();
        } finally {
            point.collect();
        }
    }
}
