package com.aiexam.warehouse.support;

import com.aiexam.warehouse.user.User;
import java.lang.reflect.Field;
import java.util.UUID;

public final class TestEntities {

    private TestEntities() {}

    public static User user(String email) {
        User user = User.register(email, "hashed-password", "Tester");
        setId(user, UUID.randomUUID());
        return user;
    }

    public static void setId(Object entity, UUID id) {
        Class<?> type = entity.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalStateException("No id field on " + entity.getClass());
    }
}
