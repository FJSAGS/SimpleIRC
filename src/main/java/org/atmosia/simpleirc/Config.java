package org.atmosia.simpleirc;

import io.wispforest.owo.config.annotation.Modmenu;

import java.util.ArrayList;
import java.util.List;

@Modmenu(modId = "simpleirc")
@io.wispforest.owo.config.annotation.Config(name = "simple-irc", wrapperName = "SimpleIRCConfig")
public class Config {
	public String ip = "";
    public String backupnick = "";
    public Integer port = 6697;
    public boolean autoconnect;
    public boolean forceSsl = true;
    public IrcVerbosity verbosity = IrcVerbosity.NORMAL;
    public List<String> postConnectionCommands = new ArrayList<String>();

}
