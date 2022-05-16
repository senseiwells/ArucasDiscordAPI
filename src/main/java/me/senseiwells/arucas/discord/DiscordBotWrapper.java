package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.ConstructorDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.api.wrappers.*;
import me.senseiwells.arucas.discord.DiscordUtils.FunctionContext;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.utils.impl.ArucasList;
import me.senseiwells.arucas.values.MapValue;
import me.senseiwells.arucas.values.NullValue;
import me.senseiwells.arucas.values.StringValue;
import me.senseiwells.arucas.values.Value;
import me.senseiwells.arucas.values.classes.WrapperClassDefinition;
import me.senseiwells.arucas.values.classes.WrapperClassValue;
import me.senseiwells.arucas.values.functions.FunctionValue;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.senseiwells.arucas.discord.DiscordAPI.*;

@SuppressWarnings("unused")
@ClassDoc(
	name = DISCORD_BOT,
	desc = "This class lets you create a Discord bot and interact with it."
)
@ArucasClass(name = "DiscordBot")
public class DiscordBotWrapper implements IArucasWrappedClass, EventListener {
	@ArucasDefinition
	public static WrapperClassDefinition DEFINITION;

	private Map<String, List<FunctionContext>> commandMap;
	private Map<String, List<FunctionContext>> eventMap;
	private JDA jda;

	@ConstructorDoc(
		desc = "This creates a new DiscordBot instance",
		params = {STRING, "token", "The token of the bot"},
		example = "new DiscordBot('token')"
	)
	@ArucasConstructor
	public void construct(Context context, StringValue token) throws LoginException {
		this.construct(JDABuilder.createDefault(token.value).addEventListeners(this).build(), context);
	}

	@FunctionDoc(
		name = "setActivity",
		desc = "This sets the activity of the bot",
		params = {
			STRING, "activity", "The activity you want the bot to have",
			STRING, "message", "The message you want to display"
		},
		throwMsgs = "... is an invalid activity",
		example = "bot.setActivity('PLAYING', 'Arucas')"
	)
	@ArucasFunction
	public void setActivity(Context context, StringValue activityAsString, StringValue message) {
		Activity activity = switch (activityAsString.value.toLowerCase()) {
			case "playing" -> Activity.playing(message.value);
			case "watching" -> Activity.watching(message.value);
			case "listening" -> Activity.listening(message.value);
			case "competing" -> Activity.competing(message.value);
			default -> throw new RuntimeException("'%s' is an invalid activity".formatted(activityAsString.value));
		};
		this.jda.getPresence().setActivity(activity);
	}

	@FunctionDoc(
		name = "getActivity",
		desc = "This gets the activity of the bot",
		returns = {STRING, "The activity of the bot, null if no activity"},
		example = "bot.getActivity()"
	)
	@ArucasFunction
	public Value getActivity(Context context) {
		Activity activity = this.jda.getPresence().getActivity();
		if (activity == null) {
			return NullValue.NULL;
		}
		return StringValue.of(activity.getType().name() + ": " + activity.getName());
	}

	@FunctionDoc(
		name = "setStatus",
		desc = "This sets the status of the bot",
		params = {STRING, "status", "The status you want the bot to have"},
		throwMsgs = "... is an invalid status",
		example = "bot.setStatus('ONLINE')"
	)
	@ArucasFunction
	public void setStatus(Context context, StringValue status) {
		OnlineStatus onlineStatus = OnlineStatus.fromKey(status.value);
		if (onlineStatus == OnlineStatus.UNKNOWN) {
			throw new RuntimeException("'%s' is an invalid status".formatted(status.value));
		}
		this.jda.getPresence().setStatus(onlineStatus);
	}

	@FunctionDoc(
		name = "getStatus",
		desc = "This gets the status of the bot",
		returns = {STRING, "The status of the bot"},
		example = "bot.getStatus()"
	)
	@ArucasFunction
	public StringValue getStatus(Context context) {
		return StringValue.of(this.jda.getPresence().getStatus().getKey());
	}


	@FunctionDoc(
		name = "getUserId",
		desc = "This gets the user id of the bot",
		returns = {STRING, "The user id of the bot"},
		example = "bot.getUserId()"
	)
	@ArucasFunction
	public StringValue getUserId(Context context) {
		return DiscordUtils.getId(this.jda.getSelfUser());
	}

