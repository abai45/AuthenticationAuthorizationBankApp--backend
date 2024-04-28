package kz.group.reactAndSpring.constant;

public class Constants {
    public static final String AUTHORITIES = "authorities";
    public static final String GET_ARRAYS_LLC = "GET_ARRAYS_LLC";
    public static final String EMPTY_VALUE = "empty";
    public static final String ROLE = "role";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORITY_DELIMITER = ",";
    public static final String USER_AUTHORITIES = "transaction:create, transaction:read, transaction:update, transaction:delete";
    public static final String ADMIN_AUTHORITIES = "user:create, user:update, transaction:create, transaction:read, transaction:update, transaction:delete";
    public static final String SUPER_ADMIN_AUTHORITIES = "user:create, user:read, user:update, user:delete, transaction:create,  transaction:read, transaction:update, transaction:delete";
    public static final String MANAGER_AUTHORITIES = "transaction:create, transaction:read, transaction:update, transaction:delete";
}
