package kz.group.reactAndSpring.utils;

public class EmailUtils {
    public static String getEmailMessage(String name, String host, String key) {
        return "Hello "+name+", \n\nYour new account has been created. " +
                "\nPlease click on the link below to verify your account.\n\n" +
                getVerificationUrl(host,key) + "\n\nThe Support Team";
    }
    public static String getResetPasswordMessage(String name, String host, String token) {
        return "Hello "+name+", \n\nYour new account has been created. " +
                "\nPlease click on the link below to verify your account.\n\n" +
                getResetPasswordUrl(host,token) + "\n\nThe Support Team";
    }
    public static String getVerificationUrl(String host, String key) {
        return host+"/user/verify/account?key="+key;
    }
    public static String getResetPasswordUrl(String host, String token) {
        return host+"/user/verify/password?token="+token;
    }
}
