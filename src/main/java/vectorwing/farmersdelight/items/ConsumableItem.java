package vectorwing.farmersdelight.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vectorwing.farmersdelight.utils.TextUtils;

import javax.annotation.Nullable;
import java.util.List;

public class ConsumableItem extends Item
{
	private final boolean hasFoodEffectTooltip;
	private final boolean hasCustomTooltip;

	/**
	 * Items that can be consumed by an entity.
	 * When consumed, they may affect the consumer somehow, and will give back containers if applicable, regardless of their stack size.
	 */
	public ConsumableItem(Properties properties) {
		super(properties);
		this.hasFoodEffectTooltip = false;
		this.hasCustomTooltip = false;
	}

	public ConsumableItem(Properties properties, boolean hasFoodEffectTooltip, boolean hasCustomTooltip) {
		super(properties);
		this.hasFoodEffectTooltip = hasFoodEffectTooltip;
		this.hasCustomTooltip = hasCustomTooltip;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity consumer) {
		if (!worldIn.isRemote) {
			this.affectConsumer(stack, worldIn, consumer);
		}

		ItemStack container = stack.getContainerItem();

		if (stack.isFood()) {
			super.onItemUseFinish(stack, worldIn, consumer);
		} else {
			PlayerEntity player = consumer instanceof PlayerEntity ? (PlayerEntity) consumer : null;
			if (player instanceof ServerPlayerEntity) {
				CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayerEntity) player, stack);
			}
			if (player != null) {
				player.addStat(Stats.ITEM_USED.get(this));
				if (!player.abilities.isCreativeMode) {
					stack.shrink(1);
				}
			}
		}

		if (stack.isEmpty()) {
			return container;
		} else {
			if (consumer instanceof PlayerEntity && !((PlayerEntity) consumer).abilities.isCreativeMode) {
				PlayerEntity player = (PlayerEntity) consumer;
				if (!player.inventory.addItemStackToInventory(container)) {
					player.dropItem(container, false);
				}
			}
			return stack;
		}
	}

	/**
	 * Override this to apply changes to the consumer (e.g. curing effects).
	 */
	public void affectConsumer(ItemStack stack, World worldIn, LivingEntity consumer) {
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (this.hasCustomTooltip) {
			IFormattableTextComponent empty = TextUtils.getTranslation("tooltip." + this);
			tooltip.add(empty.mergeStyle(TextFormatting.BLUE));
		}
		if (this.hasFoodEffectTooltip) {
			TextUtils.addFoodEffectTooltip(stack, tooltip, 1.0F);
		}
	}
}
