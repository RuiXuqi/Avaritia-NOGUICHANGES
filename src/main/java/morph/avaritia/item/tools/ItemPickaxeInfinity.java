package morph.avaritia.item.tools;

import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import codechicken.lib.math.MathHelper;
import codechicken.lib.raytracer.RayTracer;
import morph.avaritia.Avaritia;
import morph.avaritia.api.ICosmicRenderItem;
import morph.avaritia.entity.EntityImmortalItem;
import morph.avaritia.handler.AvaritiaEventHandler;
import morph.avaritia.init.AvaritiaTextures;
import morph.avaritia.init.ModItems;
import morph.avaritia.util.ToolHelper;

public class ItemPickaxeInfinity extends ItemPickaxe implements ICosmicRenderItem {

    private static final ToolMaterial TOOL_MATERIAL = EnumHelper.addToolMaterial("INFINITY_PICKAXE", 32, 9999, 9999F,
            6.0F, 200);
    // private IIcon hammer;

    public static final Set<Material> MATERIALS = Sets.newHashSet(Material.ROCK, Material.IRON, Material.ICE,
            Material.GLASS, Material.PISTON, Material.ANVIL, Material.GRASS, Material.GROUND, Material.SAND,
            Material.SNOW, Material.CRAFTED_SNOW, Material.CLAY);

    public ItemPickaxeInfinity() {
        super(TOOL_MATERIAL);
        setTranslationKey("avaritia:infinity_pickaxe");
        setRegistryName("infinity_pickaxe");
        setCreativeTab(Avaritia.tab);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.DARK_GRAY + "" +
                    I18n.translateToLocal("tooltip." + getTranslationKey(stack) + "_0.desc"));
            tooltip.add(TextFormatting.DARK_GRAY + "" +
                    I18n.translateToLocal("tooltip." + getTranslationKey(stack) + "_1.desc"));
            tooltip.add(TextFormatting.DARK_GRAY + "" +
                    I18n.translateToLocal("tooltip." + getTranslationKey(stack) + "_2.desc"));
        } else {
            tooltip.add(TextFormatting.GRAY + "" + I18n.translateToLocal("tooltip.item.avaritia:tool.desc"));
        }
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (isInCreativeTab(tab)) {
            ItemStack pick = new ItemStack(this);
            pick.addEnchantment(Enchantments.FORTUNE, 10);
            list.add(pick);
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return ModItems.COSMIC_RARITY;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        if (stack.getTagCompound() != null && stack.getTagCompound().getBoolean("hammer")) {
            return 5.0F;
        }
        for (String type : getToolClasses(stack)) {
            if (state.getBlock().isToolEffective(type, state)) {
                return efficiency;
            }
        }
        return Math.max(super.getDestroySpeed(stack, state), 6.0F);
    }

    // @SideOnly (Side.CLIENT)
    // public void registerIcons(IIconRegister ir) {
    // this.itemIcon = ir.registerIcon("avaritia:infinity_pickaxe");
    // hammer = ir.registerIcon("avaritia:infinity_hammer");
    // }

    // @Override
    // public IIcon getIcon(ItemStack stack, int pass) {
    // NBTTagCompound tags = stack.getTagCompound();
    // if (tags != null) {
    // if (tags.getBoolean("hammer")) {
    // return hammer;
    // }
    // }
    // return itemIcon;
    // }

    // @SideOnly (Side.CLIENT)
    // @Override
    // public IIcon getIconIndex(ItemStack stack) {
    // return getIcon(stack, 0);
    // }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            NBTTagCompound tags = stack.getTagCompound();
            if (tags == null) {
                tags = new NBTTagCompound();
                stack.setTagCompound(tags);
            }
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack) < 10) {
                stack.addEnchantment(Enchantments.FORTUNE, 10);
            }
            tags.setBoolean("hammer", !tags.getBoolean("hammer"));
            player.swingArm(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase victim, EntityLivingBase player) {
        if (stack.getTagCompound() != null) {
            if (stack.getTagCompound().getBoolean("hammer")) {
                if (!(victim instanceof EntityPlayer && AvaritiaEventHandler.isInfinite((EntityPlayer) victim))) {
                    int i = 10;
                    victim.addVelocity(-MathHelper.sin(player.rotationYaw * (float) Math.PI / 180.0F) * i * 0.5F, 2.0D,
                            MathHelper.cos(player.rotationYaw * (float) Math.PI / 180.0F) * i * 0.5F);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        if (stack.getTagCompound() != null && stack.getTagCompound().getBoolean("hammer")) {
            RayTraceResult traceResult = RayTracer.retrace(player, 10, true);
            if (traceResult != null) {
                breakOtherBlock(player, stack, pos, traceResult.sideHit);
            }
        }
        return false;
    }

    public void breakOtherBlock(EntityPlayer player, ItemStack stack, BlockPos pos, EnumFacing sideHit) {
        World world = player.world;
        IBlockState state = world.getBlockState(pos);
        Material mat = state.getMaterial();
        if (!MATERIALS.contains(mat)) {
            return;
        }

        if (state.getBlock().isAir(state, world, pos)) {
            return;
        }

        boolean doY = sideHit.getAxis() != Axis.Y;

        int range = 8;
        BlockPos minOffset = new BlockPos(-range, doY ? -1 : -range, -range);
        BlockPos maxOffset = new BlockPos(range, doY ? range * 2 - 2 : range, range);

        ToolHelper.aoeBlocks(player, stack, world, pos, minOffset, maxOffset, null, MATERIALS, true);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        return new EntityImmortalItem(world, location, itemstack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return false;
    }

    @Override
    public TextureAtlasSprite getMaskTexture(ItemStack stack, @Nullable EntityLivingBase player) {
        if (stack.hasTagCompound()) {
            assert stack.getTagCompound() != null;
            if (stack.getTagCompound().getBoolean("hammer")) {
                return AvaritiaTextures.INFINITY_PICKAXE_MASK_1;
            }
        }
        return AvaritiaTextures.INFINITY_PICKAXE_MASK_0;
    }

    @Override
    public float getMaskOpacity(ItemStack stack, @Nullable EntityLivingBase player) {
        return 1.0f;
    }
}
