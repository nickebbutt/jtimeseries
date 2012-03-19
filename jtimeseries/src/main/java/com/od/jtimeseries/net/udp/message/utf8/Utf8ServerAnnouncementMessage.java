package com.od.jtimeseries.net.udp.message.utf8;

import com.od.jtimeseries.net.udp.message.HttpServerAnnouncementMessage;
import com.od.jtimeseries.net.udp.message.MessageType;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 19/03/12
 * Time: 06:35
 *
 * Announce to a client that a timeseries http server is active
 */
public class Utf8ServerAnnouncementMessage extends Utf8AnnouncementMessage implements HttpServerAnnouncementMessage {

    public Utf8ServerAnnouncementMessage(int httpdPort, String host) {
        super(httpdPort, host);
    }

    public MessageType getMessageType() {
        return MessageType.SERVER_ANNOUNCE;
    }
}
