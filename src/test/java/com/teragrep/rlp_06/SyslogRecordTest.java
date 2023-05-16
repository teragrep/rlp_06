/*
   Java Reliable Event Logging Protocol Library RLP-01
   Copyright (C) 2023  Suomen Kanuuna Oy

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

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
