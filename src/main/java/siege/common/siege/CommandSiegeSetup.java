package siege.common.siege;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;

public class CommandSiegeSetup extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "siege_setup";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/siege_setup <...> (use TAB key to autocomplete parameters)";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		EntityPlayerMP operator = getCommandSenderAsPlayer(sender);
		if (args.length >= 1)
		{
			String option = args[0];

            switch (option) {
                case "new" -> {
                    String siegeName = args[1];
                    if (!SiegeDatabase.validSiegeName(siegeName)) {
                        throw new CommandException("Invalid siege name %s", siegeName);
                    }
                    if (SiegeDatabase.siegeExists(siegeName)) {
                        throw new CommandException("A siege named %s already exists!", siegeName);
                    }

                    Siege siege = new Siege(siegeName);
                    SiegeDatabase.addAndSaveSiege(siege);
                    func_152373_a(sender, this, "Created a new siege %s", siegeName);
                    return;
                }
                case "edit" -> {
                    String siegeName = args[1];
                    Siege siege = SiegeDatabase.getSiege(siegeName);
                    if (siege != null) {
                        String sFunction = args[2];

                        switch (sFunction) {
                            case "rename" -> {
                                String newName = args[3];
                                if (!SiegeDatabase.validSiegeName(newName)) {
                                    throw new CommandException("Invalid siege rename %s", newName);
                                }
                                if (SiegeDatabase.siegeExists(newName)) {
                                    throw new CommandException("A siege named %s already exists!", newName);
                                }
                                siege.rename(newName);
                                func_152373_a(sender, this, "Renamed siege %s to %s", siegeName, newName);
                                return;
                            }
                            case "setcoords" -> {
                                int posX = MathHelper.floor_double(func_110666_a(sender, operator.posX, args[3]));
                                int posZ = MathHelper.floor_double(func_110666_a(sender, operator.posZ, args[4]));
                                int radius = parseIntBounded(sender, args[5], 0, Siege.MAX_RADIUS);
                                int dim = operator.dimension;
                                siege.setCoords(dim, posX, posZ, radius);
                                func_152373_a(sender, this, "Set location of siege %s to [x=%s, z=%s, r=%s] in dim-%s", siegeName, String.valueOf(posX), String.valueOf(posZ), String.valueOf(radius), String.valueOf(dim));
                                return;
                            }
                            case "teams" -> {
                                String teamOption = args[3];
                                switch (teamOption) {
                                    case "new" -> {
                                        String teamName = args[4];
                                        if (!SiegeDatabase.validTeamName(teamName)) {
                                            throw new CommandException("Invalid team name %s", teamName);
                                        }
                                        if (siege.getTeam(teamName) != null) {
                                            throw new CommandException("Siege %s already has a team named %s!", siegeName, teamName);
                                        }

                                        siege.createNewTeam(teamName);
                                        func_152373_a(sender, this, "Created new team %s for siege %s", teamName, siegeName);
                                        return;
                                    }
                                    case "edit" -> {
                                        String teamName = args[4];
                                        SiegeTeam team = siege.getTeam(teamName);
                                        if (team != null) {
                                            String teamFunction = args[5];

                                            switch (teamFunction) {
                                                case "rename" -> {
                                                    String teamRename = args[6];
                                                    if (!SiegeDatabase.validTeamName(teamRename)) {
                                                        throw new CommandException("Invalid team rename %s", teamRename);
                                                    }
                                                    if (siege.getTeam(teamRename) != null) {
                                                        throw new CommandException("A team named %s already exists in siege %s!", teamRename, siegeName);
                                                    }
                                                    team.rename(teamRename);
                                                    func_152373_a(sender, this, "Renamed team %s to %s in siege %s", teamName, teamRename, siegeName);
                                                    return;
                                                }
                                                case "kit-add" -> {
                                                    String kitName = args[6];
                                                    if (!KitDatabase.kitExists(kitName)) {
                                                        throw new CommandException("Kit %s does not exist", kitName);
                                                    } else {
                                                        Kit kit = KitDatabase.getKit(kitName);
                                                        if (team.containsKit(kit)) {
                                                            throw new CommandException("Siege %s team %s already includes kit %s!", siegeName, teamName, kitName);
                                                        }

                                                        team.addKit(kit);
                                                        func_152373_a(sender, this, "Added kit %s for team %s in siege %s", kitName, teamName, siegeName);
                                                        return;
                                                    }
                                                }
                                                case "kit-remove" -> {
                                                    String kitName = args[6];
                                                    if (!KitDatabase.kitExists(kitName)) {
                                                        throw new CommandException("Kit %s does not exist", kitName);
                                                    } else {
                                                        Kit kit = KitDatabase.getKit(kitName);
                                                        if (!team.containsKit(kit)) {
                                                            throw new CommandException("Siege %s team %s does not include kit %s!", siegeName, teamName, kitName);
                                                        }

                                                        team.removeKit(kit);
                                                        func_152373_a(sender, this, "Removed kit %s from team %s in siege %s", kitName, teamName, siegeName);
                                                        return;
                                                    }
                                                }
                                                case "kit-limit" -> {
                                                    String kitName = args[6];
                                                    if (!KitDatabase.kitExists(kitName)) {
                                                        throw new CommandException("Kit %s does not exist", kitName);
                                                    } else {
                                                        Kit kit = KitDatabase.getKit(kitName);
                                                        if (!team.containsKit(kit)) {
                                                            throw new CommandException("Siege %s team %s does not include kit %s!", siegeName, teamName, kitName);
                                                        }

                                                        int limit = parseIntWithMin(sender, args[7], 0);
                                                        team.limitKit(kit, limit);
                                                        func_152373_a(sender, this, "Limited kit %s to %s players for team %s in siege %s", kitName, String.valueOf(limit), teamName, siegeName);
                                                        return;
                                                    }
                                                }
                                                case "kit-unlimit" -> {
                                                    String kitName = args[6];
                                                    if (!KitDatabase.kitExists(kitName)) {
                                                        throw new CommandException("Kit %s does not exist", kitName);
                                                    } else {
                                                        Kit kit = KitDatabase.getKit(kitName);
                                                        if (!team.containsKit(kit)) {
                                                            throw new CommandException("Siege %s team %s does not include kit %s!", siegeName, teamName, kitName);
                                                        }
                                                        if (!team.isKitLimited(kit)) {
                                                            throw new CommandException("Kit %s is not limited for team %s in siege %s", kitName, teamName, siegeName);
                                                        }

                                                        team.unlimitKit(kit);
                                                        func_152373_a(sender, this, "Unlimited kit %s for team %s in siege %s", kitName, teamName, siegeName);
                                                        return;
                                                    }
                                                }
                                                case "setspawn" -> {
                                                    int spawnX = MathHelper.floor_double(func_110666_a(sender, operator.posX, args[6]));
                                                    int spawnY = MathHelper.floor_double(func_110666_a(sender, operator.posY, args[7]));
                                                    int spawnZ = MathHelper.floor_double(func_110666_a(sender, operator.posZ, args[8]));
                                                    team.setRespawnPoint(spawnX, spawnY, spawnZ);
                                                    func_152373_a(sender, this, "Set siege %s team %s respawn point to [%s, %s, %s]", siegeName, teamName, String.valueOf(spawnX), String.valueOf(spawnY), String.valueOf(spawnZ));
                                                    return;
                                                }
                                            }
                                        } else {
                                            throw new CommandException("Siege %s has no team named %s", siegeName, teamName);
                                        }
                                    }
                                    case "remove" -> {
                                        String teamName = args[4];
                                        if (siege.removeTeam(teamName)) {
                                            func_152373_a(sender, this, "Removed team %s from siege %s", teamName, siegeName);
                                            return;
                                        } else {
                                            throw new CommandException("Could not remove team %s from siege %s", teamName, siegeName);
                                        }
                                    }
                                }
                            }
                            case "max-team-diff" -> {
                                int maxDiff = parseIntWithMin(sender, args[3], 0);
                                siege.setMaxTeamDifference(maxDiff);
                                func_152373_a(sender, this, "Set siege %s max team difference to %s", siegeName, String.valueOf(maxDiff));
                                return;
                            }
                            case "respawn-immunity" -> {
                                int seconds = parseIntWithMin(sender, args[3], 0);
                                siege.setRespawnImmunity(seconds);
                                func_152373_a(sender, this, "Set siege %s respawn immunity to %s", siegeName, String.valueOf(seconds));
                                return;
                            }
                            case "friendly-fire" -> {
                                String ffOption = args[3];
                                if (ffOption.equals("on")) {
                                    siege.setFriendlyFire(true);
                                    func_152373_a(sender, this, "Enabled friendly fire in siege %s", siegeName);
                                    return;
                                } else if (ffOption.equals("off")) {
                                    siege.setFriendlyFire(false);
                                    func_152373_a(sender, this, "Disabled friendly fire in siege %s", siegeName);
                                    return;
                                }
                            }
                            case "mob-spawning" -> {
                                String mobOption = args[3];
                                if (mobOption.equals("on")) {
                                    siege.setMobSpawning(true);
                                    func_152373_a(sender, this, "Enabled mob spawning in siege %s", siegeName);
                                    return;
                                } else if (mobOption.equals("off")) {
                                    siege.setMobSpawning(false);
                                    func_152373_a(sender, this, "Disabled mob spawning in siege %s", siegeName);
                                    return;
                                }
                            }
                            case "terrain-protect" -> {
                                String tOption = args[3];
                                if (tOption.equals("on")) {
                                    siege.setTerrainProtect(true);
                                    func_152373_a(sender, this, "Enabled terrain protection in siege %s", siegeName);
                                    return;
                                } else if (tOption.equals("off")) {
                                    siege.setTerrainProtect(false);
                                    func_152373_a(sender, this, "Disabled terrain protection in siege %s", siegeName);
                                    return;
                                }
                            }
                            case "terrain-protect-inactive" -> {
                                String tOption = args[3];
                                if (tOption.equals("on")) {
                                    siege.setTerrainProtectInactive(true);
                                    func_152373_a(sender, this, "Enabled inactive terrain protection in siege %s", siegeName);
                                    return;
                                } else if (tOption.equals("off")) {
                                    siege.setTerrainProtectInactive(false);
                                    func_152373_a(sender, this, "Disabled inactive terrain protection in siege %s", siegeName);
                                    return;
                                }
                            }
                            case "dispel" -> {
                                String tOption = args[3];
                                if (tOption.equals("on")) {
                                    siege.setDispelOnEnd(true);
                                    func_152373_a(sender, this, "Enabled dispel-on-end in siege %s", siegeName);
                                    return;
                                } else if (tOption.equals("off")) {
                                    siege.setDispelOnEnd(false);
                                    func_152373_a(sender, this, "Disabled dispel-on-end in siege %s", siegeName);
                                    return;
                                }
                            }
                        }
                    } else {
                        throw new CommandException("No siege for name %s", siegeName);
                    }
                }
                case "start" -> {
                    String siegeName = args[1];
                    Siege siege = SiegeDatabase.getSiege(siegeName);
                    if (siege != null) {
                        if (siege.isActive()) {
                            throw new CommandException("Siege %s is already active!", siegeName);
                        } else if (!siege.canBeStarted()) {
                            throw new CommandException("Siege %s cannot be started - it requires a location and at least one team", siegeName);
                        }

                        if (args.length >= 3) {
                            int seconds = parseIntWithMin(sender, args[2], 0);
                            int durationTicks = seconds * 20;
                            siege.startSiege(durationTicks);

                            String timeDisplay = Siege.ticksToTimeString(durationTicks);
                            func_152373_a(sender, this, "Started a new siege %s lasting for %s", siegeName, timeDisplay);
                            return;
                        } else {
                            throw new CommandException("Specify the siege duration (in seconds)");
                        }

                    } else {
                        throw new CommandException("No siege for name %s", siegeName);
                    }
                }
                case "active" -> {
                    String siegeName = args[1];
                    Siege siege = SiegeDatabase.getSiege(siegeName);
                    if (siege != null && siege.isActive()) {
                        String sFunction = args[2];

                        if (sFunction.equals("extend")) {
                            int seconds = parseIntWithMin(sender, args[3], 0);
                            int durationTicks = seconds * 20;
                            siege.extendSiege(durationTicks);

                            String timeDisplay = Siege.ticksToTimeString(durationTicks);

                            int fullDuration = siege.getTicksRemaining();
                            String fullTimeDisplay = Siege.ticksToTimeString(fullDuration);

                            func_152373_a(sender, this, "Extended siege %s for %s - now lasting for %s", siegeName, timeDisplay, fullTimeDisplay);
                            return;
                        } else if (sFunction.equals("end")) {
                            siege.endSiege();
                            func_152373_a(sender, this, "Ended siege %s", siegeName);
                            return;
                        }
                    } else {
                        throw new CommandException("No active siege for name %s", siegeName);
                    }
                }
                case "delete" -> {
                    String siegeName = args[1];
                    Siege siege = SiegeDatabase.getSiege(siegeName);
                    if (siege != null) {
                        SiegeDatabase.deleteSiege(siege);
                        func_152373_a(sender, this, "Deleted siege %s", siegeName);
                        return;

                    } else {
                        throw new CommandException("No siege for name %s", siegeName);
                    }
                }
            }
		}
		
		throw new WrongUsageException(getCommandUsage(sender));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
        	return getListOfStringsMatchingLastWord(args, "new", "edit", "start", "active", "delete");
        }
        if (args.length >= 2)
        {
        	String sOption = args[0];
            switch (sOption) {
                case "edit" -> {
                    if (args.length == 2) {
                        return getListOfStringsMatchingLastWord(args, SiegeDatabase.getAllSiegeNames().toArray(new String[0]));
                    }
                    if (args.length == 3) {
                        return getListOfStringsMatchingLastWord(args, "rename", "setcoords", "teams", "max-team-diff", "respawn-immunity", "friendly-fire", "mob-spawning", "terrain-protect", "terrain-protect-inactive", "dispel");
                    }
                    String siegeName = args[1];
                    Siege siege = SiegeDatabase.getSiege(siegeName);
                    String sFunction = args[2];
                    if (sFunction.equals("teams")) {
                        if (args.length == 4) {
                            return getListOfStringsMatchingLastWord(args, "new", "edit", "remove");
                        }
                        String teamOption = args[3];
                        if (teamOption.equals("edit")) {
                            if (args.length == 5) {
                                return getListOfStringsMatchingLastWord(args, siege.listTeamNames().toArray(new String[0]));
                            }
                            String teamName = args[4];
                            SiegeTeam team = siege.getTeam(teamName);
                            if (args.length == 6) {
                                return getListOfStringsMatchingLastWord(args, "rename", "kit-add", "kit-remove", "kit-limit", "kit-unlimit", "setspawn");
                            }
                            String teamFunction = args[5];
                            if (teamFunction.equals("kit-add")) {
                                return getListOfStringsMatchingLastWord(args, team.listUnincludedKitNames().toArray(new String[0]));
                            } else if (teamFunction.equals("kit-remove") || teamFunction.equals("kit-limit") || teamFunction.equals("kit-unlimit")) {
                                return getListOfStringsMatchingLastWord(args, team.listKitNames().toArray(new String[0]));
                            }
                        }
                        if (teamOption.equals("remove")) {
                            return getListOfStringsMatchingLastWord(args, siege.listTeamNames().toArray(new String[0]));
                        }
                    } else if (sFunction.equals("friendly-fire") || sFunction.equals("mob-spawning") || sFunction.equals("terrain-protect") || sFunction.equals("terrain-protect-inactive") || sFunction.equals("dispel")) {
                        return getListOfStringsMatchingLastWord(args, "on", "off");
                    }
                }
                case "start" -> {
                    return getListOfStringsMatchingLastWord(args, SiegeDatabase.listInactiveSiegeNames().toArray(new String[0]));
                }
                case "active" -> {
                    if (args.length == 2) {
                        return getListOfStringsMatchingLastWord(args, SiegeDatabase.listActiveSiegeNames().toArray(new String[0]));
                    }
                    if (args.length == 3) {
                        return getListOfStringsMatchingLastWord(args, "extend", "end");
                    }
                }
                case "delete" -> {
                    return getListOfStringsMatchingLastWord(args, SiegeDatabase.getAllSiegeNames().toArray(new String[0]));
                }
            }
        }
        return null;
    }
}
