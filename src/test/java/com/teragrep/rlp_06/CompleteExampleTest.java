package com.teragrep.rlp_06;

import com.teragrep.rlp_01.RelpBatch;
import com.teragrep.rlp_01.RelpConnection;
import com.teragrep.rlp_03.RelpFrameServerRX;
import com.teragrep.rlp_03.Server;
import com.teragrep.rlp_03.SyslogRXFrameProcessor;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CompleteExampleTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompleteExampleTest.class);

    // list for local relp server which will contain sent records
    private final List<byte[]> recordList = new ArrayList<>();

    // address and port where local relp server is accessible
    private final String relpServerHostname = "127.0.0.1";
    private final int relpServerListenPort = 40601;

    // local relp server instance
    private Server relpServer;

    @BeforeAll
    public void setupLocalRelpServer() throws IOException {
        // launch local relp server
        Consumer<RelpFrameServerRX> cbFunction = relpFrameServerRX -> recordList.add(relpFrameServerRX.getData());
        relpServer = new Server(relpServerListenPort, new SyslogRXFrameProcessor(cbFunction));
        relpServer.start();
    }



    @Test
    public void relpExample() throws IOException, TimeoutException {
        // create relp connection object
        RelpConnection relpConnection = new RelpConnection();

        // connect to server
        relpConnection.connect(relpServerHostname, relpServerListenPort);

        // example syslog record for the relp transport, reuses SyslogRecordWithSDTest code
        String recordString = SyslogRecordWithSDTest.createSyslogRecord();
        byte[] record = recordString.getBytes(StandardCharsets.UTF_8);
        sendWithConnection(relpConnection, record);

        // disconnect
        relpConnection.disconnect();

        // check that server received everything
        Assertions.assertEquals(
                recordString // expected
                ,
                new String(
                        recordList.get(0),
                        StandardCharsets.UTF_8
                )
        );
    }

    private void sendWithConnection(RelpConnection relpConnection, byte[] record) {
        // create a relpBatch containing the syslog record
        RelpBatch relpBatch = new RelpBatch();
        relpBatch.insert(record);


        // this loop will keep re-sending until everything has been successfully accepted by the server
        boolean allSent = false;
        while (!allSent) {

            // commit relpBatch
            try {
                relpConnection.commit(relpBatch);
            } catch (IllegalStateException | IOException | java.util.concurrent.TimeoutException e) {
                LOGGER.error("error committing relpBatch", e);
            }

            // Check if everything has been sent
            if (!relpBatch.verifyTransactionAll()) {
                relpBatch.retryAllFailed();
                try {
                    // reconnect because commit did not succeed
                    relpConnection.tearDown();
                    Thread.sleep(500); // reconnect interval
                    relpConnection.connect(relpServerHostname, relpServerListenPort);
                } catch (IOException | TimeoutException | InterruptedException e) {
                    LOGGER.error("error reconnecting", e);
                    relpConnection.tearDown();
                }
            } else {
                allSent = true;
            }
        }
    }

    @AfterAll
    public void stopLocalRelpServer() throws InterruptedException {
        if (relpServer != null) {
            // shutdown local relp server
            relpServer.stop();
        }
    }
}
