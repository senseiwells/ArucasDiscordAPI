package me.senseiwells.arucas.discord.definitions;

import kotlin.Unit;
import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.ConstructorDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.builtin.FunctionDef;
import me.senseiwells.arucas.builtin.MapDef;
import me.senseiwells.arucas.builtin.StringDef;
import me.senseiwells.arucas.classes.ClassInstance;
import me.senseiwells.arucas.classes.CreatableDefinition;
import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.discord.DiscordUtils;
import me.senseiwells.arucas.discord.impl.DiscordBot;
import me.senseiwells.arucas.exceptions.RuntimeError;
import me.senseiwells.arucas.utils.*;
import me.senseiwells.arucas.utils.impl.ArucasMap;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.*;
import static me.senseiwells.arucas.utils.Util.Types.*;

@ClassDoc(
	name = DISCORD_BOT,
	desc = "This class lets you create a Discord bot and interact with it.",
	importPath = "discordapi.Discord",
	language = Util.Language.Java
)
public class DiscordBotDef extends CreatableDefinition<DiscordBot> {
	public DiscordBotDef(Interpreter interpreter) {
		super(DISCORD_BOT, interpreter);
	}

	@Override
	public List<ConstructorFunction> defineConstructors() {
		return List.of(
			ConstructorFunction.of(1, this::construct)
		);
	}

	@Override
	public List<MemberFunction> defineMethods() {
		return List.of(
			MemberFunction.of("setActivity", 2, this::setActivity),
			MemberFunction.of("getActivity", this::getActivity),
			MemberFunction.of("setStatus", 1, this::setStatus),
			MemberFunction.of("getStatus", this::getStatus),
			MemberFunction.of("getUserId", this::getUserId),
			MemberFunction.of("registerEvent", 2, this::registerEvent),
			MemberFunction.of("addCommand", 1, this::addCommand),
			MemberFunction.of("removeCommand", 1, this::removeCommand),
			MemberFunction.of("stop", this::stop),
			MemberFunction.of("getChannel", 1, this::getChannel),
			MemberFunction.of("getServer", 1, this::getServer)
		);
	}

	@ConstructorDoc(
		desc = "This creates a new DiscordBot instance",
		params = {STRING, "token", "The token of the bot"},
		examples = "new DiscordBot('token')"
	)
	public Unit construct(Arguments arguments) {
		ClassInstance instance = arguments.next();
		String token = arguments.nextPrimitive(StringDef.class);
		instance.setPrimitive(this, new DiscordBot(JDABuilder.createDefault(token).build(), arguments.getInterpreter()));
		return null;
	}

	@FunctionDoc(
		name = "setActivity",
		desc = "This sets the activity of the bot",
		params = {
			STRING, "activity", "The activity you want the bot to have",
			STRING, "message", "The message you want to display"
		},
		examples = "bot.setActivity('PLAYING', 'Arucas')"
	)
	public Void setActivity(Arguments arguments) {
		DiscordBot bot = arguments.nextPrimitive(this);
		String activityString = arguments.nextConstant().toLowerCase();
		String activityMessage = arguments.nextPrimitive(StringDef.class);
		Activity activity = switch (activityMessage) {
			case "playing" -> Activity.playing(activityString);
			case "watching" -> Activity.watching(activityMessage);
			case "listening" -> Activity.listening(activityMessage);
			case "competing" -> Activity.competing(activityMessage);
			default -> throw new RuntimeException("'%s' is an invalid activity".formatted(activityString));
		};
		bot.getJda().getPresence().setActivity(activity);
		return null;
	}

	@FunctionDoc(
		name = "getActivity",
		desc = "This gets the activity of the bot",
		returns = {STRING, "The activity of the bot, null if no activity"},
		examples = "bot.getActivity()"
	)
	public String getActivity(Arguments arguments) {
		DiscordBot bot = arguments.nextPrimitive(this);
		Activity activity = bot.getJda().getPresence().getActivity();
		return activity == null ? null : activity.getType().name() + ": " + activity.getName();
	}

	@FunctionDoc(
		name = "setStatus",
		desc = "This sets the status of the bot",
		params = {STRING, "status", "The status you want the bot to have"},
		examples = "bot.setStatus('ONLINE')"
	)
	public Void setStatus(Arguments arguments) {
		DiscordBot bot = arguments.nextPrimitive(this);
		String status = arguments.nextConstant();
		OnlineStatus onlineStatus = OnlineStatus.fromKey(status);
		if (onlineStatus == OnlineStatus.UNKNOWN) {
			throw new RuntimeException("'%s' is an invalid status".formatted(status));
		}
		bot.getJda().getPresence().setStatus(onlineStatus);
		return null;
	}

