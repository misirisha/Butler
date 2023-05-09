package org.emerald.butler.component;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Command;
import org.emerald.butler.entity.UserCommand;
import org.emerald.butler.entity.UserCommandTrace;
import org.emerald.butler.repository.DwellerRepository;
import org.emerald.butler.repository.UserCommandRepository;
import org.emerald.butler.repository.UserCommandTraceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

@RequiredArgsConstructor
@Component
public class UserCommandManager {
    private static final Logger log = LoggerFactory.getLogger(UserCommandManager.class);

    private final Metadata metadata;
    private final UserCommandRepository userCommandRepository;
    private final UserCommandTraceRepository userCommandTraceRepository;
    private final DwellerRepository dwellerRepository;

    public void set(User user, Command command, String progress) {
        log.debug("Set current command {} for {} user", command.getTelegramCommand(), user.getUserName());
        final UserCommand userCommand = UserCommand.builder(metadata)
                .command(command.getId())
                .dweller(dwellerRepository.findByTelegramId(user.getId()).orElse(null))
                .progress(progress)
                .build();
        userCommandRepository.save(userCommand);
    }

    public Optional<UserCommand> findCurrent(User user) {
        return userCommandRepository.findByDwellerTelegramId(user.getId());
    }

    @Transactional
    public void clear(User user) {
        userCommandRepository.deleteByDwellerTelegramId(user.getId());
    }

    @Transactional
    public void delete(UserCommandTrace trace) {
        userCommandTraceRepository.delete(trace);
    }

    public void update(UserCommand userCommand) {
        userCommandRepository.save(userCommand);
    }

    public UserCommandTrace getLatestTrace(UserCommand command) {
        return Collections.max(command.getTraces(), Comparator.comparing(UserCommandTrace::getOrder));
    }

    public void updateProgress(UserCommand command, String newProgress) {
        updateProgress(command, newProgress, true);
    }

    public void updateProgress(UserCommand command, String newProgress, boolean tracePreviousProgress) {
        String previous = command.getProgress();
        if (tracePreviousProgress) {
            final long previousOrder = userCommandTraceRepository
                    .findFirstByUserCommandOrderByOrderDesc(command)
                    .map(UserCommandTrace::getOrder)
                    .orElse(0L);
            final UserCommandTrace trace = UserCommandTrace.builder(metadata)
                    .order(previousOrder + 1)
                    .userCommand(command)
                    .progressStage(previous)
                    .build();
            userCommandTraceRepository.save(trace);
        }
        command.setProgress(newProgress);
        update(command);

        log.debug("Set current progress {} for {} command. Previous - {}", newProgress, command.getCommand(), previous);
    }
}
