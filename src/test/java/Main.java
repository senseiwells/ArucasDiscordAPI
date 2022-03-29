import me.senseiwells.arucas.api.ContextBuilder;
import me.senseiwells.arucas.discord.*;
import me.senseiwells.arucas.utils.Context;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		Context context = new ContextBuilder()
			.setDisplayName("System.in")
			.addDefault()
			.addWrappers(
				DiscordAttachmentWrapper::new,
				DiscordBotWrapper::new,
				DiscordEventWrapper::new,
				DiscordMessageWrapper::new,
				DiscordUserWrapper::new,
				DiscordChannelWrapper::new,
				DiscordServerWrapper::new
			)
			.build();
		
		while (true) {
			Scanner scanner = new Scanner(System.in);
			String line = scanner.nextLine();
			if (line.trim().equals("")) {
				continue;
			}
			
			CountDownLatch latch = new CountDownLatch(1);
			context.getThreadHandler().runOnThread(context, "System.in", line, latch);
			latch.await();
		}
	}
}
