package org.emerald.butler.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Apartment;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.DwellerApartmentRole;
import org.emerald.butler.operation.Context;
import org.emerald.butler.repository.DwellerApartmentRoleRepository;
import org.emerald.butler.repository.DwellerRepository;
import org.emerald.butler.util.NumericCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

@RequiredArgsConstructor
@Service
public class DwellerService {
    private static final Logger log = LoggerFactory.getLogger(DwellerService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final DwellerRepository dwellerRepository;
    private final DwellerApartmentRoleRepository dwellerApartmentRoleRepository;
    private final FetchPlans plans;

    public Collection<Integer> getApartmentsNumbers(Context context) {
        final User user = context.user;
        final Optional<Dweller> optional = dwellerRepository.findByTelegramId(user.getId());
        //TODO: here may be a lot of dwellers cause they are linked to chat
        if (optional.isEmpty()) {
            log.error("Not found a dweller for given telegram user. The user' id: " + user.getId());
            return Collections.emptyList();
        }

        final FetchPlan plan = plans.builder(DwellerApartmentRole.class).add("apartment", FetchPlan.BASE).build();
        final Dweller dweller = optional.get();
        return dwellerApartmentRoleRepository.findAllByDweller(plan, dweller).stream()
                .map(DwellerApartmentRole::getApartment)
                .map(Apartment::getNumber)
                .collect(Collectors.toList());
    }

    public String getBirthDate(Context context, String defaultValue) {
        return findBirthDate(context).orElse(defaultValue);
    }

    public Optional<String> findBirthDate(Context context) {
        final Dweller dweller = getDweller(context);
        final LocalDate birthDate = dweller.getBirthDate();
        if (Objects.isNull(birthDate)) {
            return Optional.empty();
        }

        return Optional.of(birthDate.format(FORMATTER));
    }

    public Dweller getDweller(Context context) {
        return dwellerRepository.findByTelegramId(context.user.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Not found telegram user for user with id: " + context.user.getId())
                );
    }

    public void clearBirthDate(Context context) {
        final Dweller dweller = getDweller(context);
        dweller.setBirthDate(null);
        dwellerRepository.save(dweller);
    }

    public Optional<LocalDate> parse(String text) {
        final String[] byDots = text.split("\\.");
        if (byDots.length == 3) {
            return parse(byDots).map(this::toDate);
        }

        final String[] bySpaces = text.split(" ");
        if (bySpaces.length == 3) {
            return parse(bySpaces).map(this::toDate);
        }

        return Optional.empty();
    }

    public void updateBirthDate(Context context, LocalDate date) {
        final Dweller dweller = getDweller(context);
        dweller.setBirthDate(date);
        dwellerRepository.save(dweller);
    }

    private Optional<int[]> parse(String[] parts) {
        final int[] numbers = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            final NumericCheck check = new NumericCheck(part);
            if (!check.isInteger()) {
                return Optional.empty();
            }

            numbers[i] = Integer.parseInt(part);
        }

        return Optional.of(numbers);
    }

    private LocalDate toDate(int[] numbers) {
        return LocalDate.of(numbers[2], numbers[1], numbers[0]);
    }
}
