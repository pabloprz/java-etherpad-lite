package net.gjerull.etherpad.client;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(POSTRequest.class)
public class POSTRequestMockitoTest {

    @Mock
    private URL mockedUrl;

    @Mock
    private URLConnection mockedConnection;

    @Mock
    private OutputStream mockedOutputStream;

    @Mock
    private OutputStreamWriter mockedOutputStreamWriter;

    @Mock
    private BufferedReader mockedBufferedReader;

    @Mock
    private InputStream mockedInputStream;

    @Mock
    private InputStreamReader mockedInputStreamReader;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void sendTest() throws Exception {

        when(mockedUrl.openConnection())
                .thenReturn(mockedConnection);

        when(mockedConnection.getOutputStream())
                .thenReturn(mockedOutputStream);

        when(mockedConnection.getInputStream())
                .thenReturn(mockedInputStream);

        when(mockedBufferedReader.readLine())
                .thenReturn("Returned line", null);

        whenNew(OutputStreamWriter.class).withAnyArguments()
                .thenReturn(mockedOutputStreamWriter);

        whenNew(BufferedReader.class).withAnyArguments()
                .thenReturn(mockedBufferedReader);

        whenNew(InputStreamReader.class).withAnyArguments()
                .thenReturn(mockedInputStreamReader);

        Request postRequest = new POSTRequest(mockedUrl,
                "nBody");

        Assert.assertEquals("Returned line", postRequest.send());

    }
}
