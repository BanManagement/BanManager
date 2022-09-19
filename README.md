<p align="center">
  <a href="https://banmanagement.com">
    <img src="https://banmanagement.com/images/banmanager-icon.png" height="128">
    <h1 align="center">BanManager</h1>
  </a>
</p>

<h3 align="center">
	The defacto plugin for Minecraft to manage punishments and moderate more effectively
</h3>

<p align="center">
	<strong>
		<a href="https://banmanagement.com">Website</a>
		|
		<a href="https://banmanagement.com/docs/banmanager/install">Docs</a>
		|
		<a href="https://demo.banmanagement.com">Demo</a>
	</strong>
</p>
<p align="center">
  <a aria-label="Tests status" href="https://github.com/BanManagement/BanManager/actions/workflows/build.yml">
    <img alt="" src="https://img.shields.io/github/workflow/status/BanManagement/BanManager/Java%20CI?label=Tests&style=for-the-badge&labelColor=000000">
  </a>
  <a aria-label="Join the community on Discord" href="https://discord.gg/59bsgZB">
    <img alt="" src="https://img.shields.io/discord/664808009393766401?label=Support&style=for-the-badge&labelColor=000000&color=7289da">
  </a>
</p>

## Overview
- **Free.** Open source and free to use
- **Robust.** Used and battle tested by some of the largest Minecraft servers
- **Maintained.** Actively developed since 2012, 10+ years
- **Cross platform.** Supports Bukkit, Spigot, Bungeecord & Sponge
- **Feature rich.** An advanced punishment system, reports, appeals, network friendly and [website compatible](https://github.com/BanManagement/BanManager-WebUI)
- **Flexible.** [Fully customisable](https://banmanagement.com/docs/banmanager/configuration) with [extensive player permissions](https://banmanagement.com/docs/banmanager/permissions)

To learn more about configuration, usage and features of BanManager, take a look at [the website](https://banmanagement.com/docs/banmanager/configuration) or view [the website demo](https://demo.banmanagement.com).

## Requirements
- Java 8+
- CraftBukkit/Spigot/Paper, BungeeCord or Sponge for Minecraft 1.7.2+
- Optionally [MySQL or MariaDB](https://banmanagement.com/docs/banmanager/install#setup-shared-database-optional)

## Installation
- Download from https://banmanagement.com/download
- Copy jar to plugins (Spigot/BungeeCord) or mods (Sponge) folder
- For further instructions on how to support multiple servers [click here](https://banmanagement.com/docs/banmanager/install-network)

## Commands
View [full list here](https://banmanagement.com/docs/banmanager/commands)
- `/ban <player> <reason>` - Permanently ban a player, requires permission, requires permission `bm.command.ban`
- `/tempban <player> <timeDiff> <reason>` - Temporarily ban a player, requires permission `bm.command.tempban`
- `/unban <player> [reason]` - Unban a player, requires permission `bm.command.unban`
- `/mute <player> <reason>` - Permanently mute a player, requires permission `bm.command.mute`
- `/tempmute <player> <timeDiff> <reason>` - Temporarily mute a player, requires permission `bm.command.tempmute`
- `/unmute <player> [reason]` - Unmute a player, requires permission `bm.command.unmute`
- `/banip <player || ip> <reason>` - Permanently ban an ip address or ip of a player, requires permission `bm.command.banip`
- `/tempbanip <player || ip> <timeDiff> <reason>` - Temporarily ban an ip address or ip of a player, requires permission `bm.command.tempbanip`
- `/unbanip <ip> [reason]` - Unban an ip address, requires permission `bm.command.unbanip`
- `/muteip <player || ip> <reason>` - Permanently mute an ip address or ip of a player, requires permission `bm.command.muteip`
- `/tempmuteip <player || ip> <timeDiff> <reason>` - Temporarily mute an ip address or ip of a player, requires permission `bm.command.tempmuteip`
- `/unmuteip <ip> [reason]` - Unmute an ip address, requires permission `bm.command.unmuteip`
- `/baniprange <cidr || wildcard> <reason>` - Permanently ban a cidr or wildcard ip range, e.g. 192.168.0.1/16 or 192.168.*.*, requires permission `bm.command.baniprange`
- `/tempbaniprange <cidr || wildcard> <timeDiff> <reason>` - Temporarily ban a cidr or wildcard ip range, requires permission `bm.command.tempbaniprange`
- `/unbaniprange <cidr || wildcard || player>` - Unban an ip range, requires permission `bm.command.unbaniprange`
- `/warn <player> <reason>` - Warn a player, requires permission `bm.command.warn`
- `/tempwarn <player> <timeDiff> <reason>` - Temporarily warn a player, requires permission `bm.command.tempwarn`
- `/dwarn <player>` - Delete the last warning a player received, requires permission `bm.command.dwarn`
- `/addnote <player> <message>` - Add a note against a player, requires permission `bm.command.addnote`
- `/notes [player]` - View notes of all online players or a particular player, requires permission `bm.command.notes`
- `/kick <player> <reason>` - Kick a player from the server, requires permission `bm.command.kick`
- `/nlkick <player> <reason>` - Kick a player from the server without logging the kick if kick logging enabled, requires permission `bm.command.nlkick`
- `/bminfo [player]` - Look up information of a player, requires permission `bm.command.bminfo`
- `/bmimport`, Check the [migration guides](https://banmanagement.com/docs/banmanager/migrations) for more information, requires permission `bm.command.import`
- `/bmexport <players || ips>` - Export bans to vanilla format, requires permission `bm.command.export`
- `/bmreload` - Reload plugin configuration and messages (excludes database connection info), requires permission `bm.command.reload`
- `/banlist [players || ipranges || ips]` - List all bans stored in memory, requires permission `bm.command.banlist`
- `/bmsync <local || external>` - Force the server to syncronise with the database, requires permission `bm.command.sync`
- `/bmclear <player> [banrecords || baniprecords || kicks || muterecords || notes || warnings]` - Clear all records of a player or specify a type, requires permission `bm.command.clear`
- `/bmdelete <banrecords || kicks || muterecords || notes || warnings> <ids>` - Delete specific records for a player based on ids from /bminfo, requires permission `bm.command.delete`
- `/bmactivity <timeDiff> [player]` - View recent activity of staff, or a particular player, requires permission `bm.command.bmactivity`
- `/alts <player || ip>` - List players which have the same ip address, requires permission `bm.command.alts`
- `/report <player> <reason>` - Report a player for rule breaking, logs their location and the actors location, as well as other data, requires permission `bm.command.report`
- `/reports` - Report management, executes list by default, requires permission `bm.command.reports`
- `/reports assign <ids> [player]` - Assign a report to a player, if none given assigns to self, requires permission `bm.command.reports.assign`
- `/reports close <ids> [/command || comment]` - Marks a report as closed, with an optional comment or command, requires permission `bm.command.reports.close`
- `/reports list [page] [state]` - Lists reports, requires permission `bm.command.reports.list`
- `/reports tp <id>` - Teleports you to where the report was created, requires permission `bm.command.reports.tp`
- `/reports unassign <ids>` - Unassigns reports from a player, requires permission `bm.command.reports.unassign`
- `/bmrollback <player> <timeDiff> [types]` - Allows rolling back malicious actions by a staff member, requires permission `bm.command.bmrollback`
- `/banname <name> <reason>` - Ban any players with the name specified, requires permission `bm.command.banname`
- `/tempbanname <name> <timeDiff> <reason>` - Temporarily ban a name, requires permission `bm.command.tempbanname`
- `/unbanname <name> [reason]` - Unban a name, requires permission `bm.command.unbanname`
- `/bmutils <duplicates||missingplayers>` - Utility commands to aid with resolving issues, requires permission `bm.command.bmutils`
- `/bmutils duplicates [UUID] [newName]` - Finds duplicate player names and allows manual updating of a player name, requires permission `bm.command.bmutils.duplicates`
- `/bmutils missingplayers` - Finds missing player records associated to punishments and creates them, requires permission `bm.command.bmutils.missingplayers`

## Permissions
View [full list here](https://banmanagement.com/docs/banmanager/permissions)

## Development
```
git clone git@github.com:BanManagement/BanManager.git
```

## Contributing
If you'd like to contribute, please fork the repository and use a feature branch. Pull requests are warmly welcome.

## Help / Bug / Feature Request
If you have found a bug please [open an issue](https://github.com/BanManagement/BanManager/issues/new) with as much detail as possible, including relevant logs and screenshots where applicable

Have an idea for a new feature? Feel free to [open an issue](https://github.com/BanManagement/BanManager/issues/new) or [join us on Discord](https://discord.gg/59bsgZB) to chat

## License
Free to use under the [Creative Commons Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales](LICENCE)
