package kz.group.reactAndSpring.constant;

public class Constants {
    public static final String IMAGE_DIRECTORY = System.getProperty("user.home") + "/images/";
    public static final String[] PUBLIC_URLS = { "/user/images/**", "/user/mfa/set", "/user/delete" ,"/user/resetpassword/reset/**", "/user/verify/resetpassword/**", "/user/resetpassword/**", "/user/verify/**", "/user/login/**", "/user/verify/account/**", "/user/register/**", "/user/new/password/**", "/user/verify/**", "/user/resetpassword/**", "/user/image/**", "/user/verify/password/**" };
    public static final String LOGIN_PATH = "/user/login";
    public static final String BASE_PATH = "/**";
    public static final String FILE_NAME = "File-Name";
    public static final String[] PUBLIC_ROUTES = { "/user/resetpassword/reset", "/user/verify/resetpassword", "/user/resetpassword", "/user/verify/qrcode", "/user/stream", "/user/id", "/user/login", "/user/register", "/user/new/password", "/user/verify", "/user/refresh/token", "/user/resetpassword", "/user/image", "/user/verify/account", "/user/verify/password", "/user/verify/code"};
    public static final String BANK_APPLICATION = "BANK_APPLICATION";
    public static final String AUTHORITIES = "authorities";
    public static final int NINETY_DAYS = 90;
    public static final int OTP_LENGTH = 6;
    public static final int STRENGTH = 12;
    public static final String EMPTY_VALUE = "empty";
    public static final String ROLE = "role";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORITY_DELIMITER = ",";
    public static final String USER_AUTHORITIES = "transaction:create, transaction:read, transaction:update, transaction:delete";
    public static final String ADMIN_AUTHORITIES = "user:create, user:update, transaction:create, transaction:read, transaction:update, transaction:delete";
    public static final String SUPER_ADMIN_AUTHORITIES = "user:create, user:read, user:update, user:delete, transaction:create,  transaction:read, transaction:update, transaction:delete";
    public static final String MANAGER_AUTHORITIES = "transaction:create, transaction:read, transaction:update, transaction:delete";
}
