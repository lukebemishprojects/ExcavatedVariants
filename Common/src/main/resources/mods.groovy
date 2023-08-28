/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[40,)'

    license = 'LGPL-3.0-or-later'
    issueTrackerUrl = 'https://github.com/lukebemish/ExcavatedVariants/issues'

    mod {
        modId = this.buildProperties['mod_id']
        displayName = this.buildProperties['mod_name']
        version = this.version
        onQuilt {
            group = this.group
            intermediate_mappings = 'net.fabricmc:intermediary'
        }
        displayUrl = 'https://github.com/lukebemish/ExcavatedVariants'

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
                versionRange = ">=${this.libs.versions.dynassetgen}"
            }
            mod('defaultresources') {
                versionRange = ">=${this.libs.versions.defaultresources}"
            }
        }

        entrypoints {
            init = ['dev.lukebemish.excavatedvariants.impl.quilt.ExcavatedVariantsQuilt']
            client_init = ['dev.lukebemish.excavatedvariants.impl.quilt.ExcavatedVariantsClientQuilt']
            excavated_variants_client = [
                    'dev.lukebemish.excavatedvariants.impl.client.DefaultProvider'
            ]
            excavated_variants = [
                    'dev.lukebemish.excavatedvariants.impl.DefaultRegistrationListener'
            ]
            // to capture fabric loader's funkiness...
            // (have to use this format because MDG is funky)
            entrypoint 'main', ['dev.lukebemish.excavatedvariants.impl.quilt.StateCapturer']
        }
    }
    onQuilt {
        mixin = [
                'mixin.excavated_variants.json',
                'mixin.excavated_variants_quilt.json'
        ]
    }
}
