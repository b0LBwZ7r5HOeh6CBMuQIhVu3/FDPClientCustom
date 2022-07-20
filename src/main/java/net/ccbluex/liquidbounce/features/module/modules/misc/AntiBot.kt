//  updated at : 2022/1/23.
//


package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.extensions.getFullName
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.*
import java.util.*
import kotlin.concurrent.thread

@ModuleInfo(name = "AntiBot", category = ModuleCategory.MISC)
object AntiBot : Module() {

    private val tabValue = BoolValue("Tab", true)
    private val tabModeValue =
        ListValue("TabMode", arrayOf("Equals", "Contains"), "Contains").displayable { tabValue.get() }
    private val entityIDValue = BoolValue("EntityID", true)
    private val colorValue = BoolValue("Color", false)
    private val livingTimeValue = BoolValue("LivingTime", false)
    private val livingTimeTicksValue = IntegerValue("LivingTimeTicks", 40, 1, 200).displayable { livingTimeValue.get() }
    private val groundValue = BoolValue("Ground", true)
    private val airValue = BoolValue("Air", false)
    private val invalidGroundValue = BoolValue("InvalidGround", true)
    private val swingValue = BoolValue("Swing", false)
    private val healthValue = BoolValue("Health", false)
    private val maxHealthValue = FloatValue("MaxHealth", 1f, 1f, 40f).displayable { healthValue.get() }
    private val minHealthValue = FloatValue("MinHealth", 1f, 1f, 40f).displayable { healthValue.get() }
    private val derpValue = BoolValue("Derp", true)
    private val randomHealthValue = BoolValue("randomHealth", true)
    private val randomHealthMaxHealthValue =
        FloatValue("randomHealthMaxHealth", 20f, 1f, 40f).displayable { randomHealthValue.get() }
    private val randomHealthMinHealthValue =
        FloatValue("randomHealthMinHealth", 1f, 1f, 40f).displayable { randomHealthValue.get() }
    private val randomHealthLivingTimeTicksValue =
        IntegerValue("randomHealthLivingTimeTicks", 15, 1, 50).displayable { randomHealthValue.get() }
    private val randomHealthRadiusCheckOnlyXZValue =
        BoolValue("randomHealthRadiusCheckOnlyXZ", false).displayable { randomHealthValue.get() }
    private val randomHealthRadiusValue =
        FloatValue("randomHealthRadiusCheckBlocks", 20f, 5f, 30f).displayable { randomHealthValue.get() }
    private val hasCustomNameValue = BoolValue("hasCustomName", false)
    private val hasCustomNameStrictValue =
        BoolValue("hasCustomNameStrict", true).displayable { hasCustomNameValue.get() }
    private val wasInvisibleValue = BoolValue("WasInvisible", false)
    private val validNameValue = BoolValue("ValidName", true)
    private val validNameRegexValue =
        TextValue("ValidNameRegex", "[\\w|一-龥]{3,16}").displayable { validNameValue.get() }
    private val armorValue = BoolValue("Armor", false)

    // private val invalidArmorValue = BoolValue("invalidArmor", false)
    private val pingValue = BoolValue("Ping", false)
    private val matrixBotValue = BoolValue("MatrixBot", false)
    private val matrixBotStrictValue = BoolValue("MatrixBotStict", false).displayable { matrixBotValue.get() }
    private val needHitValue = BoolValue("NeedHit", false)
    private val lowWidthValue = BoolValue("LowWidth", true)
    private val neverMoveValue = BoolValue("NeverMove", false)
    private val neverRotationValue = BoolValue("neverRotation", false)

    // private val spawnInCombatValue = BoolValue("SpawnInCombat", false)
    private val noClipValue = BoolValue("NoClip", false)
    private val reusedEntityIdValue = BoolValue("ReusedEntityId", false)

