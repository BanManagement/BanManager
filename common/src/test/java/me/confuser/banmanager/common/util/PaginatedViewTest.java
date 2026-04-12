package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.kyori.text.Component;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PaginatedViewTest {

  @Test
  public void totalPagesCalculation() {
    List<Component> items = createItems(25);

    PaginatedView view = new PaginatedView(items, "/test");
    assertEquals(4, view.getTotalPages());
  }

  @Test
  public void totalPagesWithExactFit() {
    List<Component> items = createItems(16);

    PaginatedView view = new PaginatedView(items, "/test");
    assertEquals(2, view.getTotalPages());
  }

  @Test
  public void totalPagesWithSinglePage() {
    List<Component> items = createItems(5);

    PaginatedView view = new PaginatedView(items, "/test");
    assertEquals(1, view.getTotalPages());
  }

  @Test
  public void emptyListGivesOnePage() {
    List<Component> items = new ArrayList<>();

    PaginatedView view = new PaginatedView(items, "/test");
    assertEquals(1, view.getTotalPages());
  }

  @Test
  public void customPageSize() {
    List<Component> items = createItems(10);

    PaginatedView view = new PaginatedView(items, "/test", 5);
    assertEquals(2, view.getTotalPages());
  }

  private List<Component> createItems(int count) {
    List<Component> items = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      items.add(Component.text("Item " + i));
    }
    return items;
  }
}
