package org.monstercraft.irc.wrappers;

import java.util.ArrayList;
import java.util.List;

import org.monstercraft.irc.IRC;
import org.monstercraft.irc.util.ChatType;

import com.dthielke.herochat.Channel;
import com.dthielke.herochat.Herochat;

/**
 * This class creates an IRC channel to join.
 * 
 * @author fletch_to_99 <fletchto99@hotmail.com>
 * 
 */
public class IRCChannel extends IRC {

	private String channel;
	private String heroChatChannel;
	private ChatType type;
	private boolean autoJoin;
	private boolean defaultChannel;
	private List<String> opCommands;
	private List<String> voiceCommands;
	private List<String> userCommands;
	private List<String> ops;
	private List<String> voices;
	private String password;
	private boolean showJoinLeave;

	/**
	 * Creates an IRCChannel to join. .
	 * 
	 * @param password
	 *            The password to the channel, if any;
	 * @param showJoinLeave
	 *            The option to show leave/join messages.
	 * @param autoJoin
	 *            The option to automatically connect to the server.
	 * @param defaultChannel
	 *            Weither the channel is defaulted with the irc say command.
	 * @param channel
	 *            The channel to join.
	 * @param type
	 *            The channel type.
	 * @param opCommands
	 *            The list of commands IRC ops can use.
	 * @param voiceCommands
	 *            The list of commands IRC voices can use.
	 * @param userCommands
	 *            The list of commands IRC users can use.
	 */
	public IRCChannel(final String password, final boolean showJoinLeave,
			final boolean autoJoin, final boolean defaultChannel,
			final String channel, final ChatType type,
			final List<String> opCommands, final List<String> voiceCommands,
			final List<String> userCommands) {
		this.showJoinLeave = showJoinLeave;
		this.password = password;
		this.channel = channel;
		this.type = type;
		this.autoJoin = autoJoin;
		this.defaultChannel = defaultChannel;
		this.opCommands = opCommands;
		this.voiceCommands = voiceCommands;
		this.userCommands = userCommands;
		this.ops = new ArrayList<String>();
		this.voices = new ArrayList<String>();
		this.heroChatChannel = null;
	}

	/**
	 * Creates an IRCChannel to join.
	 * 
	 * @param password
	 *            The password to the channel, if any;
	 * @param showJoinLeave
	 *            The option to show leave/join messages.
	 * @param autoJoin
	 *            The option to automatically connect to the server.
	 * @param defaultChannel
	 *            Weither the channel is defaulted with the irc say command.
	 * @param channel
	 *            The channel to join.
	 * @param heroChatChannel
	 *            The herochat channel to pass chat to.
	 * @param type
	 *            The channel type.
	 * @param opCommands
	 *            The list of commands IRC ops can use.
	 * @param voiceCommands
	 *            The list of commands IRC voices can use.
	 * @param userCommands
	 *            The list of commands IRC users can use.
	 */
	public IRCChannel(final String password, final boolean showJoinLeave,
			final boolean autoJoin, final boolean defaultChannel,
			final String channel, final String heroChatChannel,
			final ChatType type, final List<String> opCommands,
			final List<String> voiceCommands, final List<String> userCommands) {
		this.showJoinLeave = showJoinLeave;
		this.password = password;
		this.channel = channel;
		this.heroChatChannel = heroChatChannel;
		this.type = type;
		this.autoJoin = autoJoin;
		this.defaultChannel = defaultChannel;
		this.opCommands = opCommands;
		this.voiceCommands = voiceCommands;
		this.userCommands = userCommands;
		this.ops = new ArrayList<String>();
		this.voices = new ArrayList<String>();
	}

	/**
	 * Checks if the bot should aut-join the channel.
	 * 
	 * @return True if the bot should auto-join the channel; otherwise false.
	 */
	public boolean isAutoJoin() {
		return autoJoin;
	}

	/**
	 * Checks if the bot should aut-join the channel.
	 * 
	 * @return True if the bot should auto-join the channel; otherwise false.
	 */
	public boolean isDefaultChannel() {
		return defaultChannel;
	}

	/**
	 * Fetches the IRC channel name.
	 * 
	 * @return The IRC channel's name.
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Fetches the channel's password.
	 * 
	 * @return the channel's password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Fetches the HeroChat channel to listen in.
	 * 
	 * @return The HeroChat channel to listen in.
	 */
	public Channel getHeroChatChannel() {
		return Herochat.getChannelManager().getChannel(heroChatChannel);
	}

	/**
	 * Fetches the HeroChat channel to listen in.
	 * 
	 * @return The HeroChat channel to listen in.
	 */
	public com.herocraftonline.dthielke.herochat.channels.Channel getHeroChatFourChannel() {
		return IRC.getHookManager().getHeroChatHook().getChannelManager()
				.getChannel(heroChatChannel);
	}

	/**
	 * Fetches the ChatType.
	 * 
	 * @return The Chat type.
	 */
	public ChatType getChatType() {
		return type;
	}

	/**
	 * Fetches the commands list for channel OPS.
	 * 
	 * @return The commands list for channel OPS.
	 */
	public List<String> getOpCommands() {
		return opCommands;
	}

	/**
	 * Fetches the commands list for channel voices.
	 * 
	 * @return The commands list for channel voices.
	 */
	public List<String> getVoiceCommands() {
		return voiceCommands;
	}

	/**
	 * Fetches the commands list for normal users.
	 * 
	 * @return The commands list for normal users.
	 */
	public List<String> getUserCommands() {
		return userCommands;
	}

	/**
	 * Fetches the OPS in this channel.
	 * 
	 * @return The OPS in this channel.
	 */
	public List<String> getOpList() {
		return ops;
	}

	/**
	 * Fetches the voices in this channel.
	 * 
	 * @return The voices in this channel.
	 */
	public List<String> getVoiceList() {
		return voices;
	}

	/**
	 * The option to show join and leave messages.
	 * 
	 * @return True if the option to show join and leave messages is enabled;
	 *         otherwise false.
	 */
	public boolean showJoinLeave() {
		return showJoinLeave;
	}

}