	@FunctionDoc(
		name = "getStatus",
		desc = "This gets the status of the bot",
		returns = {STRING, "The status of the bot"},
		examples = "bot.getStatus()"
	)
	public String getStatus(Arguments arguments) {
		return arguments.nextPrimitive(this).getJda().getPresence().getStatus().getKey();
	}

	@FunctionDoc(
		name = "getUserId",
		desc = "This gets the user id of the bot",
		returns = {STRING, "The user id of the bot"},
		examples = "bot.getUserId()"
	)
	public String getUserId(Arguments arguments) {
		return DiscordUtils.getId(arguments.nextPrimitive(this).getJda().getSelfUser());
	}

	@FunctionDoc(
		name = "registerEvent",
		desc = "This registers a function to be called when an event is triggered",
		params = {
			STRING, "eventName", "the name of the event",
			FUNCTION, "function", "the function to be called"
		},
		examples = "bot.registerEvent('MessageReceivedEvent', function(event) { })"
	)
	public Void registerEvent(Arguments arguments) {
		DiscordBot bot = arguments.nextPrimitive(this);
		String name = arguments.nextConstant();
		ArucasFunction callback = arguments.nextPrimitive(FunctionDef.class);
		bot.registerEvent(arguments.getInterpreter(), callback, name);
		return null;
	}

	@FunctionDoc(
		name = "addCommand",
		desc = {
			"This adds a slash command to the bot",
			"Each command must have a name and description, it can have a command, define the next subcommand with 'next'",
			"and subcommands must have the argument type, and can have whether it is required or not",
			"types: 'string', 'integer', 'number', 'boolean', 'user', 'channel', and 'attachment'"
		},
		params = {MAP, "commandMap", "the command map"},
		examples = """
		bot.addCommand({
		    "name": "command",
		    "description": "Does something",
		    "command": fun(event) {
		        // passes in the event
		        // do stuff
		    }
		    "next: {
		        "name": "subcommand",
		        "description": "Does something else",
		        "required": true,
		        "type": "String",
		        "command": fun(event, string) {
		            // passes in the event and the string argument
		            // do stuff
		        }
		    }
		});
		"""
	)
	public Void addCommand(Arguments arguments) {
		DiscordBot bot = arguments.nextPrimitive(this);
		ArucasMap map = arguments.nextPrimitive(MapDef.class);
		bot.addCommand(arguments.getInterpreter(), map);
		return null;
	}

	@FunctionDoc(
		name = "removeCommand",
		desc = "This removes a slash command from the bot",
		params = {STRING, "commandName", "the name of the command"},
		examples = "bot.removeCommand('command')"
	)
	public Void removeCommand(Arguments arguments) {
		DiscordBot bot = arguments.nextPrimitive(this);
		String commandName = arguments.nextPrimitive(StringDef.class);
		bot.removeCommand(commandName);
		return null;
	}

	@FunctionDoc(
		name = "stop",
		desc = "This stops the bot",
		examples = "bot.stop()"
	)
	public Void stop(Arguments arguments) {
		arguments.nextPrimitive(this).getJda().shutdown();
		return null;
	}

	@FunctionDoc(
		name = "getChannel",
		desc = "This gets a channel by its id",
		params = {STRING, "channelId", "the id of the channel"},
		returns = {DISCORD_CHANNEL, "the channel"},
		examples = "bot.getChannel('12345678901234567890123456789012')"
	)
	public MessageChannel getChannel(Arguments arguments) {
		DiscordBot bot = arguments.nextPrimitive(this);
		String id = arguments.nextPrimitive(StringDef.class);
		MessageChannel messageChannel = bot.getJda().getChannelById(MessageChannel.class, id);
		if (messageChannel == null) {
			throw new RuntimeError("Channel with id '%s' couldn't be found".formatted(id));
		}
		return messageChannel;
	}

	@FunctionDoc(
		name = "getServer",
		desc = "This gets a server by its id",
		params = {STRING, "serverId", "the id of the server"},
		returns = {DISCORD_SERVER, "the server"},
		examples = "bot.getServer('12345678901234567890123456789012')"
	)
	public Guild getServer(Arguments arguments) {
		DiscordBot bot = arguments.nextPrimitive(this);
		String id = arguments.nextPrimitive(StringDef.class);
		Guild guild = bot.getJda().getGuildById(id);
		if (guild == null) {
			throw new RuntimeError("Server with id '%s' couldn't be found".formatted(id));
		}
		return guild;
	}
}
