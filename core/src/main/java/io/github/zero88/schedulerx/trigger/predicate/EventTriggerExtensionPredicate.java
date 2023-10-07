package io.github.zero88.schedulerx.trigger.predicate;

/**
 * Represents for an extension predicate
 *
 * @param <T> Type of event message
 */
public interface EventTriggerExtensionPredicate<T>
    extends EventTriggerPredicate<T>, ExtraPropertiesExtension<EventTriggerExtensionPredicate<T>> {

    interface MessageExtensionConverter<T>
        extends MessageConverter<T>, ExtraPropertiesExtension<MessageExtensionConverter<T>> {

    }


    interface MessageExtensionFilter<T> extends MessageFilter<T>, ExtraPropertiesExtension<MessageExtensionFilter<T>> {

    }

}
