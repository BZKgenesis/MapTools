package net.bzkgns.maptools.entities;

public abstract class AbstractCommandData<T extends Enum<?>> {

    private String command;
    private T trigger;

    public AbstractCommandData(
            String command,
            T mode) {
        this.command = command;
        this.trigger = mode;
    }

    public String getCommand() {
        return command;
    }

    public T getTrigger() {
        return trigger;
    }

    public void setCommand(String cmd) {
        this.command = cmd;
    }

    public void setTrigger(T trigger) {
        this.trigger = trigger;
    }

}
