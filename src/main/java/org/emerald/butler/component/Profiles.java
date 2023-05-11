package org.emerald.butler.component;

import lombok.RequiredArgsConstructor;
import org.emerald.butler.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Profiles {
    private final Environment environment;

    public boolean isDebug() {
        return Arrays.contains(environment.getActiveProfiles(), "debug");
    }
}