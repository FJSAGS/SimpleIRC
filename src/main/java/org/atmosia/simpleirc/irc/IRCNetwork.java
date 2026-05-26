package org.atmosia.simpleirc.irc;

import net.minecraft.client.Minecraft;
import org.atmosia.simpleirc.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class IRCNetwork {
    private String Ip;
    private Integer Port;
    private String Nickname;
    private String BackupNickname;
    private Boolean UseBackupNickname = false;
    public IrcVerbosity Verbosity;
    private List<IRCChannel> Channels;
    private String PrimaryChannel;
    private boolean IsConnected;
    private Socket Socket;
    private BufferedReader Reader;
    private BufferedWriter Writer;
    private AtomicBoolean initialConnectionFinished;
    private AtomicBoolean registeredSasl;
    public Boolean IsAway;
    public IRCNetwork(String ip, Integer port, String nickname, String backupNickname, IrcVerbosity verbosity) {
        Ip = ip;
        Port = port;
        Nickname = nickname;
        BackupNickname = backupNickname;
        Verbosity = verbosity;
        Channels = new ArrayList<>();
        IsConnected = false;
        initialConnectionFinished = new AtomicBoolean(false);
        registeredSasl = new AtomicBoolean(false);
    }
    public void Connect() {
        if (isConnected()) {
            ChatUtils.Info("Already connected, doing nothing");
            return;
        }
        CallbackInfo callbackInfo = new CallbackInfo("IRC connection", true);
        try {
            new Thread(() ->
            {

                VerifyFields(callbackInfo);
                if (callbackInfo.isCancelled()) {
                    Cleanup();
                    return;

                }
                ChatUtils.Debug("Verified fields for connection");
                OpenSocket(callbackInfo, false);
                if (callbackInfo.isCancelled()) {
                    Cleanup();
                    return;
                }
                ChatUtils.Debug("Opened socket for connection");
                Listener();
                ChatUtils.Debug("Started listener");
                Register();
                ChatUtils.Debug("Registered user");
                synchronized (initialConnectionFinished) {
                    try {
                        ChatUtils.Info("Waiting for a welcome message...");
                        initialConnectionFinished.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    ChatUtils.Success("Successfully connected to IRC network " + Ip + ".");
                    ChatUtils.Notify("Done. You should be now connected to IRC.");
                    IsConnected = true;
                }
                for (var command : MainClient.settings.postConnectionCommands()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        ChatUtils.Error("Error sending command: Sleep interrupted");
                    }
                    try {
                        var time = Integer.parseInt(command.split(" ")[1]);
                        if (command.startsWith("wait")) {
                            Thread.sleep(time);
                        }
                    } catch (NumberFormatException e) {
                        if (command.startsWith("/simpleirc")) Minecraft.getInstance().execute(() -> Minecraft.getInstance().player.connection.sendCommand(command.replace("/simpleirc","simpleirc")));

                        SendLine(command);
                    } catch (InterruptedException e) {
                        ChatUtils.Error("Error sending command: Sleep interrupted");
                    }
                }
            }).start();


        } catch (IllegalStateException e) {
            ChatUtils.Error("Error connecting to server: ");
            ChatUtils.Error(e);
            callbackInfo.cancel();
            return;
        }


    }
    private void Listener() {
        new Thread(() -> {
            try {
                String line;
                while ((line = Reader.readLine()) != null) {
                    // System.out.println(line); // who left this here
                    if (Verbosity == IrcVerbosity.RAW || Verbosity == IrcVerbosity.DEBUG) {
                        ChatUtils.RawIn(line);
                    }
                    HandleInput(line);
                }
            } catch (Exception e) {
                ChatUtils.Error("An error has occured: " + e.getMessage());
                e.printStackTrace();
            }

        }).start();
    }
    private void Register() {
        // still TODO explicit server password? might be useful with SASL PLAIN
        SendLine("NICK " + Nickname);
        SendLine("USER " + Nickname + " mc * : " + Nickname);
    }

    public void SendLine(String line) {
        if (Writer == null)
        {
            Cleanup();
            throw new IllegalStateException("Writer is not connected to a server. Are you connected?");
        }
        if (Verbosity == IrcVerbosity.RAW || Verbosity == IrcVerbosity.DEBUG) {
            ChatUtils.RawOut(line);
        }
        try {
            if (line.contains("PART")) {
                String partedChannel = line.split(" ")[1];
                Channels.remove(GetChannel(partedChannel));
                if (PrimaryChannel.equals(partedChannel)) {
                    PrimaryChannel = Channels.getFirst().Name;
                    ChatUtils.Notify("Since you parted from channel <gray>" + partedChannel + "</gray>, the new primary channel is now <gray>" + PrimaryChannel + "</gray>.");
                }
            }
            Writer.write(line + "\r\n");
            Writer.flush();
        } catch (IOException e) {
            ChatUtils.Warn("Writer is not connected to a server. Are you connected?");
        }


    }
    private void OpenSocket(CallbackInfo callbackInfo, boolean notSsl) {
        try {
            if (!notSsl) {
                ChatUtils.Debug("Creating socket factory...");
                SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
                ChatUtils.Debug("Creating socket...");
                var socket = (SSLSocket) factory.createSocket(Ip, Port);
                ChatUtils.Debug("Setting protocols...");
                socket.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
                // Socket.setSoTimeout(5000); breaks everything...
                ChatUtils.Debug("Starting handshake... <dark_green>// Usually this is the point it gets stuck</dark_green>");
                socket.startHandshake();
                ChatUtils.Debug("Getting writer...");
                Writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                ChatUtils.Debug("And reader...");
                Reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Socket = socket;
            }
            else {
                ChatUtils.Debug("Creating socket...");
                var socket = new Socket(Ip, Port);
                ChatUtils.Debug("Getting writer...");
                Writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                ChatUtils.Debug("And reader...");
                Reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Socket = socket;
            }

            ChatUtils.Debug("Done, lets proceed");
            ChatUtils.Info("Connected to server <blue>" + Ip + "</blue>");

        }
        catch (Exception e) {
            if (MainClient.settings.forceSsl()) {
                callbackInfo.cancel();
//          ChatUtils.Exception(new Exception("An exception happened while connecting to the server:\n" + e.getMessage()));
                ChatUtils.Error("An exception happened while connecting to the server: " + e.getMessage() + "\n Usually this happens if ip or port are incorrect.");
            }
            if (!MainClient.settings.forceSsl() && !notSsl) {
                ChatUtils.Warn("Failed to connect using ssl. Since forceSsl is false, trying without ssl.");
                OpenSocket(callbackInfo, true);
            }
        }
    }

    private void VerifyFields(CallbackInfo callbackInfo) throws IllegalStateException {
        if (Ip.isEmpty() || Port < 1) {
            ChatUtils.Error("Ip or Port stated are certainly invalid. Please check them before connecting again.");
            callbackInfo.cancel();
        }
        if (Nickname.isEmpty()) {
            if (BackupNickname.isEmpty()){
                ChatUtils.Error(
                        "Both nickname and backup nickname are empty. Idk how would you get" +
                                "your nickname empty, but consider changing it.");
                callbackInfo.cancel();
            }

            Nickname = BackupNickname;
        }
        if (Character.isDigit(Nickname.charAt(0))) {
            if (Character.isDigit(BackupNickname.charAt(0))) {
                ChatUtils.Error(
                        "Both nickname and backup nickname start with digits. " +
                                "These are reserved for server purposes and cannot be used." +
                                "Please change either of them and try again"
                );
                callbackInfo.cancel();
            }
            Nickname = BackupNickname;
        }

    }

    private void HandleInput(String input) {
        if (input == null || input.isEmpty()) return;
        if (input.toLowerCase().contains("cannot join channel")) {
            ChatUtils.Warn("Cannot join channel. Message from server: " + input);
        }
        if (input.startsWith("PING")) {
            Pong(input);
            return;
        }
        if (input.contains("001")) {
            synchronized (initialConnectionFinished){
                initialConnectionFinished.notify();
            }
        }
        IRCMessageHandler.HandleMessage(input);
    }

    private void Pong(String input) {
        String payload = input.substring(4).trim();
        if (payload.startsWith(":")) {
            payload = payload.substring(1);
        }
        SendLine("PONG :" + payload);

    }
    public boolean isConnected() {
        return IsConnected;
    }
    public String ip() {
        return Ip;
    }
    public void SetIp(String ip) {
        Ip = ip;
    }
    public Integer port() {
        return Port;
    }
    public void SetPort(Integer port) {
        Port = port;
    }
    public String nickname() {
        if (UseBackupNickname) return BackupNickname;
        return Nickname;
    }
    public void SetDisplayNickname(String nickname) {
        Nickname = nickname;
    }
    public void SetBackupNickname(String backupNickname) {
        BackupNickname = backupNickname;
    }
    public List<IRCChannel> channels() {
        return Channels;
    }
    public void SendMessage(String target, String message) {
        if (target.isEmpty())
            target = PrimaryChannel;
        SendLine("PRIVMSG " + target + " :" + message);
    }

    public void SetPrimaryChannel(String channel) {
        if (Channels.stream().noneMatch(x -> Objects.equals(x.Name, channel))) {
            throw new RuntimeException("You aren't connected to channel " + channel);
        }
        PrimaryChannel = channel;
    }
    public String GetPrimaryChannel() {
        if (MainClient.DefaultToMinecraftChat) return "#minecraft_chat";

        return PrimaryChannel;
    }
    public void Disconnect(String reason) {
        SendLine("QUIT " + reason);
        try {
            ChatUtils.Verbose("Closing socket");
            Socket.close();
            IsConnected = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Cleanup();
        ChatUtils.Notify("Disconnected from server.");
    }
    public void SetVerbosity(IrcVerbosity verbosity) {
        Verbosity = verbosity;
    }
    public void AddChannel(IRCChannel channel, Boolean connect) {
        Channels.add(channel);
        ChatUtils.Info("Added channel " + channel.Name);
        if (connect) {
            ChatUtils.Info("Joining channel " + channel.Name + "...");
            SendLine(channel.Join());
        }
        if (Channels.size() == 1) {
            SetPrimaryChannel(channel.Name);
        }
    }
    public IRCChannel GetChannel(String channelName) {
        return Channels.stream().filter(x -> Objects.equals(x.Name, channelName) || Objects.equals(x.Name.substring(1), channelName)).findFirst().orElse(null);
    }
    public IRCChannel GetChannelByPrefix(Character prefix) {
        return Channels.stream().filter(x -> Objects.equals(x.Prefix, prefix)).findFirst().orElse(null);
    }
    public void AddChannelByPrefix(Character prefix, IRCChannel channel) {
        channel.Prefix = prefix;
        if (Channels.stream().noneMatch(x -> Objects.equals(x.Name, channel.Name))) {
            AddChannel(channel, true);
        }

    }
    public void UseBackupNickname() {
        SendLine("NICK " + BackupNickname);
        UseBackupNickname = true;
    }
    private void Cleanup() {
        ChatUtils.Notify("Cleaning up...");
        Socket = null;
        IsConnected = false;
        Writer = null;
        Reader = null;
        Channels = new ArrayList<>();
        initialConnectionFinished = new AtomicBoolean(false);
        registeredSasl = new AtomicBoolean(false);

    }
}
