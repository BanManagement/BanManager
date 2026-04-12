package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.kyori.text.Component;

import java.util.List;

public class PaginatedView {

  private static final int DEFAULT_PAGE_SIZE = 8;

  private final List<Component> items;
  private final int pageSize;
  private final String command;

  public PaginatedView(List<Component> items, String command) {
    this(items, command, DEFAULT_PAGE_SIZE);
  }

  public PaginatedView(List<Component> items, String command, int pageSize) {
    this.items = items;
    this.command = command;
    this.pageSize = pageSize;
  }

  public int getTotalPages() {
    return Math.max(1, (int) Math.ceil((double) items.size() / pageSize));
  }

  public void send(CommonSender sender, int page) {
    send(sender, page, null, null);
  }

  public void send(CommonSender sender, int page, Component header, Component footer) {
    int totalPages = getTotalPages();
    int safePage = Math.max(1, Math.min(page, totalPages));
    int start = (safePage - 1) * pageSize;
    int end = Math.min(start + pageSize, items.size());

    if (header != null) {
      sender.sendMessage(header);
    }

    for (int i = start; i < end; i++) {
      sender.sendMessage(items.get(i));
    }

    if (totalPages > 1 && !sender.isConsole()) {
      sender.sendMessage(buildNavigation(safePage, totalPages));
    } else if (totalPages > 1) {
      // Console doesn't support click events, so send a plain legacy string instead
      sender.sendMessage(MessageRenderer.getInstance().toLegacy(
          MessageRenderer.getInstance().render("<gray>Page <white>" + safePage + "</white> of <white>" + totalPages + "</white></gray>")));
    }

    if (footer != null) {
      sender.sendMessage(footer);
    }
  }

  private Component buildNavigation(int currentPage, int totalPages) {
    MessageRenderer renderer = MessageRenderer.getInstance();
    String escapedCommand = renderer.escapeTags(command);

    StringBuilder nav = new StringBuilder();

    if (currentPage > 1) {
      nav.append("<click:run_command:'").append(escapedCommand).append(" ").append(currentPage - 1)
          .append("'><hover:show_text:'<gray>Previous page'><gold>«</gold></hover></click> ");
    }

    nav.append("<gray>Page <white>").append(currentPage).append("</white> of <white>")
        .append(totalPages).append("</white></gray>");

    if (currentPage < totalPages) {
      nav.append(" <click:run_command:'").append(escapedCommand).append(" ").append(currentPage + 1)
          .append("'><hover:show_text:'<gray>Next page'><gold>»</gold></hover></click>");
    }

    return renderer.render(nav.toString());
  }
}
