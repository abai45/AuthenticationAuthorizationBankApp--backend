package kz.group.reactAndSpring.event;

import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.enumeration.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class UserEvent {
    private UserEntity user;
    private EventType type;
    private Map<?, ?> data;
}
