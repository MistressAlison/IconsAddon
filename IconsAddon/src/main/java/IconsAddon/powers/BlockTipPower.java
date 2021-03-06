package IconsAddon.powers;
//
//import IconsAddon.IconsAddonMod;
//import IconsAddon.util.BlockModifierManager;
//import com.evacipated.cardcrawl.mod.stslib.patches.NeutralPowertypePatch;
//import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.BetterOnApplyPowerPower;
//import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.InvisiblePower;
//import com.megacrit.cardcrawl.cards.AbstractCard;
//import com.megacrit.cardcrawl.cards.DamageInfo;
//import com.megacrit.cardcrawl.core.AbstractCreature;
//import com.megacrit.cardcrawl.helpers.GameDictionary;
//import com.megacrit.cardcrawl.helpers.TipHelper;
//import com.megacrit.cardcrawl.monsters.AbstractMonster;
//import com.megacrit.cardcrawl.powers.AbstractPower;
//
//public class BlockTipPower extends AbstractPower implements BetterOnApplyPowerPower, InvisiblePower {
//
//    public static final String POWER_ID = IconsAddonMod.makeID("BlockTip");
//
//    public BlockTipPower(AbstractCreature owner) {
//        this.owner = owner;
//        this.priority = Integer.MIN_VALUE;
//        this.ID = POWER_ID;
//        this.name = TipHelper.capitalize(GameDictionary.BLOCK.NAMES[0]);
//        this.type = NeutralPowertypePatch.NEUTRAL;
//        this.loadRegion("channel");
//    }
//
//    @Override
//    public void atEndOfRound() {
//        BlockModifierManager.atEndOfRound(owner);
//    }
//
//    @Override
//    public int onHeal(int healAmount) {
//        return BlockModifierManager.onHeal(owner, healAmount);
//    }
//
//    @Override
//    public int onAttacked(DamageInfo info, int damageAmount) {
//        return BlockModifierManager.onAttackedPostBlockReductions(owner, info, damageAmount);
//    }
//
//    @Override
//    public void onAttack(DamageInfo info, int damageAmount, AbstractCreature target) {
//        BlockModifierManager.onAttack(owner, info, damageAmount, target);
//    }
//
//    @Override
//    public void onCardDraw(AbstractCard card) {
//        BlockModifierManager.onCardDraw(owner, card);
//    }
//
//    @Override
//    public void onPlayCard(AbstractCard card, AbstractMonster m) {
//        BlockModifierManager.onPlayCard(owner, card, m);
//    }
//
//    @Override
//    public boolean betterOnApplyPower(AbstractPower abstractPower, AbstractCreature target, AbstractCreature source) {
//        return BlockModifierManager.onApplyPower(owner, abstractPower, target, source);
//    }
//
//    @Override
//    public int betterOnApplyPowerStacks(AbstractPower power, AbstractCreature target, AbstractCreature source, int stackAmount) {
//        return BlockModifierManager.onApplyPowerStacks(owner, power, target, source, stackAmount);
//    }
//
//    //No source params, patch instead
//    /*
//    @Override
//    public float atDamageFinalReceive(float damage, DamageInfo.DamageType type) {
//        return CustomBlockManager.atDamageFinalReceive(owner, damage, type);
//    }
//
//    @Override
//    public float atDamageReceive(float damage, DamageInfo.DamageType damageType) {
//        return CustomBlockManager.atDamageReceive(owner, damage, damageType);
//    }*/
//
//    //These don't get target params, so we use the patch instead
//    /*
//    @Override
//    public float atDamageGive(float damage, DamageInfo.DamageType type, AbstractCard card) {
//        return super.atDamageGive(damage, type, card);
//    }
//
//    @Override
//    public float atDamageFinalGive(float damage, DamageInfo.DamageType type, AbstractCard card) {
//        return super.atDamageFinalGive(damage, type, card);
//    }*/
//
//    //These don't get card params. Use the patch
//    /*
//    @Override
//    public float modifyBlock(float blockAmount) {
//        return super.modifyBlock(blockAmount);
//    }
//
//    @Override
//    public float modifyBlockLast(float blockAmount) {
//        return super.modifyBlockLast(blockAmount);
//    }*/
//}