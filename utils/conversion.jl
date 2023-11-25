using JSON3

const RUNTIME = joinpath(pwd(), "run")
if !isdir(RUNTIME)
    mkdir(RUNTIME)
end

const OUTDIR = joinpath(RUNTIME, "out")
if !isdir(OUTDIR)
    mkdir(OUTDIR)
end

const VANILLA_ORES = Set(["iron", "gold", "coal", "emerald", "diamond", "redstone", "copper", "lapis"])

function convertconfig(name)
    originalfile = joinpath(RUNTIME, name*".json")
    json = JSON3.read(originalfile)
    name = json[:mod_id]
    neworedir = joinpath(OUTDIR, "ore", name)
    newstonedir = joinpath(OUTDIR, "stone", name)
    if isdir(neworedir)
        rm(neworedir; recursive = true)
    end
    if isdir(newstonedir)
        rm(newstonedir; recursive = true)
    end
    mkpath(neworedir)
    mkpath(newstonedir)
    if haskey(json, :provided_stones)
        for stone ∈ json[:provided_stones]
            id = stone[:id]
            if startswith(id, name*"_")
                id = id[length(name)+2:end]
            else
                id = id
            end
            block = stone[:block_id]
            types = []
            for type ∈ stone[:types]
                if type == "stone"
                    push!(types, "excavated_variants:overworld")
                else
                    push!(types, "excavated_variants:"*type)
                end
            end
            translations = Dict()
            for (key, value) ∈ stone[:lang]
                translations[key] = replace(value, '$' => "%s")
            end
            out = Dict()
            out["types"] = types
            out["block"] = block
            out["ore_tags"] = [
                "forge:ores_in_ground/$id"
            ]
            out["translations"] = translations
            open(joinpath(newstonedir, id*".json"), "w") do f
                JSON3.pretty(f, out, JSON3.AlignmentContext(indent = 2))
            end
        end
    end
    if haskey(json, :provided_ores)
        for ore ∈ json[:provided_ores]
            id = ore[:id]
            numstones = length(ore[:stone])
            blockmap = Dict()
            for i ∈ 1:numstones
                stone = ore[:stone][i]
                if startswith(stone, name*"_")
                    stone = "excavated_variants:$(name)/"*stone[length(name)+2:end]
                else
                    stone = "excavated_variants:minecraft/"*stone
                end
                blockmap[ore[:block_id][i]] = stone
            end
            translations = Dict()
            for (key, value) ∈ ore[:lang]
                translations[key] = replace(value, '$' => "%s")
            end
            types = []
            for type ∈ ore[:types]
                if type == "stone"
                    push!(types, "excavated_variants:overworld")
                else
                    push!(types, "excavated_variants:"*type)
                end
            end
            tagsraw = []
            if haskey(ore, :orename)
                if ore[:orename] isa AbstractString
                    processedname = replace(ore[:orename], "_ore" => "")
                    push!(tagsraw, processedname)
                else
                    for name ∈ ore[:orename]
                        processedname = replace(name, "_ore" => "")
                        push!(tagsraw, processedname)
                    end
                end
            else
                push!(tagsraw, replace(id, "_ore" => ""))
            end
            tags = []
            for tag ∈ tagsraw
                push!(tags, "forge:ores/$tag")
                push!(tags, "c:ores/$tag")
                push!(tags, "c:$(tag)_ores")
                if tag in VANILLA_ORES
                    push!(tags, "minecraft:$(tag)_ores")
                end
            end
            out = Dict()
            out["types"] = types
            out["blocks"] = blockmap
            out["tags"] = tags
            out["translations"] = translations
            open(joinpath(neworedir, id*".json"), "w") do f
                JSON3.pretty(f, out, JSON3.AlignmentContext(indent = 2))
            end
        end
    end
end

# requires `json5` command
function json5tojson()
    files = filter(readdir(RUNTIME)) do file
        endswith(file, ".json5")
    end
    for file in files
        cmd = `json5 $(joinpath(RUNTIME, file)) -o $(joinpath(RUNTIME, file[1:end-1]))`
        run(cmd)
    end
end