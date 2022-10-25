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
            init = ["io.github.lukebemish.excavated_variants.quilt.ExcavatedVariantsQuilt"]
            client_init = ["io.github.lukebemish.excavated_variants.quilt.ExcavatedVariantsClientQuilt"]
            rei_init = ["io.github.lukebemish.excavated_variants.compat.rei.ExcavatedVariantsClientPlugin"]
            jei_mod_plugin = ["io.github.lukebemish.excavated_variants.compat.jei.JeiCompat"]
            emi = ["io.github.lukebemish.excavated_variants.quilt.compat.emi.EmiCompat"]
        }
    }
    onQuilt {
        access_widener = 'excavated_variants.accessWidener'
        mixin = ['mixin.excavated_variants.json',
                 'mixin.excavated_variants_quilt.json']
    }
}
