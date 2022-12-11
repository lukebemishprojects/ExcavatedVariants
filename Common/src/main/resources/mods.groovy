ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[40,)'

    license = 'LGPL-3.0-or-later'
    issueTrackerUrl = 'https://github.com/lukebemish/excavated_variants/issues'

    mod {
        modId = this.buildProperties['mod_id']
        displayName = this.buildProperties['mod_name']
        version = this.version
        group = this.group
        intermediate_mappings = 'net.fabricmc:intermediary'
        displayUrl = 'https://github.com/lukebemish/excavated_variants'

        description = 'Adds data-defined ore variants for stone/ore combinations missing them'
        authors = [this.buildProperties['mod_author'] as String]

        dependencies {
            minecraft = this.minecraftVersionRange

            forge {
                versionRange = ">=${this.forgeVersion}"
            }

            quiltLoader {
                versionRange = ">=${this.quiltLoaderVersion}"
            }

            mod('dynamic_asset_generator') {
                versionRange = ">=${this.buildProperties['dynassetgen_version']}"
            }
            mod('defaultresources') {
                versionRange = ">=${this.buildProperties['defaultresources_version']}"
            }
        }

        entrypoints {
            init = ["dev.lukebemish.excavatedvariants.quilt.ExcavatedVariantsQuilt"]
            client_init = ["dev.lukebemish.excavatedvariants.quilt.ExcavatedVariantsClientQuilt"]
            rei_init = ["dev.lukebemish.excavatedvariants.compat.rei.ExcavatedVariantsClientPlugin"]
            jei_mod_plugin = ["dev.lukebemish.excavatedvariants.compat.jei.JeiCompat"]
            emi = ["dev.lukebemish.excavatedvariants.quilt.compat.emi.EmiCompat"]
        }
    }
    onQuilt {
        mixin = ['mixin.excavated_variants.json']
    }
}
