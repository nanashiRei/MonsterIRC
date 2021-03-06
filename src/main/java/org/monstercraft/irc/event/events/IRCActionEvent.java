package org.monstercraft.irc.event.events;

import org.bukkit.event.HandlerList;
import org.monstercraft.irc.event.IRCEvent;
import org.monstercraft.irc.wrappers.IRCChannel;

public class IRCActionEvent extends IRCEvent {

	public static final String CUSTOM_TYPE = "org.monstercraft.irc.event.events.IRCActionEvent";

	private static final HandlerList handlers = new HandlerList();

	private static final long serialVersionUID = 6167425751903134777L;

	private IRCChannel channel;

	private String sender;

	private String action;

	public IRCActionEvent(IRCChannel channel, String sender, String action) {
		super(CUSTOM_TYPE);
		this.channel = channel;
		this.sender = sender;
		this.action = action;
	}
	
	public IRCChannel getChannel() {
		return channel;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getAction() {
		return action;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
