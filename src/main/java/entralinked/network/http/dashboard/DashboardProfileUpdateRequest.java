package entralinked.network.http.dashboard;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import entralinked.model.avenue.AvenueVisitor;
import entralinked.model.player.DreamEncounter;
import entralinked.model.player.DreamItem;

@Deprecated
public record DashboardProfileUpdateRequest(
        @JsonProperty(required = true) @JsonDeserialize(contentAs = DreamEncounter.class) List<DreamEncounter> encounters,
        @JsonProperty(required = true) @JsonDeserialize(contentAs = DreamItem.class)      List<DreamItem> items,
        @JsonProperty(required = true) @JsonDeserialize(contentAs = AvenueVisitor.class)  List<AvenueVisitor> avenueVisitors,
        @JsonProperty(required = true) String cgearSkin,
        @JsonProperty(required = true) String dexSkin,
        @JsonProperty(required = true) String musical,
        @JsonProperty(required = true) int gainedLevels) {}
