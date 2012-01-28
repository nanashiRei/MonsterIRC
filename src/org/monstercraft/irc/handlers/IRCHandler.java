package org.monstercraft.irc.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.bukkit.entity.Player;
import org.monstercraft.irc.IRC;
import org.monstercraft.irc.util.ChatType;
import org.monstercraft.irc.util.Variables;
import org.monstercraft.irc.wrappers.IRCChannel;

import com.gmail.nossr50.mcPermissions;

/**
 * This handles all of the IRC related stuff.
 * 
 * @author fletch_to_99 <fletchto99@hotmail.com>
 * 
 */
public class IRCHandler extends IRC {

	private BufferedWriter writer = null;
	private Socket connection = null;
	private BufferedReader reader = null;
	private Thread watch = null;
	private IRC plugin;
	private List<String> ops = new ArrayList<String>();
	private List<String> voice = new ArrayList<String>();
	private boolean connected = false;

	/**
	 * Creates an instance of the IRCHandler class.
	 * 
	 * @param plugin
	 *            The parent plugin.
	 */
	public IRCHandler(final IRC plugin) {
		this.plugin = plugin;
	}

	/**
	 * Connects to an IRC server then a channel.
	 * 
	 * @param server
	 *            The server to connec to.
	 * @param port
	 *            The port to use.
	 * @param user
	 *            The username.
	 * @param nick
	 *            The nick name.
	 * @param password
	 *            The password when identifing.
	 * @param identify
	 *            Weither the user wants to identify with nickserv.
	 * @return True if connected successfully; otherwise false.
	 */
	public boolean connect(final String server, final int port,
			final String user, final String nick, final String password,
			final boolean identify) {
		if (!isConnected()) {
			String line = null;
			try {
				connection = new Socket(server, port);
				writer = new BufferedWriter(new OutputStreamWriter(
						connection.getOutputStream()));
				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				log("Attempting to connect to chat.");
				writer.write("USER " + user + " 8 * :" + nick + "\r\n");
				writer.write("NICK " + Variables.name + "\r\n");
				writer.flush();
				log("Processing connection....");
				while ((line = reader.readLine()) != null) {
					debug(line);
					if (line.contains("004")) {
						break;
					} else if (line.contains("433")) {
						log("Your nickname is already in use, please switch it");
						log("using \"nick [NAME]\" and try to connect again.");
						disconnect();
						break;
					} else if (line.toLowerCase().startsWith("ping ")) {
						writer.write("PONG " + line.substring(5) + "\r\n");
						writer.flush();
						continue;
					}
				}
				if (identify) {
					log("Identifying with Nickserv....");

					writer.write("NICKSERV IDENTIFY " + password + "\r\n");
					writer.flush();
				}
				for (IRCChannel c : Variables.channels) {
					if (c.isAutoJoin()) {
						join(c.getChannel());

					}
				}
				watch = new Thread(KEEP_ALIVE);
				watch.setDaemon(true);
				watch.setPriority(Thread.MAX_PRIORITY);
				watch.start();
				connected = true;
			} catch (Exception e) {
				log("Failed to connect to IRC!");
				log("Please tell Fletch_to_99 the following!");
				debug(e.toString());
				disconnect();
			}
		}
		return isConnected();
	}

	/**
	 * Disconnects a user from the IRC server.
	 * 
	 * @return True if we disconnect successfully; otherwise false.
	 */
	public boolean disconnect() {
		try {
			for (IRCChannel c : Variables.channels) {
				leave(c.getChannel());
			}
			connected = false;
			reader.close();
			writer.close();
			if (!connection.isClosed()) {
				connection.shutdownInput();
				connection.shutdownOutput();
				connection.close();
			}
			if (watch != null) {
				watch.interrupt();
			}
			watch = null;
			writer = null;
			reader = null;
			connection = null;
			log("Successfully disconnected from IRC.");
		} catch (Exception e) {
			debug(e.toString());
		}
		return !isConnected();
	}

