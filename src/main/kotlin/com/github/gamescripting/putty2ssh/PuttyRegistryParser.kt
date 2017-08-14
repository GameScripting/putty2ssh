package com.github.gamescripting.putty2ssh

import java.io.File
import java.io.InputStream

class PuttyRegistryParser(
        val inputStream: InputStream
) {
    fun parse() : List<PuttyProfile> {
        val puttyLines = inputStream.reader(Charsets.UTF_16LE).readLines()

        val profiles = mutableListOf<PuttyProfile>()

        var activeProfile: PuttyProfile? = null
        for(line in puttyLines.drop(HEADER_LINES)) {
            if(line.isBlank()) continue

            val newProfile = profileNameRegex.matchEntire(line)
            if(newProfile != null) {
                val profileName = newProfile.groups.get(1)?.value ?: ""
                activeProfile = PuttyProfile(profileName)
                profiles.add(activeProfile)
                continue
            }

            if(activeProfile == null) throw Exception("Cannot parse property without an active profile")

            val property = parseProperty(line)
            activeProfile.properties.add(property)
        }

        return profiles
    }

    private fun parseProperty(line: String): PuttyProperty {
        val splitted = line.split("=", limit = 2)
        val key = splitted.getOrNull(0)?.trim('"') ?: throw Exception("Cannot have a property without a key")
        var value = splitted.getOrNull(1) ?: throw Exception("Cannot have a property without a value")

        val isText = value.startsWith("\"")
        val type = if(isText) {
            value = value.trim('"')
            Type.text
        } else {
            val splittedValue = value.split(":")
            value = splittedValue.getOrNull(1)?.trim('"') ?: throw Exception("Unknown value")
            val type = Type.valueOf(splittedValue.getOrNull(0) ?: throw Exception("Unknown type"))

            when(type) {
                Type.dword -> {
                    value = if(value == "ffffffff") Int.MAX_VALUE.toString() else value.toInt(16).toString()
                }
            }

            type
        }

        return PuttyProperty(
                key,
                type,
                value
        )
    }
}


class PuttyProfile(
        val name: String,
        val properties: MutableList<PuttyProperty> = mutableListOf()
) {
    fun property(name: String): String? {
        return properties.firstOrNull { it.name == name }?.value
    }
}

class PuttyProperty(
        val name: String,
        val type: Type,
        val value: String
)

enum class Type {
    text,
    dword
}