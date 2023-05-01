/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.codecs;

import com.mojang.serialization.DynamicOps;

import java.util.List;

public interface CommentingOps<T> extends DynamicOps<T> {
    void setComment(T input, List<String> path, String comment);
}