    private val duplicateInWorldValue = BoolValue("DuplicateInWorld", false)
    private val duplicateInTabValue = BoolValue("DuplicateInTab", false)
    private val duplicateCompareModeValue = ListValue(
        "DuplicateCompareMode",
        arrayOf("OnTime", "WhenSpawn"),
        "OnTime"
    ).displayable { duplicateInTabValue.get() || duplicateInWorldValue.get() }
    private val fastDamageValue = BoolValue("FastDamage", false)
    private val fastDamageTicksValue = IntegerValue("FastDamageTicks", 5, 1, 20).displayable { fastDamageValue.get() }

    // private val hypixelSWBotValue = BoolValue("HypixelSWBot", false)
    private val alwaysInRadiusValue = BoolValue("AlwaysInRadius", false)
    private val alwaysInRadiusOnlyXZValue = BoolValue("AlwaysInRadiusOnlyXZ", false)
    private val alwaysRadiusValue =
        FloatValue("AlwaysInRadiusBlocks", 20f, 5f, 30f).displayable { alwaysInRadiusValue.get() }
    private val spawnInRadiusValue = BoolValue("SpawnInRadius", false).displayable { alwaysInRadiusValue.get() }
    private val alwaysInRadiusWithTicksCheckValue = BoolValue(
        "AlwaysInRadiusWithTicksCheck",
        false
    ).displayable { alwaysInRadiusValue.get() && livingTimeValue.get() }
    private val alwaysInRadiusInCombatingValue =
        BoolValue("AlwaysInRadiusOnlyCombating", false).displayable { alwaysInRadiusValue.get() }
    private val hideBotValue = BoolValue("HideBot", false)
    private val hytRespawnValue = BoolValue("hytRespawn", false)

    private val ground = mutableListOf<Int>()
    private val air = mutableListOf<Int>()
    private val invalidGround = mutableMapOf<Int, Int>()
    private val swing = mutableListOf<Int>()
    private val invisible = mutableListOf<Int>()
    private val hitted = mutableListOf<Int>()
    private val spawnInCombat = mutableListOf<Int>()
    private val notAlwaysInRadius = mutableListOf<Int>()
    private val alwaysInRadius = mutableListOf<Int>()
    private val noHitDelay = mutableListOf<Int>()
    private val moved = mutableListOf<Int>()
    private val randomHealth = mutableListOf<Int>()
    private val turnHead = mutableListOf<Int>()
    private val hasHitDelay = mutableListOf<Int>()
    private val lastDamage = mutableMapOf<Int, Int>()
    private val lastDamageVl = mutableMapOf<Int, Float>()
    private val duplicate = mutableListOf<UUID>()
    private val noClip = mutableListOf<Int>()
    private val hasRemovedEntities = mutableListOf<Int>()
    private val hytNoAttack = mutableListOf<String>()
    private val regex = Regex("\\w{3,16}")

