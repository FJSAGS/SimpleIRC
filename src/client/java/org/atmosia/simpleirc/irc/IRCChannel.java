package org.atmosia.simpleirc.irc;

public class IRCChannel {
    public String Name;
    public String Password;
    public Boolean AutoConnectFlag;
    public Character Prefix;
    public String Join() {
        if (!Password.isEmpty()) {
            return "JOIN " + Name + " " + Password;
        }
        else {
            return "JOIN " + Name;
        }
    }
    public String Part(String reason) {
        if (!reason.isEmpty()) {
            return "PART " + Name + " " + reason;
        }
        else {
            return "PART " + Name;
        }
    }
    public IRCChannel(String name, String password, Boolean autoConnectFlag) {
        Name = name;
        Password = password;
        AutoConnectFlag = autoConnectFlag;
    }

}
