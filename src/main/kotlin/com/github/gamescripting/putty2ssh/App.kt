package com.github.gamescripting.putty2ssh

import java.io.File

const val HEADER_LINES = 4

val profileNameRegex = Regex(".*\\\\(.*)\\]")

fun main(args: Array<String>) {
    val puttyFilePath = args.getOrNull(1) ?: "samples/putty.reg"
    val sshFilePath = args.getOrNull(1) ?: "samples/ssh_config"

    val sshFile = File(sshFilePath)
    sshFile.delete()

    val profiles = File(puttyFilePath).inputStream().use { PuttyRegistryParser(it).parse() }

    for(profile in profiles) {
        var segment = ""

        val hostname = parseHostname(profile.property("HostName") ?: throw Exception("Invalid profile, HostName is required"))
        val port = profile.property("PortNumber") ?: "22"
        val portForwardings = parsePortForwardings(profile.property("PortForwardings"))

        segment += "Host ${profile.name}\n"
        segment += "    HostName ${hostname.host}\n"
        segment += "    Port $port\n"

        if(hostname.user != null) {
            segment += "    User ${hostname.user}\n"
        }

        for(forwarding in portForwardings) {
            val prefix = when(forwarding.type) { ForwardingType.LOCAL -> "Local"; ForwardingType.REMOTE -> "Remote" }
            segment += "    ${prefix}Forward ${forwarding.localPort} ${forwarding.remoteHost}:${forwarding.remotePort}\n"
        }

        segment += "\n"
        sshFile.appendText(segment, Charsets.UTF_8)
    }

}

data class ParsedHostname(val user: String?, val host: String)

fun parseHostname(hostname: String): ParsedHostname {
    return if (!hostname.contains("@")) {
        ParsedHostname(null, hostname)
    } else {
        val splitted = hostname.split("@")
        ParsedHostname(splitted.getOrNull(0), splitted.getOrNull(1) ?: throw Exception("could not parse hostname"))
    }
}

/**
 * Format: "PortForwardings"="L24800=127.0.0.1:24800,R5900=127.0.0.1:5900"
 */
fun parsePortForwardings(forwards: String?): List<PortForwarding> {
    if(forwards == null || forwards.isBlank()) return listOf()

    val splittedByComma= forwards.split(",")
    return splittedByComma.map { entry ->
        val type = when(entry.first()){
            'R' -> ForwardingType.REMOTE
            'L' -> ForwardingType.LOCAL
            else -> throw Exception("Unknown forwarding type")
        }

        val splitedByEquals = entry.split("=")
        val localPort = splitedByEquals.getOrNull(0)?.drop(1) ?: throw Exception("Could not parse local port")
        val remoteHostSplittedByColon = splitedByEquals.getOrNull(1)?.split(":") ?: throw Exception("Could not parse remote spec")

        val remoteHost = remoteHostSplittedByColon.getOrNull(0) ?: throw Exception("Could not parse remote host")
        val remotePort = remoteHostSplittedByColon.getOrNull(1) ?: throw Exception("Could not parse remote port")

        PortForwarding(type,localPort,remoteHost,remotePort)
    }
}

class PortForwarding(
        val type: ForwardingType,
        val localPort: String,
        val remoteHost: String,
        val remotePort: String
)

enum class ForwardingType{
    REMOTE,
    LOCAL
}
