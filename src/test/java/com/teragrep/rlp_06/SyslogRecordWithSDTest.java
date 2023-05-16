package com.teragrep.rlp_06;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.SDElement;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.UUID;

public class SyslogRecordWithSDTest {

    public static String createSyslogRecord() {
        Instant instant = Instant.parse("2023-01-01T00:00:00Z");

        String record = "{\"event\": \"login\", \"source\": \"127.0.0.1:38238\", \"username\": \"user1\", \"authentication method\": \"password\"}";


        SyslogMessage syslog = new SyslogMessage()
                .withTimestamp(instant.toEpochMilli())
                .withSeverity(Severity.INFORMATIONAL)
                .withAppName("web-shop-auth")
                .withHostname("app-server1.example.com")
                .withFacility(Facility.USER)
                .withMsg(record);


        // use real hostname here
        String realHostname = "localhost"; //InetAddress.getLocalHost().getHostName();

        // use original record uuid here, if available, otherwise use generated
        //String uuid = UUID.randomUUID().toString();
        String uuid = "9bcb400e-cec9-47d4-9930-0bae6325bbeb";

        // indicate the generation time of the syslog frame
        // instant = Instant.now();
        String headerGenerationTime = String.valueOf(instant.getEpochSecond());

        // indicate where header was generated at, this should be set "conversion-service" in case of a one
        String headerGenerationAt = "source";

        SDElement event_id_48577 = new SDElement("event_id@48577")
                .addSDParam("hostname", realHostname)
                .addSDParam("uuid", uuid)
                .addSDParam("source", headerGenerationAt)
                .addSDParam("unixtime", headerGenerationTime);


        SDElement origin_48577 = new SDElement("origin@48577")
                .addSDParam("hostname", realHostname);

        syslog = syslog
                .withSDElement(event_id_48577)
                .withSDElement(origin_48577);

        return syslog.toRfc5424SyslogMessage();
    }

    @Test
    public void exampleSyslogRecordWithSD() {
        String syslogRecord = createSyslogRecord();
        String expected = "<14>1 2023-01-01T00:00:00.000Z app-server1.example.com web-shop-auth - - [origin@48577 hostname=\"localhost\"][event_id@48577 hostname=\"localhost\" uuid=\"9bcb400e-cec9-47d4-9930-0bae6325bbeb\" source=\"source\" unixtime=\"1672531200\"] {\"event\": \"login\", \"source\": \"127.0.0.1:38238\", \"username\": \"user1\", \"authentication method\": \"password\"}";
        Assertions.assertEquals(expected, createSyslogRecord());
    }
}
