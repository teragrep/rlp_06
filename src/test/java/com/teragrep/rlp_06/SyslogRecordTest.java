package com.teragrep.rlp_06;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class SyslogRecordTest {

    @Test
    public void exampleSyslogRecord() {

        Instant instant = Instant.parse("2023-01-01T00:00:00Z");

        String record = "{\"event\": \"login\", \"source\": \"127.0.0.1:38238\", \"username\": \"user1\", \"authentication method\": \"password\"}";

        SyslogMessage syslog = new SyslogMessage()
                .withTimestamp(instant.toEpochMilli())
                .withSeverity(Severity.INFORMATIONAL)
                .withAppName("web-shop-auth")
                .withHostname("app-server1.example.com")
                .withFacility(Facility.USER)
                .withMsg(record);

        String expected = "<14>1 2023-01-01T00:00:00.000Z app-server1.example.com web-shop-auth - - - {\"event\": \"login\", \"source\": \"127.0.0.1:38238\", \"username\": \"user1\", \"authentication method\": \"password\"}";
        Assertions.assertEquals(expected, syslog.toRfc5424SyslogMessage());
    }
}
