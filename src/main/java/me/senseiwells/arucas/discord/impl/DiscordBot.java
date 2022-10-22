package me.senseiwells.arucas.discord.impl;

import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.discord.DiscordUtils;
import me.senseiwells.arucas.discord.DiscordUtils.LocatedFunction;
import me.senseiwells.arucas.utils.ArucasFunction;
import me.senseiwells.arucas.utils.impl.ArucasMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordBot implements EventListener {
	private final Map<String, List<LocatedFunction>> commandMap;
	private final Map<String, List<LocatedFunction>> eventMap;
	private final Map<String, Long> commandIds;
	private final JDA jda;

	public DiscordBot(JDA jda, Interpreter interpreter) {
		this.commandMap = new HashMap<>();
		this.eventMap = new HashMap<>();
		this.commandIds = new HashMap<>();
		this.jda = jda;

		jda.addEventListener(this);
		interpreter.getThreadHandler().addShutdownEvent(jda::shutdownNow);
	}

	public JDA getJda() {
		return this.jda;
	}

	public void registerEvent(Interpreter interpreter, ArucasFunction function, String eventName) {
		List<LocatedFunction> eventListeners = this.eventMap.computeIfAbsent(eventName, k -> new ArrayList<>());
		eventListeners.add(new LocatedFunction(interpreter, function));
	}

	public void addCommand(Interpreter interpreter, ArucasMap commandMap) {
		CommandData data = DiscordUtils.parseMapAsCommand(interpreter, this.commandMap, commandMap);
		this.commandIds.put(data.getName(), this.jda.upsertCommand(data).complete().getIdLong());
	}

	public void removeCommand(String commandName) {
		if (this.commandMap.remove(commandName) != null) {
			Long id = this.commandIds.remove(commandName);
			this.jda.deleteCommandById(id).queue();
		}
	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {
		if (event instanceof GenericCommandInteractionEvent commandEvent) {
			List<LocatedFunction> commands = this.commandMap.get(commandEvent.getName());
			int parameterSize = commandEvent.getOptions().size();
			if (commands == null || commands.size() < parameterSize) {
				return;
			}
			LocatedFunction locatedFunction = commands.get(parameterSize);
			if (locatedFunction == null) {
				commandEvent.reply("Invalid number of parameters").complete();
				return;
			}
			// Creates branch interpreter already
			Interpreter branch = locatedFunction.interpreter();
			branch.getThreadHandler().runAsync(() -> {
				return locatedFunction.function().invoke(branch, DiscordUtils.getParameters(branch, commandEvent));
			});
			return;
		}

		String eventName = event.getClass().getSimpleName();
		List<LocatedFunction> events = this.eventMap.get(eventName);
		if (events == null) {
			return;
		}

		events.forEach(locatedFunction -> {
			Interpreter branch = locatedFunction.interpreter();
			branch.getThreadHandler().runAsync(() -> {
				return locatedFunction.function().invoke(branch, List.of(branch.convertValue(event)));
			});
		});
	}
}
