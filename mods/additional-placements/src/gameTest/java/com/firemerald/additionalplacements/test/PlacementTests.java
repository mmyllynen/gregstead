package com.firemerald.additionalplacements.test;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.*;
import com.firemerald.additionalplacements.block.interfaces.*;
import com.firemerald.additionalplacements.block.stairs.common.CommonStairShapeState;
import com.firemerald.additionalplacements.generation.*;
import com.firemerald.additionalplacements.network.APBuffer;
import com.firemerald.additionalplacements.util.MessageTree;
import io.netty.buffer.Unpooled;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(AdditionalPlacementsMod.MOD_ID)
public final class PlacementTests {
    private static final Map<String,Consumer<GameTestHelper>> TESTS=new LinkedHashMap<>();
    static {
        TESTS.put("generated_registry",PlacementTests::registry);
        TESTS.put("effortless_build_undo_redo",PlacementTests::effortless);
        TESTS.put("vertical_stair_connections",PlacementTests::connections);
        TESTS.put("copper_wax_and_oxidation",PlacementTests::copper);
        TESTS.put("all_generated_default_shapes",PlacementTests::allShapes);
        TESTS.put("all_vanilla_stair_orientations",PlacementTests::stairs);
        TESTS.put("blockstate_save_roundtrip",PlacementTests::roundtrip);
        TESTS.put("vanilla_drops_preserved",PlacementTests::drops);
        TESTS.put("waterlogged_vertical_slab",PlacementTests::waterlogged);
        TESTS.put("rotations_and_mirrors",PlacementTests::rotations);
        TESTS.put("network_collection_roundtrip",PlacementTests::network);
        TESTS.put("network_rejects_invalid_size",PlacementTests::invalidNetwork);
        TESTS.put("vertical_slab_item_placement",PlacementTests::slabPlacement);
        for(Direction direction:Direction.values()) {
            TESTS.put("slab_"+direction.getSerializedName(),h -> slab(h,direction));
            if(direction!=Direction.DOWN) {
                TESTS.put("carpet_"+direction.getSerializedName(),h -> carpet(h,direction));
                TESTS.put("pressure_plate_"+direction.getSerializedName(),h -> pressure(h,direction,false));
                TESTS.put("weighted_plate_"+direction.getSerializedName(),h -> pressure(h,direction,true));
            }
        }
    }
    public PlacementTests(IEventBus bus) {
        var functions=DeferredRegister.<Consumer<GameTestHelper>>create(Registries.TEST_FUNCTION,AdditionalPlacementsMod.MOD_ID);
        TESTS.forEach((name,function)->functions.register(name,()->function));functions.register(bus);
        bus.addListener((RegisterGameTestsEvent event)->{
            var environment=event.registerEnvironment(AdditionalPlacementsMod.rl("test_environment"));
            TESTS.forEach((name,function)->event.registerTest(AdditionalPlacementsMod.rl(name),
                data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION,AdditionalPlacementsMod.rl(name)),data),
                new TestData<>(environment,AdditionalPlacementsMod.rl("empty"),300,0,true)));
        });
    }
    static void check(GameTestHelper h,boolean condition,String message){h.assertTrue(condition,message);}
    static Block vanilla(String id){return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(id));}
    static AdditionalPlacementBlock<?> variant(Block parent){
        return Registration.created().filter(e->e.originalBlock()==parent).map(e->e.newBlock()).findFirst().orElseThrow();
    }
    static void effortless(GameTestHelper h) {
        if(!net.neoforged.fml.ModList.get().isLoaded("effortlessbuilding")) {h.succeed();return;}
        try {
            var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"APPlacementTest"));player.setGameMode(GameType.SURVIVAL);
            BlockPos relative=new BlockPos(5,3,5),target=h.absolutePos(relative);h.setBlock(relative.north(),Blocks.STONE);
            player.setPos(target.getX()+0.5,target.getY(),target.getZ()+2);player.setYRot(180);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.OAK_SLAB,8));
            String prefix="nl.requios.effortlessbuilding.";
            Class<?> mode=Class.forName(prefix+"buildmode.BuildModeEnum"), action=Class.forName(prefix+"buildmode.ModeOptions$ActionEnum"),
                replace=Class.forName(prefix+"buildmode.BuildSettings$ReplaceMode"),packet=Class.forName(prefix+"network.PlaceBuildModePacket");
            Object full=action.getField("FULL").get(null);
            Object placement=packet.getConstructors()[0].newInstance(mode.getField("SINGLE").get(null),target,target,target,
                Direction.SOUTH,Vec3.atCenterOf(target.north()).add(0,0,0.5),full,full,full,full,replace.getField("ONLY_AIR").get(null),true);
            Class.forName(prefix+"network.PacketHandler").getMethod("handlePlaceBuildMode",packet,net.minecraft.server.level.ServerPlayer.class).invoke(null,placement,player);
            BlockState placed=h.getBlockState(relative);
            check(h,placed.getBlock() instanceof VerticalSlabBlock,"Effortless Building must place original slab item vertically");
            check(h,player.getInventory().countItem(Items.OAK_SLAB)==7,"Effortless Building must consume exactly one slab");
            Class<?> undo=Class.forName(prefix+"utilities.UndoManager");
            check(h,(int)undo.getMethod("undo",net.minecraft.server.level.ServerPlayer.class).invoke(null,player)==1&&h.getBlockState(relative).isAir(),"Effortless undo must remove placed variant");
            check(h,player.getInventory().countItem(Items.OAK_SLAB)==8,"Effortless undo must refund exactly one original item");
            check(h,(int)undo.getMethod("redo",net.minecraft.server.level.ServerPlayer.class).invoke(null,player)==1&&h.getBlockState(relative)==placed,"Effortless redo must retain the vertical orientation");
            check(h,player.getInventory().countItem(Items.OAK_SLAB)==7,"Effortless redo must consume exactly one item");
            undo.getMethod("clearPlayer",java.util.UUID.class).invoke(null,player.getUUID());
            System.out.println("AP_TEST_EFFORTLESS_PASS: vertical placement, material consumption, undo/refund and redo/orientation");
            h.succeed();
        } catch(ReflectiveOperationException e) {throw new IllegalStateException("Effortless Building integration failed",e);}
    }
    static void connections(GameTestHelper h) {
        var stairs=(IStairBlock<?>)Blocks.OAK_STAIRS;
        BlockPos center=h.absolutePos(new BlockPos(7,3,7));int connected=0,vertical=0;
        for(var facing:com.firemerald.additionalplacements.util.ComplexFacing.values()) {
            BlockState state=stairs.additionalplacements$getBlockState(CommonStairShapeState.of(facing,
                com.firemerald.additionalplacements.block.stairs.common.CommonStairShape.STRAIGHT),Blocks.OAK_STAIRS.defaultBlockState());
            h.getLevel().setBlock(center,state,Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
            for(Direction side:Direction.values()) {
                BlockPos neighbour=center.relative(side);
                for(var otherFacing:com.firemerald.additionalplacements.util.ComplexFacing.values()) {
                    BlockState other=stairs.additionalplacements$getBlockState(CommonStairShapeState.of(otherFacing,
                        com.firemerald.additionalplacements.block.stairs.common.CommonStairShape.STRAIGHT),Blocks.OAK_STAIRS.defaultBlockState());
                    h.getLevel().setBlock(neighbour,other,Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
                    var shape=stairs.additionalplacements$getShape(facing,h.getLevel(),center);
                    if(shape!=com.firemerald.additionalplacements.block.stairs.common.CommonStairShape.STRAIGHT) {
                        connected++;if(shape.isVertical)vertical++;
                    }
                }
                h.getLevel().setBlock(neighbour,Blocks.AIR.defaultBlockState(),Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
            }
        }
        check(h,connected>0&&vertical>0,"Both ordinary and vertical stair connections must work");
        System.out.println("AP_TEST_CONNECTIONS total="+connected+" vertical="+vertical);h.succeed();
    }
    static void copper(GameTestHelper h) {
        BlockState state=variant(vanilla("cut_copper_slab")).defaultBlockState().setValue(VerticalSlabBlock.AXIS,Direction.Axis.X);
        BlockState waxed=HoneycombItem.getWaxed(state).orElseThrow();
        check(h,waxed.getBlock()==variant(vanilla("waxed_cut_copper_slab")),"Waxing must retain generated copper slab");
        check(h,waxed.getValue(VerticalSlabBlock.AXIS)==Direction.Axis.X,"Waxing must retain vertical axis");
        BlockState oxidized=WeatheringCopper.getNext(state.getBlock()).orElseThrow().withPropertiesOf(state);
        check(h,oxidized.getBlock()==variant(vanilla("exposed_cut_copper_slab")),"Oxidation must retain generated copper slab");
        check(h,oxidized.getValue(VerticalSlabBlock.AXIS)==Direction.Axis.X,"Oxidation must retain vertical axis");
        check(h,WeatheringCopper.getPrevious(oxidized).orElseThrow()==state,"Scraping must restore original placement");
        check(h,net.neoforged.neoforge.common.DataMapHooks.getBlockUnwaxed(waxed.getBlock())==state.getBlock(),"Unwaxing must retain generated variant");
        h.succeed();
    }
    static void registry(GameTestHelper h){
        var entries=Registration.created().toList();check(h,entries.size()>100,"Vanilla variants must register");
        Map<String,Integer> counts=new TreeMap<>();
        for(var e:entries){
            check(h,BuiltInRegistries.BLOCK.getKey(e.newBlock()).equals(e.newId()),"Generated block registry ID mismatch");
            check(h,e.newBlock().asItem()==e.originalBlock().asItem(),"Variants must reuse the original item");
            check(h,e.newBlock().additionalplacements$getOtherBlock()==e.originalBlock(),"Parent block mismatch");
            counts.merge(e.originalId().getNamespace(),1,Integer::sum);
        }
        System.out.println("AP_TEST_GENERATED_REGISTRY "+counts+" TOTAL="+entries.size());h.succeed();
    }
    static void shape(GameTestHelper h,BlockState state){
        VoxelShape shape=state.getShape(h.getLevel(),h.absolutePos(new BlockPos(5,2,5)));
        check(h,!shape.isEmpty(),"Empty shape: "+state);
        AABB box=shape.bounds();check(h,box.getXsize()>0&&box.getYsize()>0&&box.getZsize()>0,"Degenerate shape: "+state);
        check(h,state.getCollisionShape(h.getLevel(),h.absolutePos(new BlockPos(5,2,5)))!=null,"Missing collision shape");
    }
    static void allShapes(GameTestHelper h){for(var e:Registration.created().toList())shape(h,e.newBlock().defaultBlockState());h.succeed();}
    static void stairs(GameTestHelper h){
        var parent=Blocks.OAK_STAIRS;var stair=(IStairBlock<?>)parent;Set<BlockState> states=new HashSet<>();
        for(var orientation:CommonStairShapeState.values()){
            BlockState state=stair.additionalplacements$getBlockState(orientation,parent.defaultBlockState());
            shape(h,state);states.add(state);
        }
        check(h,states.size()>=108,"All stair orientations must remain available; got "+states.size());
        System.out.println("AP_TEST_STAIR_STATES "+states.size());h.succeed();
    }
    static void roundtrip(GameTestHelper h){
        for(var e:Registration.created().toList()){
            BlockState state=e.newBlock().defaultBlockState();
            var encoded=BlockState.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow();
            check(h,BlockState.CODEC.parse(NbtOps.INSTANCE,encoded).getOrThrow()==state,"Generated block state did not survive NBT roundtrip");
        }h.succeed();
    }
    static void drops(GameTestHelper h){
        var slab=variant(Blocks.OAK_SLAB);var pos=new BlockPos(5,2,5);h.setBlock(pos,slab.defaultBlockState());
        h.getLevel().destroyBlock(h.absolutePos(pos),true);
        h.runAtTickTime(4,()->{
            int count=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(Vec3.atLowerCornerOf(h.absolutePos(pos.offset(-1,-1,-1))),Vec3.atLowerCornerOf(h.absolutePos(pos.offset(2,2,2)))))
                .stream().filter(e->e.getItem().is(Items.OAK_SLAB)).mapToInt(e->e.getItem().getCount()).sum();
            check(h,count==1,"Vertical slab must drop exactly one original slab, got "+count);h.succeed();
        });
    }
    static void waterlogged(GameTestHelper h){
        BlockState state=variant(Blocks.OAK_SLAB).defaultBlockState().setValue(BlockStateProperties.WATERLOGGED,true);
        h.setBlock(new BlockPos(5,2,5),state);check(h,state.getFluidState().isSource(),"Waterlogged slab must contain source water");h.succeed();
    }
    static void rotations(GameTestHelper h){
        for(var e:Registration.created().toList()){
            BlockState original=e.newBlock().defaultBlockState(),state=original;
            for(int i=0;i<4;i++)state=state.rotate(Rotation.CLOCKWISE_90);
            check(h,state==original,"Four rotations must restore original state: "+original);
            check(h,original.mirror(Mirror.FRONT_BACK).mirror(Mirror.FRONT_BACK)==original,"Double mirror must restore original state");
        }h.succeed();
    }
    static void network(GameTestHelper h){
        FriendlyByteBuf buffer=new FriendlyByteBuf(Unpooled.buffer());
        try{
            var tree=new MessageTree(Component.literal("root"),List.of(new MessageTree(Component.literal("child"))));
            tree.write(buffer);var read=new MessageTree(buffer);check(h,read.message.getString().equals("root")&&read.children.size()==1,"Message tree roundtrip failed");
            Map<Identifier,Integer> map=Map.of(AdditionalPlacementsMod.rl("a"),27);
            APBuffer.writeMap(buffer,map,FriendlyByteBuf::writeIdentifier,(b,v)->b.writeVarInt(v));
            check(h,APBuffer.readMap(buffer,FriendlyByteBuf::readIdentifier,FriendlyByteBuf::readVarInt).equals(map),"Map wire format failed");
        }finally{buffer.release();}h.succeed();
    }
    static void invalidNetwork(GameTestHelper h){
        for(int size:List.of(-1,65537)){
            FriendlyByteBuf buffer=new FriendlyByteBuf(Unpooled.buffer());buffer.writeVarInt(size);boolean rejected=false;
            try{APBuffer.readList(buffer,FriendlyByteBuf::readVarInt);}catch(IllegalArgumentException expected){rejected=true;}finally{buffer.release();}
            check(h,rejected,"Malformed collection size must be rejected");
        }h.succeed();
    }
    static void slab(GameTestHelper h,Direction direction){
        BlockState state=((ISlabBlock<?>)Blocks.OAK_SLAB).additionalplacements$forPlacing(direction,Blocks.OAK_SLAB.defaultBlockState());
        shape(h,state);AABB box=state.getShape(h.getLevel(),h.absolutePos(new BlockPos(5,2,5))).bounds();
        double thickness=switch(direction.getAxis()){case X->box.getXsize();case Y->box.getYsize();case Z->box.getZsize();};
        check(h,Math.abs(thickness-0.5)<0.001,"Slab must occupy half a block along placement axis");h.succeed();
    }
    static void carpet(GameTestHelper h,Direction direction){
        var block=variant(vanilla("red_carpet"));BlockPos pos=new BlockPos(5,2,5);
        BlockState state=block.defaultBlockState().setValue(AdditionalFloorBlock.PLACING,direction);
        h.setBlock(pos.relative(direction),Blocks.STONE);h.setBlock(pos,state);
        check(h,state.canSurvive(h.getLevel(),h.absolutePos(pos)),"Wall/ceiling carpet must survive on support");shape(h,state);
        h.setBlock(pos.relative(direction),Blocks.AIR);
        h.runAtTickTime(3,()->{check(h,h.getBlockState(pos).isAir(),"Unsupported carpet must detach");h.succeed();});
    }
    static void pressure(GameTestHelper h,Direction direction,boolean weighted){
        var block=(AdditionalBasePressurePlateBlock<?>)variant(weighted?Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE:Blocks.OAK_PRESSURE_PLATE);
        BlockPos pos=new BlockPos(5,2,5),absolute=h.absolutePos(pos);h.setBlock(pos.relative(direction),Blocks.STONE);
        BlockState state=block.defaultBlockState().setValue(AdditionalFloorBlock.PLACING,direction);h.setBlock(pos,state);
        check(h,state.canSurvive(h.getLevel(),absolute),"Pressure plate must attach to support");
        AABB detection=AdditionalBasePressurePlateBlock.TOUCH_AABBS[direction.ordinal()-1].move(absolute);
        Vec3 center=detection.getCenter();ItemEntity item=new ItemEntity(h.getLevel(),center.x,center.y-0.05,center.z,new ItemStack(Items.STICK));item.setNoGravity(true);h.getLevel().addFreshEntity(item);
        block.entityInside(state,h.getLevel(),absolute,item,InsideBlockEffectApplier.NOOP,true);
        BlockState pressed=h.getBlockState(pos);
        check(h,block.plateMethods.additionalplacements$getSignalForStatePublic(pressed)>0,"Wall/ceiling pressure plate must activate");
        check(h,pressed.getSignal(h.getLevel(),absolute,Direction.UP)>0,"Pressed plate must emit redstone");
        item.discard();h.runAtTickTime(30,()->{
            check(h,block.plateMethods.additionalplacements$getSignalForStatePublic(h.getBlockState(pos))==0,"Plate must release after entity leaves");h.succeed();
        });
    }
    static void slabPlacement(GameTestHelper h){
        Player player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(5,2,8))));
        ItemStack item=new ItemStack(Items.OAK_SLAB,2);player.setItemInHand(InteractionHand.MAIN_HAND,item);
        BlockPos support=new BlockPos(5,2,5);h.setBlock(support,Blocks.STONE);BlockPos absolute=h.absolutePos(support);
        BlockHitResult hit=new BlockHitResult(Vec3.atCenterOf(absolute).add(0,0,0.5),Direction.SOUTH,absolute,false);
        item.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit));
        check(h,h.getBlockState(support.south()).getBlock() instanceof VerticalSlabBlock,"Original slab item must place a vertical slab on side");
        check(h,item.getCount()==1,"Placement must consume one material");h.succeed();
    }
}
