package dev.jombi.woorole

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.jdabuilder.cache
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import kotlin.io.path.*

val config
    get() = Json.decodeFromString<Config>(Path("config.json").apply {
        if (!exists()) {
            createFile()
            writeText("{\"token\":\"enter the token here\"}")
        }
    }.readText())

suspend fun main() {
    val jda = light(config.token, enableCoroutines = true) {
        intents += listOf(
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MODERATION,
            GatewayIntent.GUILD_PRESENCES
        )
        cache += CacheFlag.getPrivileged()
    }
    jda.upsertCommand("role", "리게이 역할을 받아보자") {
        isGuildOnly = true
    }.await()

    jda.onCommand("role") {
        try {
            val n = List(4) { it + 1 }
            val roleButton = RoleMap.entries.map { Button.of(ButtonStyle.entries[n.random()], it.name.lowercase(), it.displayName) }

            it.reply("받을 역할을 선택하세요").setEphemeral(true)
                .addComponents(ActionRow.partitionOf(roleButton)).await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    jda.listener<ButtonInteractionEvent> {
        val upper = it.componentId.uppercase()
        val guild = it.guild ?: return@listener
        val n = RoleMap.entries.find { r -> r.name == upper } ?: return@listener
        val role = guild.roles.find { it.id == n.id } ?: return@listener
        val member = it.member ?: return@listener
        val mRole = member.roles.toMutableList()
        val finalString = if (mRole.contains(role)) {
            mRole.remove(role)
            "삭제"
        } else {
            mRole.add(role)
            "추가"
        }
        guild.modifyMemberRoles(member, mRole).await()
        it.editMessage("$finalString: ${n.displayName}").await()
    }
}