package org.atmosia.simpleirc;

import io.wispforest.owo.config.annotation.Hook;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;

import java.util.ArrayList;
import java.util.List;

@Modmenu(modId = "simpleirc")
@io.wispforest.owo.config.annotation.Config(name = "simple-irc", wrapperName = "SimpleIRCConfig")
public class Config {
    @SectionHeader("connection details")
    @Hook
	public String ip = "";
    @Hook
    public Integer port = 6697;
    @Hook
    public String backupnick = "meowbackupnick";
    public boolean autoconnect = true;
    public boolean forceSsl = true;
    @Hook
    public IrcVerbosity verbosity = IrcVerbosity.NORMAL;
    public List<String> postConnectionCommands = new ArrayList<String>();


}
