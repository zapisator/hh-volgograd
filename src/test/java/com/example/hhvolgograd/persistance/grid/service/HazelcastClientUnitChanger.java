package com.example.hhvolgograd.persistance.grid.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

public class HazelcastClientUnitChanger {

    public void onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit timeUnit, Class<?> aClass)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = aClass.getDeclaredField("unit");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, timeUnit);
    }
}
