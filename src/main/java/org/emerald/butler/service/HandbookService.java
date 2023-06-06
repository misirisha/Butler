package org.emerald.butler.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Handbook;
import org.emerald.butler.entity.HandbookCategory;
import org.emerald.butler.entity.HandbookItem;
import org.emerald.butler.operation.Context;
import org.emerald.butler.repository.HandbookRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class HandbookService {
    private final HandbookRepository handbookRepository;
    private final Metadata metadata;
    private final TelegramChatService telegramChatService;
    private final DataManager dataManager;
    private final FetchPlans plans;

    public Handbook getOrCreate(Long chatId) {
        final Optional<Handbook> optional = handbookRepository.findByChatTelegramChatId(handbookPlan(), chatId.toString());
        if (optional.isPresent()) {
            return optional.get();
        }

        final Handbook created = create(chatId);
        final SaveContext context = new SaveContext();
        context.saving(created);
        context.saving(created.getCategories());
        return dataManager.save(context).get(created);
    }

    public HandbookItem createItem(HandbookCategory category, String value) {
        final HandbookItem item = metadata.create(HandbookItem.class);
        item.setCategory(category);
        item.setValue(value);
        return item;
    }

    public List<HandbookItem> getAllItems(Context context, String categoryName) {
        return getAllItems(context.update.getMessage().getChatId(), categoryName);
    }

    public List<HandbookItem> getAllItems(Long chatId, String categoryName) {
        final Handbook handbook = getOrCreate(chatId);
        return handbook.findCategoryByName(categoryName)
                .map(HandbookCategory::getItems)
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
    }

    private Handbook create(Long chatId) {
        final Handbook handbook = metadata.create(Handbook.class);
        handbook.setChat(telegramChatService.getOrCreate(chatId));
        handbook.setCategories(List.of(
                createCategory(handbook, HandbookCategory.PHONES),
                createCategory(handbook, HandbookCategory.REPAIRS),
                createCategory(handbook, HandbookCategory.MEETINGS)
        ));

        return handbook;
    }

    private HandbookCategory createCategory(Handbook handbook, String name) {
        final HandbookCategory category = metadata.create(HandbookCategory.class);
        category.setHandbook(handbook);
        category.setName(name);
        return category;
    }

    private FetchPlan handbookPlan() {
        return plans.builder(Handbook.class).addFetchPlan(FetchPlan.BASE)
                .add("categories", categoryBuilder -> categoryBuilder
                        .addFetchPlan(FetchPlan.BASE)
                        .add("items", FetchPlan.BASE)
                )
                .build();
    }
}
