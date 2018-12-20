package net.gjerull.etherpad.client;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GETRequest.class)
public class GETRequestMockitoTest {

    @Mock
    private URL mockedUrl;

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

        when(mockedUrl.openStream())
                .thenReturn(mockedInputStream);

        when(mockedBufferedReader.readLine())
                .thenReturn("Returned line", null);

        whenNew(BufferedReader.class).withAnyArguments()
                .thenReturn(mockedBufferedReader);

        whenNew(InputStreamReader.class).withAnyArguments()
                .thenReturn(mockedInputStreamReader);

        Request postRequest = new GETRequest(mockedUrl);

        Assert.assertEquals("Returned line", postRequest.send());
    }
}
