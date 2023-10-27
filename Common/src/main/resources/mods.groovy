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

            onForge {
                forge {
                    versionRange = ">=${this.forgeVersion}"
                }
            }

            onQuilt {
                quiltLoader {
                    versionRange = ">=${this.libs.versions.quilt.loader}"
                }
                mod('quilted_fabric_api') {
                    versionRange = ">=${this.libs.versions.qfapi}"
                }
            }

            onFabric {
                fabricLoader {
                    versionRange = ">=${this.libs.versions.fabric.loader}"
                }
                mod('fabric-api') {
                    versionRange = ">=${this.libs.versions.fapi}"
                }
            }

            mod('dynamic_asset_generator') {
                versionRange = ">=${this.libs.versions.dynassetgen}"
            }
            mod('defaultresources') {
                versionRange = ">=${this.libs.versions.defaultresources}"
            }
        }

        entrypoints {
            onQuilt {
                init = ['dev.lukebemish.excavatedvariants.impl.fabriquilt.quilt.ExcavatedVariantsQuilt']
                // to capture fabric loader's funkiness...
                // (have to use this format because MDG is funky)
                entrypoint 'main', ['dev.lukebemish.excavatedvariants.impl.fabriquilt.StateCapturer']
            }
            onFabric {
                main = [
                        'dev.lukebemish.excavatedvariants.impl.fabriquilt.fabric.ExcavatedVariantsFabric',
                        'dev.lukebemish.excavatedvariants.impl.fabriquilt.StateCapturer'
                ]
            }
            entrypoint 'client', ['dev.lukebemish.excavatedvariants.impl.fabriquilt.ExcavatedVariantsClientFabriQuilt']
        }
    }
    onQuilt {
        mixin = [
                'mixin.excavated_variants.json',
                'mixin.excavated_variants_fabriquilt.json'
        ]
    }
    onFabric {
        mixin = [
                'mixin.excavated_variants.json',
                'mixin.excavated_variants_fabriquilt.json',
                'mixin.excavated_variants_fabric.json'
        ]
    }
}