	/**
	 * Checks if the user is connected to an IRC server.
	 * 
	 * @return True if conencted to an IRC server; othewise false.
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Joins an IRC channel on that server.
	 * 
	 * @param channel
	 *            The channel to join.
	 */
	public void join(final String channel) {
		try {
			writer.write("JOIN " + channel + "\r\n");
			writer.flush();
			log("Successfully joined " + channel);
		} catch (IOException e) {
			debug(e.toString());
		}
	}

	/**
	 * Quits a channel in the IRC
	 * 
	 * @param channel
	 *            The channel to leave.
	 * @throws IOException
	 */
	public void leave(final String channel) throws IOException {
		if (isConnected()) {
			writer.write("QUIT " + channel + "\r\n");
			writer.flush();
		}
	}

	private final Runnable KEEP_ALIVE = new Runnable() {
		public void run() {
			try {
				if (isConnected() && reader != null && reader.ready()) {
					String line;
					try {
						while ((line = reader.readLine()) != null) {
							debug(line);
							if (!isConnected()) {
								break;
							}
							String name = null;
							String msg = null;
							String channel = null;
							for (IRCChannel c : Variables.channels) {
								try {
									channel = c.getChannel();
									if (line.contains("PRIVMSG "
											+ c.getChannel())) {
										name = line.substring(1,
												line.indexOf("!"));
										msg = line
												.substring(line.indexOf(" :") + 2);
									} else if (line.contains("JOIN :"
											+ c.getChannel())) {
										final String _name = line.substring(1,
												line.indexOf("!"));
										msg = _name + " has joined "
												+ c.getChannel() + ".";
									} else if (line.contains("PART "
											+ c.getChannel())) {
										final String _name = line.substring(1,
												line.indexOf("!"));
										msg = _name + " has left "
												+ c.getChannel() + ".";
									} else if (line.contains("QUIT :")) {
										final String _name = line.substring(1,
												line.indexOf("!"));
										msg = _name
												+ " has quit "
												+ c.getChannel()
												+ " ("
												+ line.substring(line.indexOf(
														":", 1) + 1) + ").";
									} else if (line.contains("MODE "
											+ c.getChannel() + " +v")
											|| line.contains("MODE "
													+ c.getChannel() + " -v")
											|| line.contains("MODE "
													+ c.getChannel() + " +o")
											|| line.contains("MODE "
													+ c.getChannel() + " -o")) {
										name = line.substring(line
												.indexOf("MODE "
														+ c.getChannel() + " ")
												+ c.getChannel().length() + 9);
										final String mode = line.substring(
												line.indexOf("MODE "
														+ c.getChannel() + " ")
														+ c.getChannel()
																.length() + 6,
												line.indexOf("MODE "
														+ c.getChannel() + " ")
														+ c.getChannel()
																.length() + 8);
										if (mode.contains("+v")) {
											getVoiceList().add(name);
										} else if (mode.contains("+o")) {
											getOpList().add(name);
										} else if (mode.contains("-o")) {
											getOpList().remove(name);
										} else if (mode.contains("-v")) {
											getVoiceList().remove(name);
										}
										name = line.substring(1,
												line.indexOf("!"));
										final String _name = line
												.substring(line.indexOf("MODE "
														+ c.getChannel() + " ")
														+ c.getChannel()
																.length() + 9);
										msg = _name + " has mode " + mode + ".";
									} else if (line.contains("KICK "
											+ c.getChannel())) {
										final String _name = line.substring(1,
												line.indexOf("!"));
										msg = _name + " has been kicked from"
												+ c.getChannel() + ".";
									}
									if (msg != null && name != null
											&& channel != null) {
										if (msg.startsWith(".")) {
											IRC.getCommandManager()
													.onIRCCommand(name, msg,
															channel);
											break;
										} else if (!Variables.muted
												.contains(name.toLowerCase())) {
											handleMessage(channel, name, msg);
											break;
										}
									}
								} catch (final Exception e) {
									debug(e.toString());
								}
							}
							if (line.toLowerCase().startsWith("ping ")) {
								if (msg == null) {
									writer.write("PONG " + line.substring(5)
											+ "\r\n");
									writer.flush();
								}
								continue;
							} else if (line.contains("353")) {
								if (msg == null) {
									List<String> users = new ArrayList<String>();
									StringTokenizer st = new StringTokenizer(
											line);
									while (st.hasMoreTokens()) {
										users.add(st.nextToken());
									}
									for (Object o : users.toArray()) {
										String s = (String) o;
										if (s.contains("+")) {
											voice.add(s.substring(1));
											log(s.substring(1)
													+ " has been added to the voice list.");
										}
										if (s.contains("@")) {
											ops.add(s.substring(1));
											log(s.substring(1)
													+ " has been added to the op list.");
										}
									}
								}
								continue;
							} else if (isCTCP(line)) {
								final String _name = line.substring(1,
										line.indexOf("!"));
								final String ctcpMsg = getCTCPMessage(line)
										.toUpperCase();
								if (ctcpMsg.equals("VERSION")) {
									writer.write("NOTICE " + _name + " :"
											+ (char) ctcpControl
											+ "Monstercraft" + " : " + "1"
											+ (char) ctcpControl + "\r\n");
									writer.flush();
								} else if (ctcpMsg.equals("TIME")) {
									final SimpleDateFormat sdf = new SimpleDateFormat(
											"dd MMM yyyy hh:mm:ss zzz");
									writer.write("NOTICE " + _name + " :"
											+ (char) ctcpControl
											+ sdf.format(new Date())
											+ (char) ctcpControl + "\r\n");
									writer.flush();
								}
							}
						}
					} catch (final Exception e) {
						debug(e.toString());
					}
				}
			} catch (IOException e) {
				debug(e.toString());
			}
		}

		private final byte ctcpControl = 1;

		private boolean isCTCP(final String input) {
			if (input.length() != 0) {
				String message = input.substring(input.indexOf(":", 1) + 1);
				if (message.length() != 0) {
					char[] messageArray = message.toCharArray();
					return ((byte) messageArray[0]) == 1
							&& ((byte) messageArray[messageArray.length - 1]) == 1;
				}
			}
			return false;
		}

		private String getCTCPMessage(final String input) {
			if (input.length() != 0) {
				String message = input.substring(input.indexOf(":", 1) + 1);
				return message.substring(
						message.indexOf((char) ctcpControl) + 1,
						message.indexOf((char) ctcpControl, 1));
			}
			return null;
		}
	};

