package kz.group.reactAndSpring.enumeration;

import static kz.group.reactAndSpring.constant.Constants.*;

public enum AuthorityEnum {
    USER(USER_AUTHORITIES),
    ADMIN(ADMIN_AUTHORITIES),
    SUPER_ADMIN(SUPER_ADMIN_AUTHORITIES),
    MANAGER(MANAGER_AUTHORITIES);

    private final String value;

    AuthorityEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
