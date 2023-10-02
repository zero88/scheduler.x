package io.github.zero88.schedulerx.trigger.repr;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A trigger representation that
 *
 * @since 2.0.0
 */
@FunctionalInterface
public interface TriggerRepresentation {

    /**
     * Get the representation of trigger in default language
     *
     * @return the default representation
     */
    default @NotNull String display() { return display(null); }

    /**
     * Get the representation of trigger depends on the given lang
     *
     * @param lang the language code
     * @return the representation
     * @see Locale#getLanguage()
     * @see Locale#getISO3Language()
     */
    @NotNull String display(@Nullable String lang);

}
