package org.atmosia.simpleirc.mixins;

import java.util.concurrent.TimeUnit;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import org.atmosia.simpleirc.ChatUtils;

import org.atmosia.simpleirc.MainClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientCommonPacketListener
{
	
		@Inject(
			at = @At("HEAD"),
			method = "send",
			cancellable = true
		)
		
		private void onSendPacket(Packet<?> packet, CallbackInfo ci)
		{
			//System.out.println(packet);
			if(MainClient.autoconnect) this.checkstatus();
		}
	
		private void checkstatus() 
		{
			MainClient.autoconnect = false;
			new Thread(() -> 
			{
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(MainClient.settings.autoconnect())
				{
                    MainClient.Connect();
				}
			}).start();
            if (MainClient.irc != null && MainClient.irc.IsAway)
            {
                MainClient.irc.SendLine("AWAY");
                ChatUtils.Notify("You are no longer marked as away");
            }
		}
        @Inject(
                at = @At("HEAD"),
                method = "onDisconnect",
                cancellable = false
        )
    private void onDisconnected(DisconnectionDetails info, CallbackInfo ci)
    {
        if (MainClient.irc == null) return;
        if (!MainClient.irc.isConnected()) return;
        MainClient.irc.SendLine("AWAY Is in main menu");
        MainClient.irc.IsAway = true;
    }
}
