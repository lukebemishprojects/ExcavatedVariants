/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.util.*;

public final class PluginFinder {
    private static final Logger LOGGER = LogManager.getLogger();

    public static <T> List<T> getInstances(Class<?> annotationClass, Class<T> instanceClass) {
        var annotationType = Type.getType(annotationClass);
        Set<String> names = findClassNames(annotationType);
        List<T> instances = new ArrayList<>();
        for (String name : names) {
            try {
                var clazz = Class.forName(name, false, PluginFinder.class.getClassLoader());
                if (!instanceClass.isAssignableFrom(clazz)) {
                    continue;
                }
                var asSubclass = clazz.asSubclass(instanceClass);
                var constructor = asSubclass.getDeclaredConstructor();
                var instance = constructor.newInstance();
                instances.add(instance);
            } catch (ReflectiveOperationException | LinkageError e) {
                LOGGER.error("Failed to load: {}", name, e);
            }
        }
        return instances;
    }

    @NotNull
    private static Set<String> findClassNames(Type annotationType) {
        var scanData = ModList.get().getAllScanData();
        Set<String> names = new LinkedHashSet<>();
        for (ModFileScanData scanDatum : scanData) {
            Iterable<ModFileScanData.AnnotationData> annotations = scanDatum.getAnnotations();
            for (ModFileScanData.AnnotationData a : annotations) {
                if (Objects.equals(a.annotationType(), annotationType)) {
                    String memberName = a.memberName();
                    names.add(memberName);
                }
            }
        }
        return names;
    }
}
