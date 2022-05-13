package io.github.lukebemish.excavated_variants.client;

import io.github.lukebemish.excavated_variants.platform.Services;

public class ClientServices {
    public static final IRenderTypeHandler RENDER_TYPE_HANDLER = Services.load(IRenderTypeHandler.class);
}
