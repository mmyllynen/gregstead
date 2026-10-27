package fi.neogreg.calm;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.*;
import java.util.Set;

public final class CalmBlocks {
    static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(Registries.BLOCK,"neogreg_calm");
    static final DeferredRegister<Item> ITEMS=DeferredRegister.create(Registries.ITEM,"neogreg_calm");
    static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"neogreg_calm");
    public static final DeferredHolder<Block,CrystalBlock> CRYSTAL=BLOCKS.register("calm_crystal",()->new CrystalBlock(properties("calm_crystal").strength(1.5f).noOcclusion().lightLevel(s->7)));
    public static final DeferredHolder<Block,DistributionButton> BUTTON=BLOCKS.register("calm_dispenser_button",()->new DistributionButton(properties("calm_dispenser_button").strength(2,3600000).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)));
    public static final DeferredHolder<Item,BlockItem> CRYSTAL_ITEM=ITEMS.register("calm_crystal",()->new BlockItem(CRYSTAL.get(),itemProperties("calm_crystal")));
    public static final DeferredHolder<Item,Item> AMULET=ITEMS.register("calm_amulet",()->new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id("calm_amulet"))).stacksTo(1).component(net.minecraft.core.component.DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(java.util.List.of(Component.translatable("item.neogreg_calm.calm_amulet.desc"))))));
    public static final DeferredHolder<Item,BlockItem> BUTTON_ITEM=ITEMS.register("calm_dispenser_button",()->new AdminBlockItem(BUTTON.get(),itemProperties("calm_dispenser_button")));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<CrystalEntity>> CRYSTAL_ENTITY=ENTITIES.register("calm_crystal",()->new BlockEntityType<>(CrystalEntity::new,Set.of(CRYSTAL.get())));
    static Identifier id(String n){return Identifier.fromNamespaceAndPath("neogreg_calm",n);}
    static BlockBehaviour.Properties properties(String n){return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,id(n)));}
    static Item.Properties itemProperties(String n){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id(n))).useBlockDescriptionPrefix();}
    public static boolean admin(Player p){return p!=null&&p.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);}
    static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.block.BreakBlockEvent e)->{if(e.getState().is(BUTTON.get())&&!admin(e.getPlayer()))e.setCanceled(true);});}
    public static class CrystalEntity extends BlockEntity {
        public CrystalEntity(BlockPos p,BlockState s){super(CRYSTAL_ENTITY.get(),p,s);}
        @Override public void onLoad(){super.onLoad();if(level instanceof ServerLevel l)CalmAreas.add(l,worldPosition);}
        @Override public void onChunkUnloaded(){if(level instanceof ServerLevel l)CalmAreas.remove(l,worldPosition);super.onChunkUnloaded();}
        @Override public void setRemoved(){if(level instanceof ServerLevel l)CalmAreas.remove(l,worldPosition);super.setRemoved();}
    }
    public static class CrystalBlock extends BaseEntityBlock {
        static final VoxelShape SHAPE=Shapes.or(Block.box(2,0,2,14,3,14),Block.box(5,3,5,11,15,11));
        public CrystalBlock(Properties p){super(p);}
        @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new CrystalEntity(p,s);}
        @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
        @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
        @Override public void animateTick(BlockState s,Level l,BlockPos p,RandomSource r){if(r.nextInt(4)==0)l.addParticle(new DustParticleOptions(0x50E8D5,0.55f),p.getX()+0.25+r.nextDouble()*0.5,p.getY()+0.7+r.nextDouble()*0.4,p.getZ()+0.25+r.nextDouble()*0.5,0,0.015,0);}
        @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide())player.sendSystemMessage(Component.translatable("message.neogreg_calm.radius",NeoGregCalm.settings.radius));return InteractionResult.SUCCESS;}
    }
    public static class AdminBlockItem extends BlockItem {
        public AdminBlockItem(Block b,Item.Properties p){super(b,p);}
        @Override public InteractionResult place(BlockPlaceContext c){if(!admin(c.getPlayer())){if(c.getPlayer()!=null&&!c.getLevel().isClientSide())c.getPlayer().sendSystemMessage(Component.translatable("message.neogreg_calm.admin_only"));return InteractionResult.FAIL;}return super.place(c);}
    }
    public static class DistributionButton extends Block {
        public static final BooleanProperty PRESSED=BlockStateProperties.POWERED;
        public DistributionButton(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(PRESSED,false));}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(PRESSED);}
        @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(2,0,2,14,s.getValue(PRESSED)?3:5,14);}
        @Override protected float getDestroyProgress(BlockState s,Player player,BlockGetter l,BlockPos p){return admin(player)?super.getDestroyProgress(s,player,l,p):0;}
        @Override protected void onExplosionHit(BlockState s,ServerLevel l,BlockPos p,Explosion e,java.util.function.BiConsumer<ItemStack,BlockPos> out){}
        @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
            if(l.isClientSide())return InteractionResult.SUCCESS;
            if(!s.getValue(PRESSED)&&player instanceof ServerPlayer sp&&grant(sp)){
                l.setBlock(p,s.setValue(PRESSED,true),3);l.scheduleTick(p,this,10);
                l.playSound(null,p,SoundEvents.STONE_BUTTON_CLICK_ON,SoundSource.BLOCKS,0.5f,1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        public static boolean grant(ServerPlayer p){var stack=new ItemStack(CRYSTAL_ITEM.get());if(!p.getInventory().add(stack)){p.sendOverlayMessage(Component.translatable("message.neogreg_calm.inventory_full"));return false;}p.containerMenu.broadcastChanges();return true;}
        @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){l.setBlock(p,s.setValue(PRESSED,false),3);}
    }
}