	@FunctionDoc(
		name = "registerEvent",
		desc = "This registers a function to be called when an event is triggered",
		params = {
			STRING, "eventName", "the name of the event",
			FUNCTION, "function", "the function to be called"
		},
		example = "bot.registerEvent('MessageReceivedEvent', function(event) { })"
	)
	@ArucasFunction
	public void registerEvent(Context context, StringValue eventName, FunctionValue functionValue) {
		List<FunctionContext> events = this.eventMap.getOrDefault(eventName.value, new ArrayList<>());
		events.add(new FunctionContext(context, functionValue));
		this.eventMap.putIfAbsent(eventName.value, events);
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
		throwMsgs = {
			"Command must have name and a description",
			"Slash command went too deep",
			"Command must include option type",
			"Invalid option"
		},
		example = """
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
	@ArucasFunction
	public void addCommand(Context context, MapValue commandMap) throws CodeError {
		SlashCommandData commandData = DiscordUtils.parseMapAsCommand(context, this.commandMap, commandMap.value);
		this.jda.upsertCommand(commandData).complete();
	}

	@FunctionDoc(
		name = "removeCommand",
		desc = "This removes a slash command from the bot",
		params = {STRING, "commandName", "the name of the command"},
		example = "bot.removeCommand('command')"
	)
	@ArucasFunction
	public void removeCommand(Context context, StringValue commandName) {
		this.commandMap.remove(commandName.value);
		this.jda.deleteCommandById(commandName.value).complete();
	}

	@FunctionDoc(
		name = "stop",
		desc = "This stops the bot",
		example = "bot.stop()"
	)
	@ArucasFunction
	public void stop(Context context) {
		this.jda.shutdown();
	}

	@FunctionDoc(
		name = "getChannel",
		desc = "This gets a channel by its id",
		params = {STRING, "channelId", "the id of the channel"},
		returns = {DISCORD_CHANNEL, "the channel"},
		throwMsgs = "Channel with id ... couldn't be found",
		example = "bot.getChannel('12345678901234567890123456789012')"
	)
	@ArucasFunction
	public WrapperClassValue getChannel(Context context, StringValue channelId) throws CodeError {
		MessageChannel messageChannel = this.jda.getChannelById(MessageChannel.class, channelId.value);
		if (messageChannel == null) {
			throw new RuntimeException("Channel with id '%s' couldn't be found".formatted(channelId.value));
		}
		return DiscordChannelWrapper.newDiscordChannel(messageChannel, context);
	}

	@FunctionDoc(
		name = "getServer",
		desc = "This gets a server by its id",
		params = {STRING, "serverId", "the id of the server"},
		returns = {DISCORD_SERVER, "the server"},
		throwMsgs = "Server with id ... couldn't be found",
		example = "bot.getServer('12345678901234567890123456789012')"
	)
	@ArucasFunction
	public WrapperClassValue getServer(Context context, StringValue serverId) throws CodeError {
		Guild guild = this.jda.getGuildById(serverId.value);
		if (guild == null) {
			throw new RuntimeException("Server with id '%s' couldn't be found".formatted(serverId.value));
		}
		return DiscordServerWrapper.newDiscordServer(guild, context);
	}

	private void construct(JDA jda, Context context) {
		this.commandMap = new HashMap<>();
		this.eventMap = new HashMap<>();
		this.jda = jda;

		if (!jda.getEventManager().getRegisteredListeners().contains(this)) {
			jda.addEventListener(this);
			context.getThreadHandler().addShutdownEvent(jda::shutdownNow);
		}
	}

	public static WrapperClassValue newDiscordBot(JDA jda, Context context) throws CodeError {
		DiscordBotWrapper botWrapper = new DiscordBotWrapper();
		botWrapper.construct(jda, context);
		return DEFINITION.createNewDefinition(botWrapper, context, List.of());
	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof GenericCommandInteractionEvent commandEvent) {
			List<FunctionContext> commands = this.commandMap.get(commandEvent.getName());
			int parameterSize = commandEvent.getOptions().size();
			if (commands == null || commands.size() < parameterSize) {
				return;
			}
			FunctionContext functionContext = commands.get(parameterSize);
			if (functionContext == null) {
				commandEvent.reply("Invalid number of parameters").complete();
				return;
			}
			// Creates branch context already
			Context context = functionContext.context();
			context.getThreadHandler().runAsyncFunctionInThreadPool(context, branchContext -> {
				functionContext.function().call(branchContext, DiscordUtils.getParameters(context, commandEvent));
			});
			return;
		}
		String eventName = event.getClass().getSimpleName();
		List<FunctionContext> events = this.eventMap.get(eventName);
		if (events == null) {
			return;
		}
		events.forEach(functionContext -> {
			Context context = functionContext.context();
			context.getThreadHandler().runAsyncFunctionInThreadPool(context.createBranch(), branchContext -> {
				List<Value> parameters = ArucasList.arrayListOf(DiscordEventWrapper.newDiscordEvent(event, branchContext));
				functionContext.function().call(branchContext, parameters);
			});
		});
	}

	@Override
	public JDA asJavaValue() {
		return this.jda;
	}
}
