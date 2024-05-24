package kz.group.reactAndSpring.utils;

public class EmailUtils {
    public static String getVerificationUrl(String host, String key) {
        return host+"/user/verify/account?key="+key;
    }
    public static String getResetPasswordUrl(String host, String key) {
        return host+"/user/verify/password?key="+key;
    }
}
