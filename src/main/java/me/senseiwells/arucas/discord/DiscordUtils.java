package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.builtin.*;
import me.senseiwells.arucas.classes.ClassInstance;
import me.senseiwells.arucas.classes.PrimitiveDefinition;
import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.exceptions.RuntimeError;
import me.senseiwells.arucas.utils.ArucasFunction;
import me.senseiwells.arucas.utils.impl.ArucasIterable;
import me.senseiwells.arucas.utils.impl.ArucasList;
import me.senseiwells.arucas.utils.impl.ArucasMap;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordUtils {
	private static Map<String, Permission> cachedPermissions;

	public static String getId(ISnowflake snowflake) {
		return snowflake.getId();
	}

	private static <T extends PrimitiveDefinition<V>, V> V getFieldInMap(ArucasMap map, Interpreter interpreter, String field, Class<T> type) {
		ClassInstance instance = getFieldInMap(map, interpreter, field);
		return instance == null ? null : instance.getPrimitive(type);
	}

	private static ClassInstance getFieldInMap(ArucasMap map, Interpreter interpreter, String field) {
		return map.get(interpreter, interpreter.create(StringDef.class, field));
	}

	public static MessageEmbed parseMapAsEmbed(Interpreter interpreter, ArucasMap map) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		String title = getFieldInMap(map, interpreter, "title", StringDef.class);
		if (title != null) {
			embedBuilder.setTitle(title);
		}

		ClassInstance description = getFieldInMap(map, interpreter, "description");
		if (description != null) {
			ArucasIterable iterable = description.getPrimitive(IterableDef.class);
			if (iterable != null) {
				for (ClassInstance desc : iterable) {
					embedBuilder.appendDescription(desc.toString(interpreter));
				}
			} else {
				embedBuilder.setDescription(description.toString(interpreter));
			}
		}

		Double colour = getFieldInMap(map, interpreter, "colour", NumberDef.class);
		if (colour == null) {
			colour = getFieldInMap(map, interpreter, "color", NumberDef.class);
		}
		if (colour != null) {
			embedBuilder.setColor(colour.intValue());
		}

		ArucasList list = getFieldInMap(map, interpreter, "fields", ListDef.class);
		if (list != null) {
			for (ClassInstance field : list) {
				ArucasMap fieldObj = field.getPrimitive(MapDef.class);
				if (fieldObj == null) {
					continue;
				}
				String name = getFieldInMap(fieldObj, interpreter, "name", StringDef.class);
				if (name == null) {
					throw new RuntimeError("Field 'name' was null");
				}
				String value = getFieldInMap(fieldObj, interpreter, "value", StringDef.class);
				if (value == null) {
					throw new RuntimeError("Field 'value' was null");
				}
				Boolean inline = getFieldInMap(fieldObj, interpreter, "ineline", BooleanDef.class);
				if (inline == null) {
					throw new RuntimeError("Field 'inline' was null");
				}
				embedBuilder.addField(name, value, inline);
			}
		}

		String image = getFieldInMap(map, interpreter, "image", StringDef.class);
		if (image != null) {
			embedBuilder.setImage(image);
		}
		return embedBuilder.build();
	}

	public static void parseMapAsRole(Interpreter interpreter, RoleAction roleAction, ArucasMap map) {
		String name = getFieldInMap(map, interpreter, "name", StringDef.class);
		if (name != null) {
			roleAction = roleAction.setName(name);
		}

		Double colour = getFieldInMap(map, interpreter, "colour", NumberDef.class);
		if (colour == null) {
			colour = getFieldInMap(map, interpreter, "color", NumberDef.class);
		}
		if (colour != null) {
			roleAction = roleAction.setColor(colour.intValue());
		}

		Boolean hoisted = getFieldInMap(map, interpreter, "hoisted", BooleanDef.class);
		if (hoisted == Boolean.TRUE) {
			roleAction = roleAction.setHoisted(true);
		}

		Boolean mentionable = getFieldInMap(map, interpreter, "mentionable", BooleanDef.class);
		if (mentionable == Boolean.TRUE) {
			roleAction = roleAction.setMentionable(true);
		}

		ArucasIterable permissions = getFieldInMap(map, interpreter, "permissions", IterableDef.class);
		if (permissions == null) {
			roleAction.complete();
			return;
		}

		if (cachedPermissions == null) {
			cachedPermissions = new HashMap<>();

			for (Permission permission : Permission.values()) {
				cachedPermissions.put(permission.getName(), permission);
			}
		}

		for (ClassInstance permission : permissions) {
			Permission listedPermission = cachedPermissions.get(permission.toString(interpreter));
			if (listedPermission != null) {
				roleAction = roleAction.setPermissions(listedPermission);
			}
		}
		roleAction.complete();
	}

	public static SlashCommandData parseMapAsCommand(Interpreter interpreter, Map<String, List<LocatedFunction>> commandMap, ArucasMap map) {
		String name = getFieldInMap(map, interpreter, "name", StringDef.class);
		String description = getFieldInMap(map, interpreter, "description", StringDef.class);
		if (name == null || description == null) {
			throw new RuntimeException("Command must have name and a description");
		}
		SlashCommandData slashCommandData = Commands.slash(name, description);
		List<LocatedFunction> functions = new ArrayList<>();
		commandMap.put(name, functions);
		ArucasFunction command = getFieldInMap(map, interpreter, "command", FunctionDef.class);
		functions.add(0, new LocatedFunction(interpreter, command));
		ArucasMap next = getFieldInMap(map, interpreter, "next", MapDef.class);
		if (next != null) {
			slashCommandData = commandOption(Commands.slash(name, description), functions, interpreter, next, 1);
		}
		return slashCommandData;
	}

	private static SlashCommandData commandOption(SlashCommandData slashCommandData, List<LocatedFunction> commandList, Interpreter interpreter, ArucasMap map, int depth) {
		if (depth > 25) {
			throw new RuntimeException("Slash command went too deep");
		}
		String option = getFieldInMap(map, interpreter, "type", StringDef.class);
		if (option == null) {
			throw new RuntimeException("Command must include option type");
		}
		OptionType optionType = switch (option.toLowerCase()) {
			case "string" -> OptionType.STRING;
			case "integer" -> OptionType.INTEGER;
			case "number" -> OptionType.NUMBER;
			case "boolean" -> OptionType.BOOLEAN;
			case "user" -> OptionType.USER;
			case "channel" -> OptionType.CHANNEL;
			case "attachment" -> OptionType.ATTACHMENT;
			default -> throw new RuntimeException("Invalid option");
		};
		String name = getFieldInMap(map, interpreter, "name", StringDef.class);
		String description = getFieldInMap(map, interpreter, "description", StringDef.class);
		if (name == null || description == null) {
			throw new RuntimeException("Command must have name and a description");
		}
		boolean required = getFieldInMap(map, interpreter, "required", BooleanDef.class) == Boolean.TRUE;
		slashCommandData = slashCommandData.addOption(optionType, name, description, required);
		ArucasFunction function = getFieldInMap(map, interpreter, "command", FunctionDef.class);
		commandList.add(depth, function != null ? new LocatedFunction(interpreter, function) : null);
		ArucasMap next = getFieldInMap(map, interpreter, "next", MapDef.class);
		if (next != null) {
			slashCommandData = commandOption(slashCommandData, commandList, interpreter, next, depth + 1);
		}
		return slashCommandData;
	}

	public static List<ClassInstance> getParameters(Interpreter context, GenericCommandInteractionEvent commandEvent) {
		List<ClassInstance> parameters = new ArrayList<>();
		parameters.add(context.convertValue(commandEvent));
		for (OptionMapping mapping : commandEvent.getOptions()) {
			parameters.add(parseMapping(context, mapping));
		}
		return parameters;
	}

	private static ClassInstance parseMapping(Interpreter context, OptionMapping mapping) {
		return context.convertValue(switch (mapping.getType()) {
			case INTEGER, NUMBER -> mapping.getAsDouble();
			case BOOLEAN -> mapping.getAsBoolean();
			case USER -> mapping.getAsUser();
			case ATTACHMENT -> mapping.getAsAttachment();
			case CHANNEL -> {
				GuildChannelUnion channelUnion = mapping.getAsChannel();
				if (channelUnion instanceof MessageChannel) {
					yield channelUnion;
				}
				yield channelUnion.getId();
			}
			default -> mapping.getAsString();
		});
	}

	public record LocatedFunction(Interpreter interpreter, ArucasFunction function) {
		public LocatedFunction(Interpreter interpreter, ArucasFunction function) {
			this.interpreter = interpreter.branch();
			this.function = function;
		}

		public Interpreter interpreter() {
			return this.interpreter.branch();
		}
	}
}