	/**
	 * Sends a message to the specified channel.
	 * 
	 * @param Message
	 *            The message to send.
	 * @param channel
	 *            The channel to send the message to.
	 */
	public void sendMessage(final String Message, final String channel) {
		if (isConnected() && writer != null) {
			try {
				writer.write("PRIVMSG " + channel + " :" + Message + "\r\n");
				writer.flush();
			} catch (IOException e) {
				debug(e.toString());
			}
		}
	}

	/**
	 * Changes the nickname of the IRC bot.
	 * 
	 * @param Nick
	 *            The name to change to.
	 */
	public void changeNick(final String Nick) {
		if (isConnected()) {
			try {
				writer.write("NICK " + Nick + "\r\n");
				writer.flush();
			} catch (IOException e) {
				debug(e.toString());
			}
		}
	}

	/**
	 * Bans a user from the IRC channel if the bot is OP.
	 * 
	 * @param Nick
	 *            The user to ban.
	 * @param channel
	 *            The channel to ban in.
	 */
	public void ban(final String Nick, final String channel) {
		if (isConnected()) {
			try {
				writer.write("KICK " + channel + " " + Nick + "\r\n");
				writer.flush();
				writer.write("MODE " + channel + " +b" + Nick + "\r\n");
				writer.flush();
			} catch (IOException e) {
				debug(e.toString());
			}
		}
	}

