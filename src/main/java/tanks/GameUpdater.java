package tanks;

import basewindow.IUpdater;
import tanks.extension.Extension;
import tanks.handle.*;

public class GameUpdater implements IUpdater
{
	@Override
	public void update()
	{
		try
		{
			if (Game.enableExtensions)
			{
				for (int i = 0; i < Game.extensionRegistry.extensions.size(); i++)
				{
					Extension e = Game.extensionRegistry.extensions.get(i);

					e.preUpdate();
				}
			}

			Panel.panel.update();

			if (Game.enableExtensions)
			{
				for (int i = 0; i < Game.extensionRegistry.extensions.size(); i++)
				{
					Extension e = Game.extensionRegistry.extensions.get(i);

					e.update();
				}
			}
		}
		catch (Throwable e)
		{
			if (e instanceof GameCrashedException)
				Game.displayCrashScreen(((GameCrashedException) e).originalException);
			else
				Game.displayCrashScreen(e);
            HandleRegistry.callAllHandles(ICrashHandler.class, h -> ((ICrashHandler) h).handle(e));
		}
	}
}
