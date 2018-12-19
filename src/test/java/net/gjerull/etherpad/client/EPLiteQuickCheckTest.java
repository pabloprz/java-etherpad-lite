package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

@RunWith(JUnitQuickcheck.class)
public class EPLiteQuickCheckTest {

    private static final String RESPONSE_TEMPLATE = "{\n"
            + "  \"code\": %d,\n" + "  \"message\": \"%s\",\n"
            + "  \"data\": %s\n" + "}";
    private static final String API_VERSION = "1.2.12";
    private static final String ENCODING = "UTF-8";

    @Before
    public void setUp() {
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
                .getLogger("junit-quickcheck.value-reporting"))
                        .setLevel(
                                ch.qos.logback.classic.Level.OFF);
    }

    @Property
    public void domain_with_trailing_slash_when_construction_an_api_path(
            String apiPath) throws Exception {
        EPLiteConnection connection = new EPLiteConnection(
                "http://example.com/", "apikey", API_VERSION,
                ENCODING);
        String apiMethodPath = connection.apiPath(apiPath);
        assertEquals("/api/1.2.12/" + apiPath, apiMethodPath);
    }

    @Property
    public void query_string_from_map(String padId, int rev)
            throws Exception {
        EPLiteConnection connection = new EPLiteConnection(
                "http://example.com/", "apikey", API_VERSION,
                ENCODING);
        Map<String, Object> apiArgs = new TreeMap<>();
        apiArgs.put("padID", padId);
        apiArgs.put("rev", rev);

        String queryString = connection.queryString(apiArgs,
                false);

        assertEquals(
                "apikey=apikey&padID=" + padId + "&rev=" + rev,
                queryString);
    }

    @Property
    public void handle_valid_response_from_server(String pad1,
            String pad2, String pad3) throws Exception {
        EPLiteConnection connection = new EPLiteConnection(
                "http://example.com/", "apikey", API_VERSION,
                ENCODING);
        String listAllPads = "{\"padIDs\": [\n" + "  \"" + pad1
                + "\",\n" + "  \"" + pad2 + "\",\n" + "  \""
                + pad3 + "\"\n" + "]}";
        String serverResponse = String.format(RESPONSE_TEMPLATE,
                0, "no or wrong API Key", listAllPads);

        Map response = (Map) connection
                .handleResponse(serverResponse);

        assertEquals(pad1,
                ((List) response.get("padIDs")).get(0));
    }

}
