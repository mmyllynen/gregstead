package calmregression;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import fi.neogreg.calm.CalmAreas;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

/** Test-only mod: real AI ticks in a fresh, loopback-only dedicated server. Never shipped. */
@Mod("neogreg_calm_regression")
public final class CalmCombatRegression {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    final Path work = Path.of(System.getProperty("calm.regression.work"));
    final String mode = System.getProperty("calm.regression.mode", "fixed");
    final List<String> checks = new ArrayList<>();
    final List<JsonObject> cases = new ArrayList<>();
    final List<Zombie> zombies = new ArrayList<>();
    final List<Villager> villagers = new ArrayList<>();
    ServerLevel level;
    MinecraftServer server;
    ServerPlayer player;
    JsonObject fixture;
    JsonObject sample;
    int ticks, scene, age;
    int playerAttempts, canceledPlayerAttempts, villagerAttempts, villagerTargetTicks, playerTargetTicks, noTargetTicks;
    float damage;
    boolean finished;
    boolean waitingCrystal;
    boolean waitingClear;
    final String[] scenarios = System.getProperty("calm.regression.scenarios", "replica_amulet,replica_no_player,replica_raise_roof_no_player,open_amulet,open_no_player,open_unprotected_player").split(",");

    public CalmCombatRegression() {
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, this::target);
    }
    void check(boolean ok, String name) {
        if (!ok) throw new IllegalStateException(name);
        checks.add(name);
        System.out.println("CALM_REGRESSION_CHECK " + name);
    }
    void target(LivingChangeTargetEvent e) {
        if (!zombies.contains(e.getEntity())) return;
        if (e.getOriginalAboutToBeSetTarget() instanceof Player) {
            playerAttempts++;
            if (e.isCanceled()) canceledPlayerAttempts++;
        } else if (e.getOriginalAboutToBeSetTarget() instanceof Villager) villagerAttempts++;
    }
    Item amulet() { return BuiltInRegistries.ITEM.getValue(Identifier.parse("neogreg_calm:calm_amulet")); }
    void equip(boolean enabled) {
        var inv = CuriosApi.getCuriosInventory(player).orElseThrow();
        inv.loadDatapacks();
        var necklace = inv.getStacksHandler("necklace").orElseThrow();
        necklace.getStacks().setStackInSlot(0, enabled ? new ItemStack(amulet()) : ItemStack.EMPTY);
        check(inv.isSlotActive("necklace", 0), "active_necklace_" + scene);
    }
    void cleanup() {
        zombies.forEach(Entity::discard); villagers.forEach(Entity::discard);
        zombies.clear(); villagers.clear();
        if (player != null) { level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED); player = null; }
        // Only this disposable world's small test arena is cleared.
        for (var p : BlockPos.betweenClosed(-4, 99, -4, 24, 129, 25)) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
        for (var p : BlockPos.betweenClosed(-4, 99, -4, 24, 99, 25)) level.setBlock(p, Blocks.STONE.defaultBlockState(), 2);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    static BlockState property(BlockState state, String key, String value) {
        Property p = state.getBlock().getStateDefinition().getProperty(key);
        if (p == null) throw new IllegalArgumentException("Missing block property " + key);
        return (BlockState)p.getValue(value).map(v -> state.setValue(p, (Comparable)v)).orElseThrow();
    }
    void replica() {
        for (var element : fixture.getAsJsonArray("blocks")) {
            var b = element.getAsJsonObject(); var pos = b.getAsJsonArray("pos");
            var block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(b.get("id").getAsString()));
            var state = block.defaultBlockState();
            for (var entry : b.getAsJsonObject("properties").entrySet()) state = property(state, entry.getKey(), entry.getValue().getAsString());
            level.setBlock(new BlockPos(pos.get(0).getAsInt(), 100 + pos.get(1).getAsInt(), pos.get(2).getAsInt()), state, 2);
        }
        for (var element : fixture.getAsJsonArray("entities")) {
            var e = element.getAsJsonObject(); var pos = e.getAsJsonArray("pos");
            Mob mob = e.get("id").getAsString().equals("minecraft:zombie") ? zombie() : villager();
            mob.setPos(pos.get(0).getAsDouble(), 100 + pos.get(1).getAsDouble(), pos.get(2).getAsDouble());
            mob.setCustomName(Component.literal(e.get("name").getAsString().isEmpty() ? "Synthetic villager" : e.get("name").getAsString()));
            check(!e.get("noAI").getAsBoolean(), "replica_saved_ai_enabled_" + mob.getUUID());
            level.addFreshEntity(mob);
        }
    }
    Zombie zombie() {
        var z = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        Objects.requireNonNull(z).setPersistenceRequired();
        z.getRandom().setSeed(0);
        z.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        zombies.add(z); return z;
    }
    Villager villager() {
        var v = EntityTypes.VILLAGER.create(level, EntitySpawnReason.COMMAND);
        Objects.requireNonNull(v).getRandom().setSeed(0);
        villagers.add(v); return v;
    }
    void setupScene() {
        cleanup(); age = 0; damage = 0;
        playerAttempts = canceledPlayerAttempts = villagerAttempts = villagerTargetTicks = playerTargetTicks = noTargetTicks = 0;
        String name = scenarios[scene];
        boolean isReplica = name.startsWith("replica");
        if (isReplica) {
            replica();
            if(name.equals("replica_raise_roof_no_player"))
                for(int z=0;z<19;z++){var pos=new BlockPos(3,104,z);var state=level.getBlockState(pos);level.setBlock(pos.above(),state,2);level.removeBlock(pos,false);}
        }
        else {
            var z = zombie(); z.setPos(3.5, 100, 10.5); level.addFreshEntity(z);
            var v = villager(); v.setNoAi(true); v.setPos(5, 100, 10.5); level.addFreshEntity(v);
        }
        if (!name.endsWith("no_player")) {
            player = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes(("CalmObserver"+scene).getBytes()), "CalmObserver"+scene));
            player.setGameMode(GameType.SURVIVAL); player.getAbilities().invulnerable = false; player.setPermanentlyInvulnerable(false); player.setInvulnerableTime(0);
            if (isReplica) { var p=fixture.getAsJsonArray("player"); player.setPos(p.get(0).getAsDouble(),100+p.get(1).getAsDouble(),p.get(2).getAsDouble()); }
            else player.setPos(8,100,10.5);
            level.addNewPlayer(player); player.connection.markClientLoaded(); equip(name.endsWith("amulet"));
            check(level.players().contains(player), "player_registered_"+name);
            check(player.canBeSeenAsEnemy(), "survival_target_valid_"+name);
        }
        sample = new JsonObject(); sample.addProperty("scenario", name);
        if (player != null) {
            sample.addProperty("calmArea", CalmAreas.contains(zombies.getFirst()));
            sample.addProperty("playerVisible", zombies.getFirst().getSensing().hasLineOfSight(player));
            sample.addProperty("canAttackPlayer", zombies.getFirst().canAttack(player));
        }
        var rays=new JsonArray();
        for(var z:zombies){
            var v=villagers.stream().min(Comparator.comparingDouble(z::distanceToSqr)).orElseThrow();
            var hit=level.clip(new ClipContext(z.getEyePosition(),v.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,z));
            var ray=new JsonObject();ray.addProperty("zombie",z.getName().getString());ray.addProperty("visible",z.getSensing().hasLineOfSight(v));ray.addProperty("hitType",hit.getType().toString());
            if(hit.getType()!=HitResult.Type.MISS){ray.addProperty("block",BuiltInRegistries.BLOCK.getKey(level.getBlockState(hit.getBlockPos()).getBlock()).toString());ray.addProperty("position",hit.getBlockPos().toShortString());}
            rays.add(ray);
        }
        sample.add("initialVillagerSight",rays);
        System.out.println("CALM_REGRESSION_SCENE " + name);
    }
    void observe() {
        for (var z : zombies) {
            if (z.getTarget() instanceof Villager) villagerTargetTicks++;
            else if (z.getTarget() instanceof Player) playerTargetTicks++;
            else noTargetTicks++;
        }
        for (var v : villagers) {
            if (v.getHealth() < 20) { damage += 20-v.getHealth(); v.setHealth(20); }
        }
        if (player != null) { if(player.getHealth() < 20){player.setHealth(20);player.deathTime=0;} player.setInvulnerableTime(0); }
    }
    void finishScene() {
        sample.addProperty("playerAttempts",playerAttempts);sample.addProperty("canceledPlayerAttempts",canceledPlayerAttempts);
        sample.addProperty("villagerAttempts",villagerAttempts);sample.addProperty("villagerTargetTicks",villagerTargetTicks);
        sample.addProperty("playerTargetTicks",playerTargetTicks);sample.addProperty("noTargetTicks",noTargetTicks);sample.addProperty("villagerDamage",damage);
        var actors=new JsonArray();
        for(var z:zombies){var a=new JsonObject();a.addProperty("alive",z.isAlive());a.addProperty("ticks",z.tickCount);a.addProperty("noAI",z.isNoAi());a.addProperty("effectiveAI",z.isEffectiveAi());a.addProperty("position",z.position().toString());a.addProperty("range",z.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE));actors.add(a);}
        sample.add("zombies",actors);
        var z0=zombies.getFirst();var candidates=level.getEntitiesOfClass(net.minecraft.world.entity.npc.villager.AbstractVillager.class,z0.getBoundingBox().inflate(35));
        sample.addProperty("candidateCount",candidates.size());
        sample.addProperty("freshVillagerGoalCanUse",new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(z0,net.minecraft.world.entity.npc.villager.AbstractVillager.class,0,false,false,null).canUse());
        if(!villagers.isEmpty()){sample.addProperty("canAttackVillager",z0.canAttack(villagers.getFirst()));sample.addProperty("villagerCombatCondition",TargetingConditions.forCombat().ignoreLineOfSight().test(level,z0,villagers.getFirst()));}

        var targets=new JsonArray();for(var v:villagers){var a=new JsonObject();a.addProperty("alive",v.isAlive());a.addProperty("valid",v.canBeSeenAsEnemy());a.addProperty("position",v.position().toString());a.addProperty("ticks",v.tickCount);targets.add(a);}sample.add("villagers",targets);
        if(scenarios[scene].equals("open_unprotected_player"))check(playerTargetTicks>0,"unprotected_player_selected");
        cases.add(sample); System.out.println("CALM_REGRESSION_OBSERVATION " + sample);
        if (mode.equals("fixed") && scenarios[scene].endsWith("amulet")) {
            check(!sample.get("canAttackPlayer").getAsBoolean(),"protected_candidate_rejected_"+scenarios[scene]);
            check(playerAttempts==0,"no_player_goal_preemption_"+scenarios[scene]);
            if(scenarios[scene].equals("open_amulet")) check(villagerTargetTicks>0,"villager_selected_"+scenarios[scene]);
        }
        if (scenarios[scene].equals("replica_raise_roof_no_player")) check(villagerTargetTicks>0,"replica_raised_roof_restores_target");
        if (scenarios[scene].equals("open_no_player") || (mode.equals("fixed") && scenarios[scene].equals("open_amulet"))) check(damage>0,"villager_actually_hit_"+scenarios[scene]);
    }
    void policyChecks() {
        cleanup();
        var z=zombie();z.setPos(0.5,100,0.5);level.addFreshEntity(z);
        player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"PolicyObserver"));player.setGameMode(GameType.SURVIVAL);player.setPermanentlyInvulnerable(false);player.setInvulnerableTime(0);player.setPos(1.5,100,0.5);level.addNewPlayer(player);player.connection.markClientLoaded();equip(true);
        var v=villager();v.setPos(2,100,0.5);level.addFreshEntity(v);
        check(CalmAreas.contains(z),"amulet_area_active");
        boolean eligible=z.canAttack(player);
        if(mode.equals("fixed"))check(!eligible,"close_player_rejected_below_two_blocks");
        sample=new JsonObject();sample.addProperty("baselineCandidateAccepted",eligible);
        check(TargetingConditions.forNonCombat().test(level,z,player),"noncombat_visibility_unchanged");
        z.setTarget(player);check(z.getTarget()==null,"late_target_guard_retained");
        check(net.neoforged.neoforge.common.CommonHooks.onLivingChangeTarget(z,player,LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET).isCanceled(),"brain_target_guard_retained");
        z.setTarget(v);check(z.getTarget()==v,"villager_target_untouched");z.setTarget(null);
        player.setPos(32.4,100,0.5);check(CalmAreas.contains(z),"sphere_inside_31_9");
        player.setPos(32.6,100,0.5);check(!CalmAreas.contains(z)&&z.canAttack(player),"sphere_outside_32_1");
        player.setPos(1.5,100,0.5);equip(false);check(!CalmAreas.contains(z)&&z.canAttack(player),"unequip_restores_vanilla");
        z.setTarget(player);check(z.getTargetUnchecked()==player,"unprotected_target_primed");
        equip(true);waitingClear=true;age=0;
    }
    void finishPolicyChecks() {
        var z=zombies.getFirst();
        check(CalmAreas.contains(z),"crystal_area_active");
        if(mode.equals("fixed"))check(!z.canAttack(player),"crystal_candidate_rejected");
        equip(true);level.removeBlock(new BlockPos(0,100,2),false);check(CalmAreas.contains(z),"overlap_remaining_source");
        equip(false);check(!CalmAreas.contains(z)&&z.canAttack(player),"last_source_removal_immediate");equip(true);
        float health=player.getHealth();player.setInvulnerableTime(0);player.hurtServer(level,level.damageSources().mobAttack(z),2);check(player.getHealth()==health,"incoming_player_damage_blocked");
        var slime=EntityTypes.SLIME.create(level,EntitySpawnReason.COMMAND);Objects.requireNonNull(slime).setPos(0.5,100,0.5);player.setInvulnerableTime(0);player.hurtServer(level,level.damageSources().mobAttack(slime),2);check(player.getHealth()<health,"slime_contact_exception_retained");player.setHealth(20);player.setInvulnerableTime(0);
        for(var type:List.of(EntityTypes.WARDEN,EntityTypes.WITHER,EntityTypes.ENDER_DRAGON)){
            var boss=(Mob)type.create(level,EntitySpawnReason.COMMAND);Objects.requireNonNull(boss).setPos(0.5,100,0.5);boolean protectedEligibility=boss.canAttack(player);boss.setTarget(player);boolean protectedTarget=boss.getTargetUnchecked()==player;
            equip(false);boss.setTarget(null);boolean vanillaEligibility=boss.canAttack(player);boss.setTarget(player);
            check(protectedEligibility==vanillaEligibility&&protectedTarget==(boss.getTargetUnchecked()==player),"excluded_boss_"+type.toShortString());equip(true);
        }
        z.setTarget(null);z.setInvulnerableTime(0);z.hurtServer(level,level.damageSources().playerAttack(player),1);
        check(z.getPersistentData().getInt("neogreg_calm:remaining_ticks").orElse(0)==400,"actual_player_damage_grants_400_ticks");
        check(z.canAttack(player),"retaliation_candidate_allowed");z.setTarget(player);check(z.getTarget()==player,"retaliation_target_allowed");
        age=0;
    }
    void complete(boolean ok, Throwable failure) {
        finished=true;
        try {
            var result=new JsonObject();result.addProperty("passed",ok);result.addProperty("mode",mode);
            result.add("checks",GSON.toJsonTree(checks));result.add("scenarios",GSON.toJsonTree(cases));
            if(sample!=null)result.add("policy",sample);
            if(failure!=null)result.addProperty("failure",failure.toString());
            Files.writeString(work.resolve("report.json"),GSON.toJson(result)+"\n");
        } catch(Exception e){e.printStackTrace();}
        if(failure!=null)failure.printStackTrace();
        System.out.println("CALM_REGRESSION_"+(ok?"PASSED":"FAILED"));server.halt(false);
    }
    void tick(ServerTickEvent.Post e) {
        if(finished)return;
        server=e.getServer();level=server.overworld();
        try {
            if(++ticks==20){
                fixture=JsonParser.parseString(Files.readString(Path.of(System.getProperty("calm.regression.fixture")))).getAsJsonObject();
                level.getGameRules().set(GameRules.SPAWN_MOBS,false,server);server.setDifficulty(Difficulty.NORMAL,true);
                for(int x=-1;x<=1;x++)for(int z=-1;z<=2;z++)level.setChunkForced(x,z,true);
                setupScene();return;
            }
            if(ticks<20)return;
            if(scene<scenarios.length){observe();if(++age>=400){finishScene();if(++scene<scenarios.length)setupScene();else if(Boolean.getBoolean("calm.regression.observeOnly"))complete(true,null);else policyChecks();}return;}
            if(player!=null){if(player.getHealth()<20)player.setHealth(20);player.setInvulnerableTime(0);}
            if(waitingClear){if(++age>=2){
                waitingClear=false;check(zombies.getFirst().getTargetUnchecked()==null,"equipping_clears_stored_player_target");
                equip(false);level.setBlock(new BlockPos(0,100,2),BuiltInRegistries.BLOCK.getValue(Identifier.parse("neogreg_calm:calm_crystal")).defaultBlockState(),3);
                waitingCrystal=true;age=0;
            }return;}
            if(waitingCrystal){if(++age>=2){waitingCrystal=false;finishPolicyChecks();}return;}
            if(++age==50)check(zombies.getFirst().canAttack(player),"retaliation_still_active");
            if(age==410){
                var z=zombies.getFirst();check(z.getPersistentData().getInt("neogreg_calm:remaining_ticks").orElse(0)==0,"retaliation_expires_with_loaded_ticks");
                check(CalmAreas.contains(z)&&player.canBeSeenAsEnemy(),"expiry_still_has_protected_living_player");
                if(mode.equals("fixed"))check(!z.canAttack(player),"expiry_rejects_player_again");
                check(!(z.getTarget() instanceof Player),"expiry_clears_player_target");
                // Removing the protected player must not leave permanent AI suppression.
                level.removePlayerImmediately(player,Entity.RemovalReason.DISCARDED);player=null;
                check(z.canAttack(villagers.getFirst()),"player_departure_keeps_villager_eligible");
                complete(true,null);
            }
        }catch(Throwable t){complete(false,t);}
    }
}
