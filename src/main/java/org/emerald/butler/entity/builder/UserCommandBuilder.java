package org.emerald.butler.entity.builder;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.UserCommand;

@RequiredArgsConstructor
public class UserCommandBuilder {
    private final Metadata metadata;
    private String command;
    private Dweller dweller;
    private String progress;

    public UserCommandBuilder command(String command) {
        this.command = command;
        return this;
    }

    public UserCommandBuilder dweller(Dweller dweller) {
        this.dweller = dweller;
        return this;
    }

    public UserCommandBuilder progress(String progress) {
        this.progress = progress;
        return this;
    }

    public UserCommand build() {
        UserCommand uc = metadata.create(UserCommand.class);
        uc.setCommand(command);
        uc.setDweller(dweller);
        uc.setProgress(progress);
        return uc;
    }
}
