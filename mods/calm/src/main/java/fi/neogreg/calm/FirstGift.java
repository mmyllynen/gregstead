package fi.neogreg.calm;

import com.google.gson.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import java.nio.file.*;
import java.util.*;

/** World-scoped receipts survive death; player save closes the receipt-write recovery window. */
public final class FirstGift {
    private static final String RECEIPT="neogreg_calm:first_gift";
    private static final Set<UUID> pending=new HashSet<>();
    private static JsonObject receipts;
    private static Path path(ServerPlayer p){return p.level().getServer().getWorldPath(LevelResource.ROOT).resolve("neogreg-calm/first-gifts.json");}
    private static JsonObject receipts(ServerPlayer p){
        if(receipts==null)try{receipts=Files.exists(path(p))?JsonParser.parseString(Files.readString(path(p))).getAsJsonObject():new JsonObject();}
        catch(Exception e){throw new IllegalStateException("Cannot read calm gift receipts",e);}
        return receipts;
    }
    private static void record(ServerPlayer p){
        var data=receipts(p);data.addProperty(p.getUUID().toString(),true);
        try{var f=path(p);Files.createDirectories(f.getParent());var tmp=f.resolveSibling("first-gifts.json.tmp");Files.writeString(tmp,new GsonBuilder().setPrettyPrinting().create().toJson(data)+"\n");Files.move(tmp,f,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        catch(Exception e){throw new IllegalStateException("Cannot save calm gift receipts",e);}
        pending.remove(p.getUUID());
    }
    public static ItemStack book(){
        int radius=NeoGregCalm.settings.radius;String seconds=String.format(Locale.ROOT,"%.1f",NeoGregCalm.settings.provokeDuration/20.0).replaceAll("\\.0$","");
        List<String> pages=List.of(
            "Some stones remember the world before fear.\n\nSet one upon the earth, and its quiet will take root.\n\nBind it in gold and wear it close, and the stillness shall follow your footsteps.",
            "The Rooted Crystal\n\nA G A\nG D G\nA G A\n\nA: Amethyst Shard\nG: Gold Ingot\nD: Diamond\n\nWeave these upon a crafting table. One crystal shall awaken.",
            "The Wandering Light\n\n  G\nG C G\n  G\n\nG: Gold Ingot\nC: Calm Crystal\n\nUpon a crafting table, bind one crystal with four gold ingots. One amulet shall answer.",
            "Place the crystal where peace should take root. Its hush reaches "+radius+" blocks in every direction, even through stone.\n\nBreak it to carry it elsewhere. It rests when its lands fall beyond the waking world.",
            "Wear the amulet in the necklace place of your Curios adornments. Its hush reaches just as far, but follows you.\n\nIn a pocket or a hand, it sleeps. Neither light asks fuel, nor does mingling their circles deepen the peace.",
            "A pale turquoise halo marks a creature held in stillness.\n\nStrike it, and peace yields to anger for "+seconds+" seconds of waking time. Beyond the circle, the old wildness returns.\n\nAll travellers share the shelter.",
            "No vow binds every danger.\n\nThe Dragon, the Wither and the Warden heed no hush. Slimes and magma cubes still wound by touch. A creeper lit by hand still burns.\n\nBeasts may yet fight beasts. Walk gently."
        );
        var stack=new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(Filterable.passThrough("A Whisper of Stillness"),"The Quiet Keeper",0,pages.stream().map(t->Filterable.<Component>passThrough(Component.literal(t))).toList(),true));
        return stack;
    }
    public static ItemStack pouch(){
        var stack=new ItemStack(Items.DYED_BUNDLE.cyan());
        stack.set(DataComponents.CUSTOM_NAME,Component.literal("Calm things"));
        stack.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(List.of(book(),new ItemStack(Items.AMETHYST_SHARD,4),new ItemStack(Items.GOLD_INGOT,8),new ItemStack(Items.DIAMOND)).stream().map(ItemStackTemplate::fromNonEmptyStack).toList()));
        return stack;
    }
    public static void deliver(ServerPlayer p){
        String id=p.getUUID().toString();if(receipts(p).has(id)){pending.remove(p.getUUID());return;}
        if(p.getPersistentData().getBoolean(RECEIPT).orElse(false)){record(p);return;}
        if(!p.isAlive()||p.isSpectator())return;
        if(!p.getInventory().add(pouch()))return;
        p.getPersistentData().putBoolean(RECEIPT,true);
        p.level().getServer().getPlayerList().getPlayerIo().save(p);
        record(p);p.containerMenu.broadcastChanges();
    }
    static void register(){
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e)->{if(e.getEntity() instanceof ServerPlayer p&&!(p instanceof FakePlayer)){pending.add(p.getUUID());deliver(p);}});
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post e)->{if(e.getEntity() instanceof ServerPlayer p&&p.tickCount%20==0&&pending.contains(p.getUUID()))deliver(p);});
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e)->pending.remove(e.getEntity().getUUID()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e)->{pending.clear();receipts=null;});
    }
}
