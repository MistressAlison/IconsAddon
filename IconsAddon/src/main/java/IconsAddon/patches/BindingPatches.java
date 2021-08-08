package IconsAddon.patches;

import IconsAddon.blockModifiers.AbstractBlockModifier;
import IconsAddon.damageModifiers.AbstractDamageModifier;
import IconsAddon.powers.OnCreateBlockContainerPower;
import IconsAddon.powers.OnCreateDamageInfoPower;
import IconsAddon.util.BlockContainer;
import IconsAddon.util.BlockModifierManager;
import IconsAddon.util.DamageModifierManager;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

public class BindingPatches {

    public static final ArrayList<AbstractDamageModifier> directlyBoundDamageMods = new ArrayList<>();
    public static final ArrayList<AbstractBlockModifier> directlyBoundBlockMods = new ArrayList<>();
    public static Object directlyBoundInstigator;
    private static AbstractCard cardInUse;

    @SpirePatch(clz = AbstractGameAction.class, method = SpirePatch.CLASS)
    public static class BoundGameAction {
        public static SpireField<Object> actionDelayedInstigator = new SpireField<>(() -> null);
        public static SpireField<AbstractCard> actionDelayedCardInUse = new SpireField<>(() -> null);
        public static SpireField<ArrayList<AbstractDamageModifier>> actionDelayedDamageMods = new SpireField<>(ArrayList::new);
        public static SpireField<ArrayList<AbstractBlockModifier>> actionDelayedBlockMods = new SpireField<>(ArrayList::new);
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "useCard")
    public static class GrabCardInUse {
        @SpireInsertPatch(locator = Locator.class)
        public static void RememberCardPreUseCall(AbstractPlayer __instance, AbstractCard c, AbstractMonster monster, int energyOnUse) {
            //Right before you call card.use, set it as the object in use
            cardInUse = c;
        }
        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractCard.class, "use");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
        @SpireInsertPatch(locator = Locator2.class)
        public static void ForgetCardPostUseCall(AbstractPlayer __instance, AbstractCard c, AbstractMonster monster, int energyOnUse) {
            //Once you call card.use, set the object back to null, as any actions were already added to the queue
            cardInUse = null;
        }
        private static class Locator2 extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(GameActionManager.class, "addToBottom");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(clz = GameActionManager.class, method = "addToTop")
    @SpirePatch(clz = GameActionManager.class, method = "addToBottom")
    public static class BindObjectToAction {
        @SpirePrefixPatch
        public static void WithoutCrashingHopefully(GameActionManager __instance, AbstractGameAction action) {
            //When our action is added to the queue, see if there is an active object in use that caused this to happen
            if (cardInUse != null) {
                //If so, this is our instigator object, we need to add any non-innate card mods
                BoundGameAction.actionDelayedCardInUse.set(action, cardInUse);
            }
        }
    }

    @SpirePatch(clz = DamageInfo.class, method = "<ctor>", paramtypez = {AbstractCreature.class, int.class, DamageInfo.DamageType.class})
    private static class BindObjectToDamageInfo {
        @SpirePostfixPatch()
        public static void PostfixMeToPiggybackBinding(DamageInfo __instance, AbstractCreature damageSource, int base, DamageInfo.DamageType type) {
            AbstractCard instigatorCard = null;
            //Grab the action currently running, as this is what was processing when our damage info was created
            AbstractGameAction a = AbstractDungeon.actionManager.currentAction;
            if (a != null) {
                if (!BoundGameAction.actionDelayedDamageMods.get(a).isEmpty()) {
                    DamageModifierManager.bindDamageMods(__instance, BoundGameAction.actionDelayedDamageMods.get(a));
                    if (BoundGameAction.actionDelayedInstigator.get(a) instanceof AbstractCard) {
                        instigatorCard = BoundGameAction.actionDelayedCardInUse.get(a);
                    }
                }
                if (BoundGameAction.actionDelayedCardInUse.get(a) != null && a.source == damageSource) {
                    DamageModifierManager.bindDamageMods(__instance, DamageModifierManager.modifiers(BoundGameAction.actionDelayedCardInUse.get(a)).stream().filter(m -> m.automaticBindingForCards).collect(Collectors.toList()));
                    instigatorCard = BoundGameAction.actionDelayedCardInUse.get(a);
                }
            }
            if (!directlyBoundDamageMods.isEmpty()) {
                DamageModifierManager.bindDamageMods(__instance, directlyBoundDamageMods);
                if (directlyBoundInstigator instanceof AbstractCard) {
                    instigatorCard = (AbstractCard) directlyBoundInstigator;
                }
            }
            if (cardInUse != null) {
                DamageModifierManager.bindDamageMods(__instance, DamageModifierManager.modifiers(cardInUse).stream().filter(m -> m.automaticBindingForCards).collect(Collectors.toList()));
                instigatorCard = cardInUse;
            }
            if (damageSource != null) {
                for (AbstractPower p : damageSource.powers) {
                    if (p instanceof OnCreateDamageInfoPower) {
                        ((OnCreateDamageInfoPower) p).onCreateDamageInfo(__instance, instigatorCard);
                    }
                }
            }
            DamageModifierManager.bindInstigatorCard(__instance, instigatorCard);
        }
    }

    @SpirePatch(clz = AbstractCreature.class, method = "addBlock")
    public static class AddBlockMakePlaceHolderIfNeeded {
        static final HashSet<AbstractBlockModifier> blockSet = new HashSet<>();
        @SpireInsertPatch(locator = CreatureAddBlockLocator.class, localvars = "tmp")
        public static void pls(AbstractCreature __instance, int amount, float tmp) {
            AbstractCard instigatorCard = null;
            //Grab the action currently running, as this is what was processing when our block method was called
            AbstractGameAction a = AbstractDungeon.actionManager.currentAction;
            if (a != null) {
                //If the action is not null, see if it has an instigator object
                if (!BoundGameAction.actionDelayedBlockMods.get(a).isEmpty()) {
                    blockSet.addAll(BoundGameAction.actionDelayedBlockMods.get(a));
                }
                if (BoundGameAction.actionDelayedCardInUse.get(a) != null) {
                    for (AbstractBlockModifier m : BlockModifierManager.modifiers(BoundGameAction.actionDelayedCardInUse.get(a))) {
                        if (m.automaticBindingForCards) {
                            blockSet.add(m);
                        }
                    }
                    instigatorCard = BoundGameAction.actionDelayedCardInUse.get(a);
                }
            }
            if (!directlyBoundBlockMods.isEmpty()) {
                blockSet.addAll(directlyBoundBlockMods);
            }
            if (cardInUse != null) {
                for (AbstractBlockModifier m : BlockModifierManager.modifiers(cardInUse)) {
                    if (m.automaticBindingForCards) {
                        blockSet.add(m);
                    }
                }
                instigatorCard = cardInUse;
            }
            for (AbstractPower p : __instance.powers) {
                if (p instanceof OnCreateBlockContainerPower) {
                    ((OnCreateBlockContainerPower) p).onCreateBlockContainer(blockSet, instigatorCard);
                }
            }
            ArrayList<AbstractBlockModifier> blockTypes = new ArrayList<>();
            for (AbstractBlockModifier m : blockSet) {
                blockTypes.add(m.makeCopy());
            }
            blockSet.clear();
            Collections.sort(blockTypes);
            BlockContainer b = new BlockContainer(__instance, (int)tmp, blockTypes);
            BlockModifierManager.addBlockContainer(__instance, b);
        }
    }

    private static class CreatureAddBlockLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(MathUtils.class, "floor");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}