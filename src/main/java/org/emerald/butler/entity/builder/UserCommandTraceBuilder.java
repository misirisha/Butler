package org.emerald.butler.entity.builder;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.UserCommand;
import org.emerald.butler.entity.UserCommandTrace;

@RequiredArgsConstructor
public class UserCommandTraceBuilder {
    private final Metadata metadata;
    private Long order;
    private UserCommand userCommand;
    private String progressStage;

    public UserCommandTraceBuilder order(Long order) {
        this.order = order;
        return this;
    }

    public UserCommandTraceBuilder userCommand(UserCommand userCommand) {
        this.userCommand = userCommand;
        return this;
    }

    public UserCommandTraceBuilder progressStage(String progressStage) {
        this.progressStage = progressStage;
        return this;
    }

    public UserCommandTrace build() {
        UserCommandTrace trace = metadata.create(UserCommandTrace.class);
        trace.setOrder(order);
        trace.setUserCommand(userCommand);
        trace.setProgressStage(progressStage);
        return trace;
    }
}
