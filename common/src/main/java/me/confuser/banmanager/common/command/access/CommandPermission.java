/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.confuser.banmanager.common.command.access;

import me.confuser.banmanager.common.sender.Sender;

/**
 * An enumeration of the permissions required to execute built in BanManager commands.
 */
public enum CommandPermission {
    BMINFO("bminfo", Type.COMMAND),//bm.command.bminfo
    BMACTIVITY("activity", Type.COMMAND),//bm.command.activity
    BANALL("banall", Type.COMMAND),//bm.command.banall
    BANIPALL("banipall", Type.COMMAND),//bm.command.banipall
    ADDNOTEALL("addnoteall", Type.COMMAND),//bm.command.addnoteall
    MUTEALL("muteall", Type.COMMAND),//bm.command.muteall
    TEMPBANALL("tempbanall", Type.COMMAND),//bm.command.tempbanall
    TEMPBANIPALL("tempipbanall", Type.COMMAND),//bm.command.tempipbanall
    TEMPMUTEALL("tempmuteall", Type.COMMAND),//bm.command.tempmuteall
    UNBANALL("unbanall", Type.COMMAND),//bm.command.unbanall
    UNBANIPALL("unbanipall", Type.COMMAND),//bm.command.unbanipall
    UNMUTEALL("unmuteall", Type.COMMAND),//bm.command.unmuteall
    ADDNOTE("addnote", Type.COMMAND),//bm.command.addnote
    BMDELETE("delete", Type.COMMAND),//bm.command.delete
    BAN("ban", Type.COMMAND),//bm.command.ban
    BANIP("banip", Type.COMMAND),//bm.command.banip
    BANIPRANGE("baniprange", Type.COMMAND),//bm.command.baniprange
    BANLIST("banlist", Type.COMMAND),//bm.command.banlist
    WARN("warn", Type.COMMAND),//bm.command.warn
    UNMUTEIP("unmuteip", Type.COMMAND),//bm.command.unmuteip
    UNMUTE("unmute", Type.COMMAND),//bm.command.unmute
    BMROLLBACK("bmrollback", Type.COMMAND),//bm.command.bmrollback
    BMRELOAD("bmreload", Type.COMMAND),//bm.command.bmreload
    BANNAME("banname", Type.COMMAND),//bm.command.banname
    BMCLEAR("clear", Type.COMMAND),//bm.command.clear
    UNBANNAME("unbanname", Type.COMMAND),//bm.command.unbanname
    UNBANIPRANGE("unbaniprange", Type.COMMAND),//bm.command.unbaniprange
    NOTES("notes", Type.COMMAND),//bm.command.notes
    TEMPMUTE("tempmute", Type.COMMAND),//bm.command.tempmute
    MUTE("mute", Type.COMMAND),//bm.command.mute
    UNBANIP("unbanip", Type.COMMAND),//bm.command.unbanip
    DWARN("dwarn", Type.COMMAND),//bm.command.dwarn
    BMEXPORT("export", Type.COMMAND),//bm.command.export
    BMIMPORT("import", Type.COMMAND),//bm.command.import
    ALTS("alts", Type.COMMAND),//bm.command.alts
    KICK("kick", Type.COMMAND),//bm.command.kick
    NLKICK("nlkick", Type.COMMAND),//bm.command.nlkick
    UNBAN("unban", Type.COMMAND),//bm.command.unban
    REPORT("report", Type.COMMAND),//bm.command.report
    BMSYNC("sync", Type.COMMAND),//bm.command.sync
    TEMPBANIP("tempbanip", Type.COMMAND),//bm.command.tempbanip
    TEMPBAN("tempban", Type.COMMAND),//bm.command.tempban
    MUTEIP("muteip", Type.COMMAND),//bm.command.muteip
    REASONS("reasons", Type.COMMAND),//bm.command.reasons
    TEMPWARN("tempwarn", Type.COMMAND),//bm.command.tempwarn
    TEMPMUTEIP("tempmuteip", Type.COMMAND),//bm.command.tempmuteip
    TEMPBANNAME("tempbanname", Type.COMMAND),//bm.command.tempbanname
    TEMPBANIPRANGE("tempbaniprange", Type.COMMAND),//bm.command.tempbaniprange
    REPORTS_ASSIGN("reports.assign", Type.COMMAND),//bm.command.reports.assign





    //TODO remove unneeded
    SYNC("sync", Type.NONE),
    INFO("info", Type.NONE),
    EDITOR("editor", Type.NONE),
    DEBUG("debug", Type.NONE),
    VERBOSE("verbose", Type.NONE),
    TREE("tree", Type.NONE),
    SEARCH("search", Type.NONE),
    CHECK("check", Type.NONE),
    IMPORT("import", Type.NONE),
    EXPORT("export", Type.NONE),
    RELOAD_CONFIG("reloadconfig", Type.NONE),
    BULK_UPDATE("bulkupdate", Type.NONE),
    APPLY_EDITS("applyedits", Type.NONE),
    MIGRATION("migration", Type.NONE),

