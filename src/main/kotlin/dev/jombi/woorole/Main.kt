package dev.jombi.woorole

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.jdabuilder.cache
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
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

val roleDict = hashMapOf(
    "1183587679103893524" to hashMapOf(
        RoleMap.DJMAX to "1183612034131165266",
        RoleMap.EZ2ON to "1183612035989254144",
        RoleMap.OSU to "1183612037511790622",
        RoleMap.SDVX to "1183612039852216400",
        RoleMap.CHUNITHM to "1183612041936785488",
        RoleMap.PROJECT_SEKAI to "1183612043954225156",
        RoleMap.MAIMAI to "1183612046177218630",
        RoleMap.WACCA to "1183612048299524119",
        RoleMap.JUBEAT to "1183612050883219509",
        RoleMap.DANCERUSH to "1183612052699365556",
        RoleMap.MUSE_DASH to "1183612053953462324",
        RoleMap.ARCAEA to "1183612055958335569",
        RoleMap.PHIGROS to "1183612058424594442",
        RoleMap.SIXTAR to "1183612059930333205",
        RoleMap.PUMP to "1183612063130595338",
        RoleMap.IIDX to "1183612065559097344",
        RoleMap.POP_EN_MUSIC to "1183655937249128488",
        RoleMap.D4DJ to "1183655978302971934",
    ),
    "1081866481274462328" to hashMapOf(
        RoleMap.DJMAX to "1167098817049534485",
        RoleMap.EZ2ON to "1167098913715671111",
        RoleMap.OSU to "1167098973253795920",
        RoleMap.SDVX to "1167099045991432232",
        RoleMap.CHUNITHM to "1167099330927276082",
        RoleMap.PROJECT_SEKAI to "1167099932327550996",
        RoleMap.MAIMAI to "1167100349845348493",
        RoleMap.WACCA to "1167101498434535534",
        RoleMap.JUBEAT to "1167100580460773386",
        RoleMap.DANCERUSH to "1167101682690314260",
        RoleMap.MUSE_DASH to "1167101886722220083",
        RoleMap.ARCAEA to "1167102000199110716",
        RoleMap.PHIGROS to "1167102053651337267",
        RoleMap.SIXTAR to "1167102196098273280",
        RoleMap.PUMP to "1167102325987475587",
        RoleMap.IIDX to "1167101810855657482",
        RoleMap.POP_EN_MUSIC to "1167101810855657482",
        RoleMap.D4DJ to "1167101810855657482",
    )
)

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

    jda.upsertCommand("rolecreate", "리게이 역할들을 만들어보자") {
        isGuildOnly = true
        defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
    }.await()

    jda.onCommand("rolecreate") {cmd ->
        if (roleDict.contains(cmd.guild!!.id))
            cmd.reply("이미있잖아 뭘또만들어").setEphemeral(true).await().let { return@onCommand }
        val n = cmd.deferReply().await()
        RoleMap.entries.associateWith { cmd.guild!!.createRole().setName(it.roleName).setMentionable(false).await().id }
            .forEach { (t, u) ->
                println("RoleMap.${t.name} to \"$u\",")
            }
        n.editOriginal("role set up finished").await()
    }

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
        val n = RoleMap.entries.find { r -> r.name == upper } ?: it.reply("뭔가잘못됐대요: 응애못찾음").await().let { return@listener }
        val role = guild.roles.find { it.id == roleDict[it.guild.id]!![n] } ?: it.reply("뭔가잘못됐대요: 역할못찾음").await().let { return@listener }
        val member = it.member ?: return@listener
        val mRole = member.roles.toMutableList()
        val finalString = if (mRole.contains(role)) mRole.remove(role).let { "삭제" } else mRole.add(role).let { "추가" }
        guild.modifyMemberRoles(member, mRole).await()
        it.editMessage("$finalString: ${n.displayName}").await()
    }
}