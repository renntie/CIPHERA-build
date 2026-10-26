package com.example.crypto

import java.security.SecureRandom
import kotlin.math.ln
import kotlin.math.roundToInt

object KeyGenEngine {

    private val secureRandom = SecureRandom()

    val EFF_WORDS = listOf(
        "acorn", "acoustics", "acrobat", "acronym", "acting", "action", "active", "activist",
        "albatross", "alchemy", "alder", "alert", "algae", "alibi", "alien", "alignment",
        "amber", "ambush", "amethyst", "amnesty", "amplifier", "amulet", "anchor", "android",
        "aperture", "apex", "aphorism", "apogee", "apostle", "apparatus", "applause", "apricot",
        "aqueduct", "arbiter", "archive", "arctic", "armada", "armory", "aroma", "artifact",
        "asteroid", "astrolabe", "asylum", "atlas", "atom", "atrium", "aurora", "avalanche",
        "axiom", "azimuth", "baboon", "badger", "balcony", "ballad", "bamboo", "banjo",
        "barricade", "basalt", "bastion", "beacon", "behemoth", "bismuth", "blizzard", "bonsai",
        "boulder", "brigade", "bronze", "bunker", "cabaret", "cadence", "caliber", "calypso",
        "camellia", "canopy", "canyon", "capacitor", "caravan", "carbon", "cardinal", "carousel",
        "cascade", "catacombs", "catalyst", "catapult", "cathedral", "cauldron", "cavern", "celestial",
        "centaur", "ceramic", "chalice", "chameleon", "champion", "citadel", "cipher", "cobalt",
        "colossus", "comet", "compass", "conduit", "conifer", "continuum", "copper", "coral",
        "corridor", "cosmos", "covenant", "crater", "crimson", "crucible", "crystal", "cyclone",
        "cylinder", "cypress", "dagger", "dandelion", "chronicle", "decipher", "delta", "diorama",
        "dynamo", "eclipse", "edifice", "elixir", "emerald", "emissary", "emperor", "enclave",
        "enigma", "entropy", "epiphany", "equinox", "estuary", "ether", "evergreen", "excalibur",
        "exodus", "expedition", "falcon", "fathom", "feldspar", "filament", "fjord", "flint",
        "fortress", "fossil", "foundry", "fractal", "frigate", "frontier", "fulcrum", "fusion",
        "galaxy", "galleon", "gambit", "garnet", "garrison", "gazelle", "genesis", "geode",
        "geyser", "glacier", "gladiator", "glimmer", "gondola", "granite", "gravity", "griffin",
        "guardian", "halcyon", "harbinger", "harmony", "harpoon", "hearth", "helium", "helix",
        "hemisphere", "herald", "heritage", "hexagon", "hieroglyph", "horizon", "hurricane", "hydra",
        "hyperion", "iceberg", "ignition", "illusion", "imperial", "impulse", "incense", "indigo",
        "infinity", "ingot", "insignia", "integrity", "isotope", "ivory", "jasper", "juggernaut",
        "junction", "juniper", "kaleidoscope", "kestrel", "keystone", "kinetic", "kingdom",
        "krypton", "labyrinth", "lagoon", "lantern", "lapis", "latitude", "legacy", "leviathan",
        "lexicon", "liberty", "lighthouse", "lithium", "lodestar", "lotus", "luminary", "lynx",
        "maelstrom", "magenta", "magnesium", "magnet", "magnolia", "mahogany", "malachite",
        "mammoth", "mandarin", "manifesto", "mantis", "marathon", "marigold", "mariner",
        "matrix", "mausoleum", "maverick", "medallion", "megalith", "melody", "mercury", "meridian",
        "meteor", "metropolis", "microcosm", "millennium", "mirage", "mithril", "monolith", "monsoon",
        "mosaic", "mystic", "nautilus", "nebula", "nectar", "nemesis", "neodymium", "neptune",
        "neutron", "nexus", "nightingale", "nimbus", "nirvana", "nitrogen", "nocturne", "nomad",
        "nova", "nucleus", "nutrition", "oasis", "obelisk", "obsidian", "octagon", "odyssey",
        "olympus", "omega", "onyx", "opal", "oracle", "orbit", "orchid", "origami", "orion",
        "osmium", "osprey", "outpost", "oxygen", "paladin", "palisade", "palladium", "panacea"
    )

    data class StrengthEvaluation(
        val scorePercent: Int,
        val label: String,
        val bits: Int
    )

    fun evaluateStrength(password: String): StrengthEvaluation {
        if (password.isEmpty()) {
            return StrengthEvaluation(0, "Belum Diisi", 0)
        }
        var pool = 0
        if (password.any { it.isLowerCase() }) pool += 26
        if (password.any { it.isUpperCase() }) pool += 26
        if (password.any { it.isDigit() }) pool += 10
        if (password.any { !it.isLetterOrDigit() }) pool += 32

        val bits = (password.length * (ln(pool.coerceAtLeast(1).toDouble()) / ln(2.0))).roundToInt()
        return when {
            bits < 40 -> StrengthEvaluation(25, "Lemah", bits)
            bits < 65 -> StrengthEvaluation(50, "Sedang", bits)
            bits < 90 -> StrengthEvaluation(75, "Kuat", bits)
            else -> StrengthEvaluation(100, "Kriptografis Sangat Kuat", bits)
        }
    }

    fun generateRandomPassword(
        length: Int = 20,
        useUpper: Boolean = true,
        useLower: Boolean = true,
        useDigits: Boolean = true,
        useSymbols: Boolean = true
    ): String {
        val pool = StringBuilder()
        if (useLower) pool.append("abcdefghijklmnopqrstuvwxyz")
        if (useUpper) pool.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        if (useDigits) pool.append("0123456789")
        if (useSymbols) pool.append("!@#$%^&*()_+-=[]{}|;:,.<>?")
        if (pool.isEmpty()) pool.append("abcdefghijklmnopqrstuvwxyz0123456789")

        val sb = java.lang.StringBuilder()
        for (i in 0 until length) {
            val idx = secureRandom.nextInt(pool.length)
            sb.append(pool[idx])
        }
        return sb.toString()
    }

    fun generatePassphrase(wordCount: Int = 5, delimiter: String = "-"): String {
        val count = wordCount.coerceIn(3, 12)
        val selected = mutableListOf<String>()
        for (i in 0 until count) {
            val idx = secureRandom.nextInt(EFF_WORDS.size)
            selected.add(EFF_WORDS[idx])
        }
        return selected.joinToString(delimiter)
    }
}
