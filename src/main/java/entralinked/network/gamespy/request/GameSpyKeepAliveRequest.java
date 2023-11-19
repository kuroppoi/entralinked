package entralinked.network.gamespy.request;

import entralinked.network.gamespy.GameSpyHandler;

public record GameSpyKeepAliveRequest() implements GameSpyRequest {

    @Override
    public void process(GameSpyHandler handler) {}
}