    fun isBot(entity: EntityLivingBase): Boolean {
        // Check if entity is a player
        if (entity !is EntityPlayer) {
            return false
        }

        // Check if anti bot is enabled
        if (!state) {
            return false
        }

        // Anti Bot checks
        if (colorValue.get() && !entity.displayName.formattedText.replace("§r", "").contains("§")) {
            return true
        }

        if (livingTimeValue.get() && entity.ticksExisted < livingTimeTicksValue.get()) {
            return true
        }

        if (groundValue.get() && !ground.contains(entity.entityId)) {
            return true
        }

        if (airValue.get() && !air.contains(entity.entityId)) {
            return true
        }

        if (swingValue.get() && !swing.contains(entity.entityId)) {
            return true
        }
        if (healthValue.get() && (entity.health > maxHealthValue.get() || entity.health <= minHealthValue.get())) {
            return true
        }

        if (alwaysInRadiusInCombatingValue.get() && spawnInCombat.contains(entity.entityId)) {
            return true
        }

        if (lowWidthValue.get() && entity.width < 0.5F) {
            return true
        }

        if (noClipValue.get() && noClip.contains(entity.entityId)) {
            return true
        }

        if (reusedEntityIdValue.get() && hasRemovedEntities.contains(entity.entityId)) {
            return false
        }
        if (randomHealthValue.get() && randomHealth.contains(entity.entityId)) {
            return true
        }

        if (hasCustomNameValue.get() && entity.hasCustomName() && (!hasCustomNameStrictValue.get() || entity.getCustomNameTag()
                .contains(entity.getName()))
        ) {
            return true
        }
        if(hytRespawnValue.get() && hytNoAttack.contains(entity.getName())){
            return true
        }
        if (entityIDValue.get() && (entity.entityId >= 1000000000 || entity.entityId <= -1)) {
            return true
        }

        if (derpValue.get() && (entity.rotationPitch > 90F || entity.rotationPitch < -90F)) {
            return true
        }

        if (wasInvisibleValue.get() && invisible.contains(entity.entityId)) {
            return true
        }

        if (validNameValue.get() && !entity.name.matches(Regex(validNameRegexValue.get()))) {
            return true
        }

        if (armorValue.get()) {
            if (entity.inventory.armorInventory[0] == null && entity.inventory.armorInventory[1] == null &&
                entity.inventory.armorInventory[2] == null && entity.inventory.armorInventory[3] == null
            ) {
                return true
            }
        }
        /*        if (invalidArmorValue.get()) {
                    for (it in entity.inventory.armorInventory){
                        if(it != null && it !is ItemArmor){
                            return true
                        }
                    }
                }*/
        if (pingValue.get()) {
            if (mc.netHandler.getPlayerInfo(entity.uniqueID)?.responseTime == 0) {
                return true
            }
        }

        if (needHitValue.get() && !hitted.contains(entity.entityId)) {
            return true
        }

        if (invalidGroundValue.get() && invalidGround.getOrDefault(entity.entityId, 0) >= 10) {
            return true
        }

        if (tabValue.get()) {
            val equals = tabModeValue.equals("Equals")
            val targetName = stripColor(entity.displayName.formattedText)

            for (networkPlayerInfo in mc.netHandler.playerInfoMap) {
                val networkName = stripColor(networkPlayerInfo.getFullName())

                if (if (equals) targetName == networkName else targetName.contains(networkName)) {
                    return false
                }
            }

            return true
        }

        if (duplicateCompareModeValue.equals("WhenSpawn") && duplicate.contains(entity.gameProfile.id)) {
            return true
        }

        if (duplicateInWorldValue.get() && duplicateCompareModeValue.equals("OnTime") && mc.theWorld.loadedEntityList.count { it is EntityPlayer && it.name == it.name } > 1) {
            return true
        }

        if (duplicateInTabValue.get() && duplicateCompareModeValue.equals("OnTime") && mc.netHandler.playerInfoMap.count { entity.name == it.gameProfile.name } > 1) {
            return true
        }

        if (fastDamageValue.get() && lastDamageVl.getOrDefault(entity.entityId, 0f) > 0) {
            return true
        }

        if (alwaysInRadiusValue.get() && !notAlwaysInRadius.contains(entity.entityId) || alwaysInRadius.contains(entity.entityId)) {
            return true
        }

        if (matrixBotValue.get() && noHitDelay.contains(entity.entityId)) {
            return true
        }
        if (matrixBotValue.get() && matrixBotStrictValue.get() && !hasHitDelay.contains(entity.entityId)) {
            return true
        }
        if (neverMoveValue.get() && !moved.contains(entity.entityId)) {
            return true
        }
        if (neverRotationValue.get() && !turnHead.contains(entity.entityId)) {
            return true
        }
        return entity.name.isEmpty() || entity.name == mc.thePlayer.name
    }

    override fun onDisable() {
        clearAll()
        super.onDisable()
    }

