package org.Commands;

import org.Utiilities.MessageBuilder;

public abstract class AbstractCommand implements ICommand {
    MessageBuilder builder;

    public void setBuilder(MessageBuilder builder) {
        this.builder = builder;
    }
}
