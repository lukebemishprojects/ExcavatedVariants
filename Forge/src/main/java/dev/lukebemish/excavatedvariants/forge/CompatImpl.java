package dev.lukebemish.excavatedvariants.forge;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.ICompat;
import dev.lukebemish.excavatedvariants.api.ExcavatedVariantsListener;
import dev.lukebemish.excavatedvariants.api.IOreListModifier;

import java.util.List;

@AutoService(ICompat.class)
public class CompatImpl implements ICompat {
    public List<IOreListModifier> getOreListModifiers() {
        return PluginFinder.getInstances(ExcavatedVariantsListener.class, IOreListModifier.class);
    }
}
