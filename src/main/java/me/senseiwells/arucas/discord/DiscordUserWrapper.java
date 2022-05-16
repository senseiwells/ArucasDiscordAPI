package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.api.wrappers.ArucasClass;
import me.senseiwells.arucas.api.wrappers.ArucasDefinition;
import me.senseiwells.arucas.api.wrappers.ArucasFunction;
import me.senseiwells.arucas.api.wrappers.IArucasWrappedClass;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.values.StringValue;
import me.senseiwells.arucas.values.classes.WrapperClassDefinition;
import me.senseiwells.arucas.values.classes.WrapperClassValue;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_USER;
import static me.senseiwells.arucas.utils.ValueTypes.STRING;

@SuppressWarnings("unused")
@ClassDoc(
	name = DISCORD_USER,
	desc = "This class is used to interact with Discord users."
)
@ArucasClass(name = "DiscordUser")
public class DiscordUserWrapper implements IArucasWrappedClass {
	@ArucasDefinition
	public static WrapperClassDefinition DEFINITION;

	private User user;

	@FunctionDoc(
		name = "getName",
		desc = "This gets the name of the user",
		returns = {STRING, "The name of the user"},
		example = "user.getName();"
	)
	@ArucasFunction
	public StringValue getName(Context context) {
		return StringValue.of(this.user.getName());
	}

	@FunctionDoc(
		name = "getTag",
		desc = "This gets the tag of the user, the numbers after the #",
		returns = {STRING, "The tag of the user"},
		example = "user.getTag();"
	)
	@ArucasFunction
	public StringValue getTag(Context context) {
		return StringValue.of(this.user.getDiscriminator());
	}

	@FunctionDoc(
		name = "getNameAndTag",
		desc = "This gets the name and tag of the user",
		returns = {STRING, "The name and tag of the user"},
		example = "user.getNameAndTag();"
	)
	@ArucasFunction
	public StringValue getNameAndTag(Context context) {
		return StringValue.of(this.user.getAsTag());
	}

	@FunctionDoc(
		name = "getId",
		desc = "This gets the id of the user",
		returns = {STRING, "The id of the user"},
		example = "user.getId();"
	)
	@ArucasFunction
	public StringValue getId(Context context) {
		return DiscordUtils.getId(this.user);
	}

	public User getUser() {
		return this.user;
	}

	public static WrapperClassValue newDiscordUser(User user, Context context) throws CodeError {
		DiscordUserWrapper userWrapper = new DiscordUserWrapper();
		userWrapper.user = user;
		return DEFINITION.createNewDefinition(userWrapper, context, List.of());
	}

	@Override
	public User asJavaValue() {
		return this.user;
	}
}
