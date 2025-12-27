package com.presencechecker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
// This import is required because your test file and main file are in different packages
import com.presencechecker.PresenceChecker;

public class PresenceCheckerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PresenceChecker.class);
		RuneLite.main(args);
	}
}