package hugman.ce_foodstuffs.objects.item;

import hugman.ce_foodstuffs.objects.item.tea.TeaHelper;
import hugman.ce_foodstuffs.objects.item.tea.TeaType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class TeaBottleItem extends Item {
	public TeaBottleItem(Settings settings) {
		super(settings);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		TeaHelper.appendTeaTooltip(tooltip, TeaHelper.getTeaTypesByCompound(stack.getTag()));
	}

	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
		if(group == ItemGroup.SEARCH) {
			for(TeaType teaType : TeaHelper.getAllTypes()) {
				stacks.add(TeaHelper.appendTeaType(new ItemStack(this), teaType));
			}
		}
		else if(this.isIn(group)) {
			for(TeaType.Flavor flavor : TeaType.Flavor.values()) {
				stacks.add(TeaHelper.appendTeaType(new ItemStack(this), new TeaType(TeaType.Strength.NORMAL, flavor)));
			}
		}
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 46;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return ItemUsage.consumeHeldItem(world, user, hand);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity) user : null;
		if(user instanceof ServerPlayerEntity) {
			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) user;
			Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
			serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
		}
		if(!world.isClient) {
			List<TeaType> teaTypes = TeaHelper.getTeaTypesByCompound(stack.getTag());
			if(!teaTypes.isEmpty()) {
				for(TeaType teaType : teaTypes) {
					StatusEffect effect = teaType.getFlavor().getEffect();
					if(effect != null) {
						if(effect.isInstant()) {
							effect.applyInstantEffect(user, user, user, teaType.getStrength().getPotency(), 1.0D);
						}
						else {
							user.addStatusEffect(new StatusEffectInstance(effect, teaType.getStrength().getPotency() * 400));

						}
					}
					if(teaType.getFlavor() == TeaType.Flavor.GLOOPY) {
						Items.CHORUS_FRUIT.finishUsing(stack, world, user);
					}
				}
			}
		}
		if(playerEntity != null) {
			playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
			if(!playerEntity.abilities.creativeMode) {
				stack.decrement(1);
			}
		}
		if(playerEntity == null || !playerEntity.abilities.creativeMode) {
			if(stack.isEmpty()) {
				return new ItemStack(Items.GLASS_BOTTLE);
			}
			if(playerEntity != null) {
				playerEntity.inventory.insertStack(new ItemStack(Items.GLASS_BOTTLE));
			}
		}
		return stack;
	}
}