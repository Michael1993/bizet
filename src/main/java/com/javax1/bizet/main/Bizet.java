package com.javax1.bizet.main;

import javax.inject.Inject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Bizet {

    public static <T> void inject(final T main) {
        Arrays.stream(main.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> {
                    injectField(main, field);
                });
    }

    private static <T> void injectField(T main, Field field) {
        var type = field.getType();
        field.setAccessible(true);
        if (isMultiple(type))
            injectPluralValues(main, field);
        else
            injectSingleValue(main, field);
    }

    private static <T> void injectSingleValue(T main, Field field) {
        var serviceType = field.getType();
        ServiceLoader<?> loader = loadService(serviceType);
        Object service = loader.findFirst().orElseThrow(() -> new BizetException("No service was loaded"));
        injectValue(main, field, service);
    }

    private static <T> void injectPluralValues(T main, Field field) {
        if (field.getType().isArray()) {
            injectArray(main, field);
        } else {
            injectCollection(main, field);
        }
    }

    private static <T> void injectCollection(T main, Field field) {
        // it should be a collection<service>
        var parameterized = (ParameterizedType) field.getGenericType();
        String typeName = parameterized.getActualTypeArguments()[0].getTypeName();
        Class<?> serviceType;
        try {
            serviceType = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw bizetException(e, "Can not find service %s, but it should have been loaded", typeName);
        }
        ServiceLoader<?> loader = loadService(serviceType);
        Collection<?> serviceCollection = loader.stream()
                .map(Provider::get)
                .collect(Collectors.toCollection(() -> createCollection(field.getType())));
        injectValue(main, field, serviceCollection);
    }

    private static <T> void injectArray(T main, Field field) {
        var serviceType = field.getType().getComponentType();
        ServiceLoader<?> loader = loadService(serviceType);
        Object[] services = loader.stream()
                .map(Provider::get)
                .toArray(i -> (Object[]) Array.newInstance(serviceType, i));
        injectValue(main, field, services);
    }

    private static ServiceLoader<?> loadService(Class<?> serviceType) {
        Bizet.class.getModule().addUses(serviceType);
        return ServiceLoader.load(serviceType);
    }

    private static Collection<Object> createCollection(Class<?> type) {
        try {
            return (Collection<Object>) type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassCastException e) {
            throw bizetException(e, "Can not instantiate service collection.");
        }
    }

    public static boolean isMultiple(Class<?> fieldType) {
        return fieldType.isArray()
                || Collection.class.isAssignableFrom(fieldType);
    }

    private static <T> void injectValue(T main, Field field, Object services) {
        try {
            field.set(main, services);
        } catch (IllegalAccessException e) {
            throw bizetException(e, "Can not inject service %s to field %s", services.getClass(), field);
        }
    }

    private static BizetException bizetException(Exception cause, String message, Object... args) {
        var exception = new BizetException(format(message, args));
        exception.initCause(cause);
        return exception;
    }
}
