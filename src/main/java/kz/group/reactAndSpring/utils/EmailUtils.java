package kz.group.reactAndSpring.utils;

public class EmailUtils {
    public static String getEmailMessage(String name, String otpCode, String host, String key) {
        return "Hello "+name+", \n\nYour new account has been created. " +
                "\nPlease follow the link below and enter a one-time code to verify your account. \nOTP code: " +
                otpCode +"\n\n" +
                getVerificationUrl(host,key) + "\n\nThe Support Team";
    }
    public static String getResetPasswordMessage(String name, String host, String key) {
        return "Hello "+name+", \n\n. " +
                "\nPlease use this link below to reset your password." +
                "\n\n" +
                getResetPasswordUrl(host,key) + "\n\nThe Support Team";
    }
    public static String getVerificationUrl(String host, String key) {
        return host+"/user/verify/account?key="+key;
    }
    public static String getResetPasswordUrl(String host, String key) {
        return host+"/user/verify/password?key="+key;
    }
}