    CREATE_GROUP("creategroup", Type.NONE),
    DELETE_GROUP("deletegroup", Type.NONE),
    LIST_GROUPS("listgroups", Type.NONE),

    CREATE_TRACK("createtrack", Type.NONE),
    DELETE_TRACK("deletetrack", Type.NONE),
    LIST_TRACKS("listtracks", Type.NONE),

    USER_INFO("info", Type.USER),
    USER_PERM_INFO("permission.info", Type.USER),
    USER_PERM_SET("permission.set", Type.USER),
    USER_PERM_UNSET("permission.unset", Type.USER),
    USER_PERM_SET_TEMP("permission.settemp", Type.USER),
    USER_PERM_UNSET_TEMP("permission.unsettemp", Type.USER),
    USER_PERM_CHECK("permission.check", Type.USER),
    USER_PERM_CHECK_INHERITS("permission.checkinherits", Type.USER),
    USER_PERM_CLEAR("permission.clear", Type.USER),
    USER_PARENT_INFO("parent.info", Type.USER),
    USER_PARENT_SET("parent.set", Type.USER),
    USER_PARENT_SET_TRACK("parent.settrack", Type.USER),
    USER_PARENT_ADD("parent.add", Type.USER),
    USER_PARENT_REMOVE("parent.remove", Type.USER),
    USER_PARENT_ADD_TEMP("parent.addtemp", Type.USER),
    USER_PARENT_REMOVE_TEMP("parent.removetemp", Type.USER),
    USER_PARENT_CLEAR("parent.clear", Type.USER),
    USER_PARENT_CLEAR_TRACK("parent.cleartrack", Type.USER),
    USER_PARENT_SWITCHPRIMARYGROUP("parent.switchprimarygroup", Type.USER),
    USER_META_INFO("meta.info", Type.USER),
    USER_META_SET("meta.set", Type.USER),
    USER_META_UNSET("meta.unset", Type.USER),
    USER_META_SET_TEMP("meta.settemp", Type.USER),
    USER_META_UNSET_TEMP("meta.unsettemp", Type.USER),
    USER_META_ADD_PREFIX("meta.addprefix", Type.USER),
    USER_META_ADD_SUFFIX("meta.addsuffix", Type.USER),
    USER_META_SET_PREFIX("meta.setprefix", Type.USER),
    USER_META_SET_SUFFIX("meta.setsuffix", Type.USER),
    USER_META_REMOVE_PREFIX("meta.removeprefix", Type.USER),
    USER_META_REMOVE_SUFFIX("meta.removesuffix", Type.USER),
    USER_META_ADD_TEMP_PREFIX("meta.addtempprefix", Type.USER),
    USER_META_ADD_TEMP_SUFFIX("meta.addtempsuffix", Type.USER),
    USER_META_SET_TEMP_PREFIX("meta.settempprefix", Type.USER),
    USER_META_SET_TEMP_SUFFIX("meta.settempsuffix", Type.USER),
    USER_META_REMOVE_TEMP_PREFIX("meta.removetempprefix", Type.USER),
    USER_META_REMOVE_TEMP_SUFFIX("meta.removetempsuffix", Type.USER),
    USER_META_CLEAR("meta.clear", Type.USER),
    USER_EDITOR("editor", Type.USER),
    USER_SHOW_TRACKS("showtracks", Type.USER),
    USER_PROMOTE("promote", Type.USER),
    USER_DEMOTE("demote", Type.USER),
    USER_CLEAR("clear", Type.USER),
    USER_CLONE("clone", Type.USER),

