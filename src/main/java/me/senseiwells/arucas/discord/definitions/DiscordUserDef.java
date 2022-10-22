package me.senseiwells.arucas.discord.definitions;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.classes.CreatableDefinition;
import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.discord.DiscordUtils;
import me.senseiwells.arucas.utils.Arguments;
import me.senseiwells.arucas.utils.MemberFunction;
import me.senseiwells.arucas.utils.Util;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_USER;
import static me.senseiwells.arucas.utils.Util.Types.STRING;

@ClassDoc(
	name = DISCORD_USER,
	desc = "This class is used to interact with Discord users.",
	importPath = "discordapi.Discord",
	language = Util.Language.Java
)
public class DiscordUserDef extends CreatableDefinition<User> {
	public DiscordUserDef(Interpreter interpreter) {
		super(DISCORD_USER, interpreter);
	}

	@Nullable
	@Override
	public List<MemberFunction> defineMethods() {
		return List.of(
			MemberFunction.of("getName", this::getName),
			MemberFunction.of("getTag", this::getTag),
			MemberFunction.of("getNameAndTag", this::getNameAndTag),
			MemberFunction.of("getId", this::getId)
		);
	}

	@FunctionDoc(
		name = "getName",
		desc = "This gets the name of the user",
		returns = {STRING, "The name of the user"},
		examples = "user.getName();"
	)
	public String getName(Arguments arguments) {
		User user = arguments.nextPrimitive(this);
		return user.getName();
	}

	@FunctionDoc(
		name = "getTag",
		desc = "This gets the tag of the user, the numbers after the #",
		returns = {STRING, "The tag of the user"},
		examples = "user.getTag();"
	)
	public String getTag(Arguments arguments) {
		User user = arguments.nextPrimitive(this);
		return user.getDiscriminator();
	}

	@FunctionDoc(
		name = "getNameAndTag",
		desc = "This gets the name and tag of the user",
		returns = {STRING, "The name and tag of the user"},
		examples = "user.getNameAndTag();"
	)
	public String getNameAndTag(Arguments arguments) {
		User user = arguments.nextPrimitive(this);
		return user.getAsTag();
	}

	@FunctionDoc(
		name = "getId",
		desc = "This gets the id of the user",
		returns = {STRING, "The id of the user"},
		examples = "user.getId();"
	)
	public String getId(Arguments arguments) {
		User user = arguments.nextPrimitive(this);
		return DiscordUtils.getId(user);
	}
}
