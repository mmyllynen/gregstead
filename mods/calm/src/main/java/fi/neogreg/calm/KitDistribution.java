package fi.neogreg.calm;

import com.google.gson.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.nio.file.*;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Persisted per-request receipts make an offline admin kit a one-time delivery. */
public final class KitDistribution {
    static final String RECEIPT="neogreg_calm:last_kit";
    static Path path(MinecraftServer s){return s.getWorldPath(LevelResource.ROOT).resolve("neogreg-calm/pending-kits.json");}
    static JsonObject read(MinecraftServer s){try{return Files.exists(path(s))?JsonParser.parseString(Files.readString(path(s))).getAsJsonObject():new JsonObject();}catch(Exception e){throw new IllegalStateException("Cannot read calm kit queue",e);}}
    static void write(MinecraftServer s,JsonObject data){try{
        Path f=path(s),tmp=f.resolveSibling(f.getFileName()+".tmp");Files.createDirectories(f.getParent());
        try(var c=FileChannel.open(tmp,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE)){var b=ByteBuffer.wrap((new GsonBuilder().setPrettyPrinting().create().toJson(data)+"\n").getBytes(StandardCharsets.UTF_8));while(b.hasRemaining())c.write(b);c.force(true);}
        Files.move(tmp,f,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
    }catch(Exception e){throw new IllegalStateException("Cannot save calm kit queue",e);}}
    static List<ItemStack> contents(){return List.of(new ItemStack(CalmBlocks.BUTTON_ITEM.get()),new ItemStack(CalmBlocks.CRYSTAL_ITEM.get()),new ItemStack(Items.STONE_BRICKS,32),new ItemStack(Items.STONE_BRICK_SLAB,16),new ItemStack(Items.LANTERN,2));}
    public static void deliver(ServerPlayer p){
        var s=p.level().getServer();var q=read(s);String id=p.getUUID().toString();if(!q.has(id))return;
        String request=q.get(id).getAsString();if(p.getPersistentData().getString(RECEIPT).orElse("").equals(request)){q.remove(id);write(s,q);return;}
        if(!CalmBlocks.admin(p)){p.sendSystemMessage(Component.translatable("message.neogreg_calm.admin_only"));return;}
        var inv=p.getInventory();int free=0;for(int i=0;i<36;i++)if(inv.getItem(i).isEmpty())free++;
        if(free<5){p.sendSystemMessage(Component.translatable("message.neogreg_calm.kit_space"));return;}
        for(var stack:contents())if(!inv.add(stack))throw new IllegalStateException("Reserved kit slots unavailable");
        p.getPersistentData().putString(RECEIPT,request);s.getPlayerList().getPlayerIo().save(p);
        q.remove(id);write(s,q);p.containerMenu.broadcastChanges();p.sendSystemMessage(Component.translatable("message.neogreg_calm.kit_delivered"));
    }
    public static void queue(MinecraftServer s,NameAndId profile){var q=read(s);if(!q.has(profile.id().toString())){q.addProperty(profile.id().toString(),UUID.randomUUID().toString());write(s,q);}var p=s.getPlayerList().getPlayer(profile.id());if(p!=null)deliver(p);}
    static void register(){
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e)->{if(e.getEntity() instanceof ServerPlayer p)deliver(p);});
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e)->{
            e.getDispatcher().register(Commands.literal("neogregcalm").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("kit").executes(c->{var p=c.getSource().getPlayerOrException();queue(c.getSource().getServer(),new NameAndId(p.getGameProfile()));return 1;})
                    .then(Commands.argument("player",StringArgumentType.word()).executes(c->{
                        var s=c.getSource().getServer();String name=StringArgumentType.getString(c,"player");
                        var profile=s.getPlayerList().getOps().getEntries().stream().map(x->x.getUser()).filter(x->x.name().equalsIgnoreCase(name)).findFirst();
                        if(profile.isEmpty()){c.getSource().sendFailure(Component.literal("Unknown admin: "+name));return 0;}
                        queue(s,profile.get());c.getSource().sendSuccess(()->Component.literal("Calm building kit requested for "+name+"; delivered now or on next login."),false);return 1;
                    }))));
        });
    }
}
