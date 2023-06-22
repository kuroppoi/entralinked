package entralinked.model.user;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public record UserDto(
        @JsonProperty(required = true)  String id,
        @JsonProperty(required = true)  String password,
        @JsonDeserialize(contentAs = GameProfileDto.class) Map<String, GameProfileDto> profiles) {
    
    public UserDto(User user) {
        this(user.getId(), user.getPassword(),
                user.getProfileMap().entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new GameProfileDto(entry.getValue()))));
    }
    
    public User toUser() {
        User user = new User(id, password);
        profiles.entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toProfile())) // Map GameProfileDto to GameProfile
            .forEach(user::addProfile); // Add each GameProfile to the User object
        return user;
    }
}
