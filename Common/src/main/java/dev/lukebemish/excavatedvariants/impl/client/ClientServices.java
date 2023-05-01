/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import dev.lukebemish.excavatedvariants.impl.platform.Services;

public class ClientServices {
    public static final RenderTypeHandler RENDER_TYPE_HANDLER = Services.load(RenderTypeHandler.class);
}