	/**
	 * Checks if a user is OP in the IRC.
	 * 
	 * @param sender
	 *            The name to check.
	 * @param opList
	 *            The list to check in.
	 * @return True if the sender is OP; otherwise false.
	 */
	public boolean isOp(final String sender, final List<String> opList) {
		return opList.contains(sender);
	}

	/**
	 * Checks if a user is Voice in the IRC.
	 * 
	 * @param sender
	 *            The name to check.
	 * @param voiceList
	 *            The list to check in.
	 * @return True if the sender is Voice; otherwise false.
	 */
	public boolean isVoice(final String sender, final List<String> voiceList) {
		return voiceList.contains(sender);
	}

	/**
	 * Fetches the list of Operaters in the current IRC channel.
	 * 
	 * @return The list of Operators.
	 */
	public List<String> getOpList() {
		return ops;
	}

	/**
	 * Fetches the list of Voices in the current IRC channel.
	 * 
	 * @return The list of Voices.
	 */
	public List<String> getVoiceList() {
		return voice;
	}

	/**
	 * Removes colors from a string of text.
	 * 
	 * @param msg
	 *            The string to remove the colors from.
	 * @return The string without the colors.
	 */
	public String removeColors(final String msg) {
		String text = "";
		text = msg.replace("&0", "");
		text = text.replace("&1", "");
		text = text.replace("&2", "");
		text = text.replace("&3", "");
		text = text.replace("&4", "");
		text = text.replace("&5", "");
		text = text.replace("&6", "");
		text = text.replace("&7", "");
		text = text.replace("&8", "");
		text = text.replace("&9", "");
		text = text.replace("&A", "");
		text = text.replace("&B", "");
		text = text.replace("&C", "");
		text = text.replace("&D", "");
		text = text.replace("&E", "");
		text = text.replace("&F", "");

		text = text.replace("�0", "");
		text = text.replace("�1", "");
		text = text.replace("�2", "");
		text = text.replace("�3", "");
		text = text.replace("�4", "");
		text = text.replace("�5", "");
		text = text.replace("�6", "");
		text = text.replace("�7", "");
		text = text.replace("�8", "");
		text = text.replace("�9", "");
		text = text.replace("�A", "");
		text = text.replace("�B", "");
		text = text.replace("�C", "");
		text = text.replace("�D", "");
		text = text.replace("�E", "");
		text = text.replace("�F", "");
		return text;
	}

	private void handleMessage(final String Channel, final String name,
			final String message) {
		try {
			for (IRCChannel c : Variables.channels) {
				if (c.getChannel().equalsIgnoreCase(Channel)) {
					if (c.getChatType() == ChatType.ADMINCHAT) {
						if (IRC.getHookManager().getmcMMOHook() != null) {
							String format = "�b" + "{" + "�f" + "[IRC] " + name
									+ "�b" + "} " + message;
							for (Player p : plugin.getServer()
									.getOnlinePlayers()) {
								if (p.isOp()
										|| mcPermissions.getInstance()
												.adminChat(p))
									p.sendMessage(format);
								break;
							}
						}
					} else if (c.getChatType() == ChatType.HEROCHAT
							&& IRC.getHookManager().getHeroChatHook() != null) {
						c.getHeroChatChannel().sendMessage("<" + name + ">",
								removeColors(message),
								c.getHeroChatChannel().getMsgFormat(), false);
						break;
					} else if (c.getChatType() == ChatType.ALL) {
						plugin.getServer()
								.broadcastMessage(
										"[IRC]<" + name + ">: "
												+ removeColors(message));
						break;
					}
				}
			}
		} catch (Exception e) {
			debug(e.toString());
		}
	}
}