    private fun processEntityMove(entity: Entity, onGround: Boolean) {
        if (entity is EntityPlayer) {
            if (onGround && !ground.contains(entity.entityId)) {
                ground.add(entity.entityId)
            }
            if (!onGround && !air.contains(entity.entityId)) {
                air.add(entity.entityId)
            }

            if (onGround) {
                if (entity.prevPosY != entity.posY) {
                    invalidGround[entity.entityId] = invalidGround.getOrDefault(entity.entityId, 0) + 1
                }
            } else {
                val currentVL = invalidGround.getOrDefault(entity.entityId, 0) / 2
                if (currentVL <= 0) {
                    invalidGround.remove(entity.entityId)
                } else {
                    invalidGround[entity.entityId] = currentVL
                }
            }

            if (entity.isInvisible && !invisible.contains(entity.entityId)) {
                invisible.add(entity.entityId)
            }

            if (!noClip.contains(entity.entityId)) {
                val cb = mc.theWorld.getCollidingBoundingBoxes(
                    entity,
                    entity.entityBoundingBox.contract(0.0625, 0.0625, 0.0625)
                )
//                alert("NOCLIP[${cb.size}] ${entity.displayName.unformattedText} ${entity.posX} ${entity.posY} ${entity.posZ}")
                if (cb.isNotEmpty()) {
                    noClip.add(entity.entityId)
                }
                val dist = mc.thePlayer.getDistance(
                    entity.posX,
                    if (alwaysInRadiusOnlyXZValue.get()) mc.thePlayer.posY else entity.posY,
                    entity.posZ
                )
                val dist2 = mc.thePlayer.getDistance(
                    entity.posX,
                    if (randomHealthRadiusCheckOnlyXZValue.get()) mc.thePlayer.posY else entity.posY,
                    entity.posZ
                )
                if ((!livingTimeValue.get() || entity.ticksExisted > livingTimeTicksValue.get() || !alwaysInRadiusWithTicksCheckValue.get()) && !notAlwaysInRadius.contains(
                        entity.entityId
                    ) && dist > alwaysRadiusValue.get()
                ) {
                    notAlwaysInRadius.add(entity.entityId)
                }
                if (!notAlwaysInRadius.contains(entity.entityId) && dist < alwaysRadiusValue.get() && alwaysInRadiusValue.get() && spawnInRadiusValue.get() && (LiquidBounce.combatManager.inCombat || !alwaysInRadiusInCombatingValue.get())) {
                    alwaysInRadius.add(entity.entityId)
                }
                if (!noHitDelay.contains(entity.entityId) && entity.hurtResistantTime < 1 && entity.hurtTime > 9) {
                    noHitDelay.add(entity.entityId)
                }
                if (!hasHitDelay.contains(entity.entityId) && entity.hurtResistantTime > 0 && entity.hurtTime > 9) {
                    hasHitDelay.add(entity.entityId)
                }
                if (!moved.contains(entity.entityId) && (entity.lastTickPosY != entity.posY || entity.lastTickPosX != entity.posX || entity.lastTickPosZ != entity.posZ)) {
                    moved.add(entity.entityId)
                }
                if (!turnHead.contains(entity.entityId) && (entity.prevRotationYaw != entity.rotationYaw || entity.prevRotationPitch != entity.rotationPitch)) {
                    turnHead.add(entity.entityId)
                }
                if (!randomHealth.contains(entity.entityId) && entity.ticksExisted < randomHealthLivingTimeTicksValue.get() && (entity.health <= maxHealthValue.get() && entity.health >= minHealthValue.get()) && dist2 < randomHealthRadiusValue.get()) {
                    randomHealth.add(entity.entityId)
                }
            }

            if ((!livingTimeValue.get() || entity.ticksExisted > livingTimeTicksValue.get() || !alwaysInRadiusWithTicksCheckValue.get()) && !notAlwaysInRadius.contains(
                    entity.entityId
                ) && mc.thePlayer.getDistanceToEntity(entity) > alwaysRadiusValue.get()
            ) {
                notAlwaysInRadius.add(entity.entityId)
                // if (alwaysInRadiusRemoveValue.get()) {
                //     mc.theWorld.removeEntity(entity)

                // }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val packet = event.packet
        if (packet is S02PacketChat) {
            val chat = packet.chatComponent.unformattedText
            if(hytRespawnValue.get() && !chat.contains(":")){
                if(chat.contains("起床战争") && chat.contains("杀死了")){
                        val hytKilledRegex = """杀死了\S*\(""".toRegex()
                        val noSpaceChat = chat.replace(" ","")
                        val player = hytKilledRegex.find(noSpaceChat)!!.value.replace("杀死了","").replace("(","").replace(")","")
                        hytNoAttack.add(player)
                        thread(start = true) {
                            try {
                                Thread.sleep(6000)
                                hytNoAttack.remove(player)
                            } catch (ignored: InterruptedException) {
                            }
                        }
                }
            }
        } else if (packet is S18PacketEntityTeleport) {
            processEntityMove(mc.theWorld.getEntityByID(packet.entityId) ?: return, packet.onGround)
        } else if (packet is S14PacketEntity) {
            processEntityMove(packet.getEntity(mc.theWorld) ?: return, packet.onGround)
        } else if (packet is S0BPacketAnimation) {
            val entity = mc.theWorld.getEntityByID(packet.entityID)

            if (entity != null && entity is EntityLivingBase && packet.animationType == 0 &&
                !swing.contains(entity.entityId)
            ) {
                swing.add(entity.entityId)
            }
        } else if (packet is S38PacketPlayerListItem) {
            if (duplicateCompareModeValue.equals("WhenSpawn") && packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                packet.entries.forEach { entry ->
                    val name = entry.profile.name
                    if (duplicateInWorldValue.get() && mc.theWorld.playerEntities.any { it.name == name } ||
                        duplicateInTabValue.get() && mc.netHandler.playerInfoMap.any { it.gameProfile.name == name }) {
                        duplicate.add(entry.profile.id)
                    }
                }
            }
        } else if (packet is S0CPacketSpawnPlayer) {
            if (LiquidBounce.combatManager.inCombat && !hasRemovedEntities.contains(packet.entityID)) {

                spawnInCombat.add(packet.entityID)
            }
        } else if (packet is S13PacketDestroyEntities) {
            hasRemovedEntities.addAll(packet.entityIDs.toTypedArray())

        }

        if (packet is S19PacketEntityStatus && packet.opCode.toInt() == 2 || packet is S0BPacketAnimation && packet.animationType == 1) {
            val entity = if (packet is S19PacketEntityStatus) {
                packet.getEntity(mc.theWorld)
            } else if (packet is S0BPacketAnimation) {
                mc.theWorld.getEntityByID(packet.entityID)
            } else {
                null
            } ?: return

            if (entity is EntityPlayer) {
                lastDamageVl[entity.entityId] =
                    lastDamageVl.getOrDefault(entity.entityId, 0f) + if (entity.ticksExisted - lastDamage.getOrDefault(
                            entity.entityId,
                            0
                        ) <= fastDamageTicksValue.get()
                    ) {
                        1f
                    } else {
                        -0.5f
                    }
                lastDamage[entity.entityId] = entity.ticksExisted
            }
        }
    }

    @EventTarget
    fun onAttack(e: AttackEvent) {
        val entity = e.targetEntity

        if (entity is EntityLivingBase && !hitted.contains(entity.entityId)) {
            hitted.add(entity.entityId)
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        clearAll()
    }

    /*    fun isInTabList(player: EntityPlayer): Boolean {
            return mc.ingameGUI.getTabList().getList()
                .contains(mc.getNetHandler().getPlayerInfo(player.getGameProfile().getId()))
        }*/

    private fun clearAll() {
        hitted.clear()
        swing.clear()
        ground.clear()
        invalidGround.clear()
        invisible.clear()
        lastDamage.clear()
        lastDamageVl.clear()
        notAlwaysInRadius.clear()
        alwaysInRadius.clear()
        noHitDelay.clear()
        hasHitDelay.clear()
        moved.clear()
        turnHead.clear()
        duplicate.clear()
        spawnInCombat.clear()
        noClip.clear()
        hasRemovedEntities.clear()
    }
}
