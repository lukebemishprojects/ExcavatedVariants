package dev.lukebemish.excavatedvariants.client;

import dev.lukebemish.excavatedvariants.platform.Services;

public class ClientServices {
    public static final IRenderTypeHandler RENDER_TYPE_HANDLER = Services.load(IRenderTypeHandler.class);
}
