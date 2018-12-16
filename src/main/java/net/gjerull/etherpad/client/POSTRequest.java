package net.gjerull.etherpad.client;

import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * A class for easily executing an HTTP POST request.<br />
 * <br />
 * Example:<br />
 * <br />
 * <code>
 * Request req = new POSTRequest(url_object);<br />
 * String resp = req.send();<br />
 * </code>
 */
public class POSTRequest implements Request {

    /** The url. */
    private final URL url;

    /** The body. */
    private final String body;

    /** The Constant ETM_MONITOR. */
    private static final EtmMonitor ETM_MONITOR = EtmManager
            .getEtmMonitor();

    /**
     * Instantiates a new POSTRequest.
     *
     * @param nUrl  the URL object
     * @param nBody url-encoded
     *              (application/x-www-form-urlencoded) request
     *              body
     */
    public POSTRequest(final URL nUrl, final String nBody) {
        this.url = nUrl;
        this.body = nBody;
    }

    /**
     * Sends the request and returns the response.
     *
     * @return String
     * @throws Exception the exception
     */
    public final String send() throws Exception {

        EtmPoint point = ETM_MONITOR.createPoint(
                "Monitor point in POSTRequest.send");

        try {
            URLConnection con = this.url.openConnection();
            con.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(
                    con.getOutputStream(),
                    StandardCharsets.UTF_8);
            out.write(this.body);
            out.close();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(),
                            StandardCharsets.UTF_8));
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
