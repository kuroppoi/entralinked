package entralinked.network.gamespy.request;

import entralinked.network.gamespy.GameSpyHandler;
import entralinked.network.gamespy.message.GameSpyMessage;

@GameSpyMessage(name = "ka")
public record GameSpyKeepAliveRequest() implements GameSpyRequest {

    @Override
    public void process(GameSpyHandler handler) {
        handler.sendMessage(this); // Pong it back
    }
}
