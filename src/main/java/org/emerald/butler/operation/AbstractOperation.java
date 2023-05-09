package org.emerald.butler.operation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.component.Sender;
import org.emerald.butler.component.UserCommandManager;
import org.emerald.butler.entity.UserCommand;
import org.emerald.butler.entity.UserCommandTrace;
import org.emerald.butler.repository.DwellerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@RequiredArgsConstructor
public abstract class AbstractOperation implements Operation {
    private static final Logger log = LoggerFactory.getLogger(AbstractOperation.class);

    protected final UserCommandManager userCommandManager;
    protected final Sender sender;
    protected final DwellerRepository dwellerRepository;

    protected abstract Map<String, Consumer<Context>> getProgressesMap();

    @Override
    public void doOperation(Update update) {
        User user = update.getMessage().getFrom();
        UserCommand userCommand = userCommandManager.findCurrent(user).orElseThrow();
        String progress = userCommand.getProgress();

        Map<String, Consumer<Context>> progressesMap = getProgressesMap();
        Optional.ofNullable(progressesMap.get(progress))
                .ifPresent(method -> {
                    String updateText = update.getMessage().getText();
                    final Context context = new Context(progress, update, user, userCommand, updateText);
                    method.accept(context);
                });
    }

    protected void onCancel(Context context) {
        userCommandManager.clear(context.user);
        sender.send("Операция отменена", context.update);
    }

    protected void onError(Context context) {
        sender.send("Неправильная команда", context.update);
    }

    @Transactional
    protected void onBack(Context context) {
        onBack(context, 1);
    }

    @Transactional
    protected void onBack(Context context, int howMuch) {
        final List<UserCommandTrace> traces = context.userCommand.getTraces();
        traces.sort(Comparator.comparing(UserCommandTrace::getOrder));
        if (traces.size() < howMuch) {
            log.warn("Cannot move back - not enough traces is present. Present: {}, required: {}", traces.size(), howMuch);
        } else {
            final UserCommandTrace latest = traces.get(traces.size() - howMuch);
            userCommandManager.updateProgress(context.userCommand, latest.getProgressStage(), false);

            for (int i = traces.size() - 1, j = 0; i >= 0 && j < howMuch; i--, j++) {
                userCommandManager.delete(traces.get(i));
                traces.remove(i);
            }
        }
    }

    @EqualsAndHashCode
    @Getter
    @RequiredArgsConstructor
    protected static final class Chain {
        private final String previous;
        private final String current;
    }

    @RequiredArgsConstructor
    protected static final class Context {
        public final String currentStage;
        public final Update update;
        public final User user;
        public final UserCommand userCommand;
        public final String text;
    }
}
