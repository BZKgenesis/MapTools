package net.bzkgns.maptools.entities;

public abstract class AbstractCommandData<T extends Enum<?>> {

    private String command;
    private T trigger;
    private boolean enabled;

    public AbstractCommandData(String command, T mode) {
        this(command, mode, true);
    }

    public AbstractCommandData(
            String command,
            T mode,
            boolean enabled) {
        this.command = command;
        this.trigger = mode;
        this.enabled = enabled;
    }

    public String getCommand() {
        return command;
    }

    public T getTrigger() {
        return trigger;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setCommand(String cmd) {
        this.command = cmd;
    }

    public void setTrigger(T trigger) {
        this.trigger = trigger;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
