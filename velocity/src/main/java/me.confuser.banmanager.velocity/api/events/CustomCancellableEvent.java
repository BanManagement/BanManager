package me.confuser.banmanager.velocity.api.events;

import com.velocitypowered.api.event.ResultedEvent;

import java.util.Objects;


public abstract class CustomCancellableEvent extends CustomEvent implements ResultedEvent<ResultedEvent.GenericResult> {

  private GenericResult result = GenericResult.allowed();

  public CustomCancellableEvent() {
    super();
  }

  @Override
  public void setResult(GenericResult result) {
    this.result = Objects.requireNonNull(result);
  }

  public GenericResult getResult() {
    return result;
  }

}
