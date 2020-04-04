package io.eventuate;

public class MissingProcessMethodException extends EventuateCommandProcessingFailedUnexpectedlyException {
  private final Command command;

  public MissingProcessMethodException(Throwable e, Command command) {
    super(e);
    this.command = command;
  }

  public Command getCommand() {
    return command;
  }
}