    GROUP_INFO("info", Type.GROUP),
    GROUP_PERM_INFO("permission.info", Type.GROUP),
    GROUP_PERM_SET("permission.set", Type.GROUP),
    GROUP_PERM_UNSET("permission.unset", Type.GROUP),
    GROUP_PERM_SET_TEMP("permission.settemp", Type.GROUP),
    GROUP_PERM_UNSET_TEMP("permission.unsettemp", Type.GROUP),
    GROUP_PERM_CHECK("permission.check", Type.GROUP),
    GROUP_PERM_CHECK_INHERITS("permission.checkinherits", Type.GROUP),
    GROUP_PERM_CLEAR("permission.clear", Type.GROUP),
    GROUP_PARENT_INFO("parent.info", Type.GROUP),
    GROUP_PARENT_SET("parent.set", Type.GROUP),
    GROUP_PARENT_SET_TRACK("parent.settrack", Type.GROUP),
    GROUP_PARENT_ADD("parent.add", Type.GROUP),
    GROUP_PARENT_REMOVE("parent.remove", Type.GROUP),
    GROUP_PARENT_ADD_TEMP("parent.addtemp", Type.GROUP),
    GROUP_PARENT_REMOVE_TEMP("parent.removetemp", Type.GROUP),
    GROUP_PARENT_CLEAR("parent.clear", Type.GROUP),
    GROUP_PARENT_CLEAR_TRACK("parent.cleartrack", Type.GROUP),
    GROUP_META_INFO("meta.info", Type.GROUP),
    GROUP_META_SET("meta.set", Type.GROUP),
    GROUP_META_UNSET("meta.unset", Type.GROUP),
    GROUP_META_SET_TEMP("meta.settemp", Type.GROUP),
    GROUP_META_UNSET_TEMP("meta.unsettemp", Type.GROUP),
    GROUP_META_ADD_PREFIX("meta.addprefix", Type.GROUP),
    GROUP_META_ADD_SUFFIX("meta.addsuffix", Type.GROUP),
    GROUP_META_SET_PREFIX("meta.setprefix", Type.GROUP),
    GROUP_META_SET_SUFFIX("meta.setsuffix", Type.GROUP),
    GROUP_META_REMOVE_PREFIX("meta.removeprefix", Type.GROUP),
    GROUP_META_REMOVE_SUFFIX("meta.removesuffix", Type.GROUP),
    GROUP_META_ADD_TEMP_PREFIX("meta.addtempprefix", Type.GROUP),
    GROUP_META_ADD_TEMP_SUFFIX("meta.addtempsuffix", Type.GROUP),
    GROUP_META_SET_TEMP_PREFIX("meta.settempprefix", Type.GROUP),
    GROUP_META_SET_TEMP_SUFFIX("meta.settempsuffix", Type.GROUP),
    GROUP_META_REMOVE_TEMP_PREFIX("meta.removetempprefix", Type.GROUP),
    GROUP_META_REMOVE_TEMP_SUFFIX("meta.removetempsuffix", Type.GROUP),
    GROUP_META_CLEAR("meta.clear", Type.GROUP),
    GROUP_EDITOR("editor", Type.GROUP),
    GROUP_LIST_MEMBERS("listmembers", Type.GROUP),
    GROUP_SHOW_TRACKS("showtracks", Type.GROUP),
    GROUP_SET_WEIGHT("setweight", Type.GROUP),
    GROUP_SET_DISPLAY_NAME("setdisplayname", Type.GROUP),
    GROUP_CLEAR("clear", Type.GROUP),
    GROUP_RENAME("rename", Type.GROUP),
    GROUP_CLONE("clone", Type.GROUP),

    TRACK_INFO("info", Type.TRACK),
    TRACK_APPEND("append", Type.TRACK),
    TRACK_INSERT("insert", Type.TRACK),
    TRACK_REMOVE("remove", Type.TRACK),
    TRACK_CLEAR("clear", Type.TRACK),
    TRACK_RENAME("rename", Type.TRACK),
    TRACK_CLONE("clone", Type.TRACK),

    LOG_RECENT("recent", Type.LOG),
    LOG_USER_HISTORY("userhistory", Type.LOG),
    LOG_GROUP_HISTORY("grouphistory", Type.LOG),
    LOG_TRACK_HISTORY("trackhistory", Type.LOG),
    LOG_SEARCH("search", Type.LOG),
    LOG_NOTIFY("notify", Type.LOG),

    SPONGE_PERMISSION_INFO("permission.info", Type.SPONGE),
    SPONGE_PERMISSION_SET("permission.set", Type.SPONGE),
    SPONGE_PERMISSION_CLEAR("permission.clear", Type.SPONGE),
    SPONGE_PARENT_INFO("parent.info", Type.SPONGE),
    SPONGE_PARENT_ADD("parent.add", Type.SPONGE),
    SPONGE_PARENT_REMOVE("parent.remove", Type.SPONGE),
    SPONGE_PARENT_CLEAR("parent.clear", Type.SPONGE),
    SPONGE_OPTION_INFO("option.info", Type.SPONGE),
    SPONGE_OPTION_SET("option.set", Type.SPONGE),
    SPONGE_OPTION_UNSET("option.unset", Type.SPONGE),
    SPONGE_OPTION_CLEAR("option.clear", Type.SPONGE);

    public static final String ROOT = "bm.";

    private final String node;

    private final Type type;

    CommandPermission(String node, Type type) {
        this.type = type;

        if (type == Type.NONE) {
            this.node = ROOT + node;
        } else {
            this.node = ROOT + type.getTag() + "." + node;
        }
    }

    public String getPermission() {
        return this.node;
    }

    public boolean isAuthorized(Sender sender) {
        return sender.hasPermission(this);
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {

        NONE(null),
        USER("user"),
        GROUP("group"),
        TRACK("track"),
        LOG("log"),
        SPONGE("sponge"),
        COMMAND("command");

        private final String tag;

        Type(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return this.tag;
        }
    }

}
