package entralinked.model.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TrainerInfo {
    @JsonProperty(required = false) private String trainerName;
    @JsonProperty(required = false) private int trainerId;
    @JsonProperty(required = false) private int secretId;
    @JsonProperty(required = false) private int country;
    @JsonProperty(required = false) private int region;
    @JsonProperty(required = false) private TrainerGender gender;
    @JsonProperty(required = false) private Playtime playtime;
    @JsonProperty(required = false) private long adventureStartTime;
    @JsonProperty(required = false) private long money;

    @JsonProperty(required = false)
    @JsonDeserialize(contentAs = GymBadge.class)
    private final List<GymBadge> gymBadges = new ArrayList<>();

    public TrainerInfo() {}

    @JsonIgnore
    public TrainerInfo(
            String trainerName, int trainerId, int secretId, int country, int region, TrainerGender gender, Playtime playtime
    ) {
        this.trainerName = trainerName;
        this.trainerId = trainerId;
        this.secretId = secretId;
        this.country = country;
        this.region = region;
        this.gender = gender;
        this.playtime = playtime;
    }

    @JsonIgnore
    public void setAdventureStartTime(long adventureStartTime) {
        this.adventureStartTime = adventureStartTime;
    }

    @JsonIgnore
    public void setMoney(long money) {
        this.money = money;
    }

    @JsonIgnore
    public void setGymBadges(Collection<GymBadge> gymBadges) {
        if(gymBadges.size() <= 8) {
            this.gymBadges.clear();
            this.gymBadges.addAll(gymBadges);
        }
    }
}
