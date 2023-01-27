package Starlight.ui;

import Starlight.cards.interfaces.OnProjectCard;
import Starlight.patches.CardCounterPatches;
import Starlight.powers.interfaces.OnProjectPower;
import Starlight.util.CustomTags;
import Starlight.util.Wiz;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.ClearCardQueueAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.OverlayMenu;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.ui.panels.ExhaustPanel;
import com.megacrit.cardcrawl.vfx.BobEffect;
import javassist.CtBehavior;

public class ProjectedCardManager {
    public static final float Y_OFFSET = 70f * Settings.scale;
    public static final float X_OFFSET = 100f * Settings.scale;
    public static final CardGroup cards = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
    public static final CardGroup renderQueue = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
    private static final BobEffect bob = new BobEffect(3.0f * Settings.scale, 3.0f);
    public static AbstractCard hovered;

    public static void render(SpriteBatch sb) {
        for (AbstractCard card : cards.group) {
            if (card != hovered) {
                card.render(sb);
            }
        }
        if (hovered != null) {
            hovered.render(sb);
            TipHelper.renderTipForCard(hovered, sb, hovered.keywords);
        }
        renderQueue.render(sb);
    }

    public static void update() {
        bob.update();
        int i = 0;
        hovered = null;
        for (AbstractCard card : cards.group) {
            card.target_y = Wiz.adp().hb.cY + Wiz.adp().hb.height/2f + Y_OFFSET + bob.y;
            card.target_x = Wiz.adp().hb.cX + X_OFFSET * (cards.size()-1) / 2f - X_OFFSET * i;
            card.targetAngle = 0f;
            card.update();
            card.hb.update();
            if (card.hb.hovered && hovered == null) {
                card.targetDrawScale = 0.75f;
                hovered = card;
            } else {
                card.targetDrawScale = 0.2f;
            }
            card.applyPowers();
            i++;
        }
        renderQueue.update();
    }

    public static void playCards() {
        /*Wiz.atb(new AbstractGameAction() {
            @Override
            public void update() {
                for (AbstractCard card : cards.group) {
                    card.targetDrawScale = 0.75F;
                    card.applyPowers();
                    ProjectedCardField.projectedField.set(card, true);
                    Wiz.atb(new NewQueueCardAction(card, true, false, true));
                }
                renderQueue.group.addAll(cards.group);
                cards.clear();
                this.isDone = true;
            }
        });*/
        for (AbstractCard card : cards.group) {
            card.targetDrawScale = 0.75F;
            card.applyPowers();
            ProjectedCardField.projectedField.set(card, true);
        }
        renderQueue.group.addAll(cards.group);
        cards.clear();
        playNextCard();
    }

    public static void playNextCard() {
        if (!renderQueue.isEmpty()) {
            AbstractCard card = renderQueue.group.get(0);
            if (card.target == AbstractCard.CardTarget.ENEMY && AbstractDungeon.getMonsters().monsters.stream().noneMatch(m -> !m.isDead && !m.escaped && !m.halfDead)) {
                Wiz.atb(new UseCardAction(card, null));
            } else {
                Wiz.atb(new NewQueueCardAction(card, true, false, true));
            }
        }
    }

    public static void addCard(AbstractCard card) {
        addCard(card, true);
    }

    public static void addCard(AbstractCard card, boolean playSFX) {
        card.targetAngle = 0f;
        card.beginGlowing();
        cards.addToTop(card);
        if (card instanceof OnProjectCard) {
            ((OnProjectCard) card).onProject();
        }
        for (AbstractPower p : Wiz.adp().powers) {
            if (p instanceof OnProjectPower) {
                ((OnProjectPower) p).onProject(card);
            }
        }
        if (playSFX) {
            CardCrawlGame.sound.play("ORB_SLOT_GAIN", 0.1F);
        }
        CardCounterPatches.cardsProjectedThisTurn++;
        CardCounterPatches.cardsProjectedThisCombat++;
    }

    @SpirePatch2(clz = AbstractCard.class, method = SpirePatch.CLASS)
    public static class ProjectedCardField {
        public static SpireField<Boolean> projectedField = new SpireField<>(() -> false);
    }

    @SpirePatch2(clz = UseCardAction.class, method = SpirePatch.CLASS)
    public static class ProjectedActionField {
        public static SpireField<Boolean> projectedField = new SpireField<>(() -> false);
    }

    @SpirePatch2(clz = UseCardAction.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.class, AbstractCreature.class})
    public static class InheritProjectedField {
        @SpirePrefixPatch
        public static void pushProjected(UseCardAction __instance, AbstractCard card) {
            if (ProjectedCardField.projectedField.get(card)) {
                ProjectedActionField.projectedField.set(__instance, true);
                ProjectedCardField.projectedField.set(card, false);
                renderQueue.removeCard(card);
                playNextCard();
            }
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "applyStartOfTurnCards")
    public static class PlayCards {
        @SpirePrefixPatch
        public static void playCards() {
            ProjectedCardManager.playCards();
        }
    }

    @SpirePatch2(clz = OverlayMenu.class, method = "render")
    public static class RenderPanel {
        @SpireInsertPatch(locator = Locator.class)
        public static void render(OverlayMenu __instance, SpriteBatch sb) {
            ProjectedCardManager.render(sb);
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher m = new Matcher.MethodCallMatcher(ExhaustPanel.class, "render");
                return LineFinder.findInOrder(ctBehavior, m);
            }
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "combatUpdate")
    public static class UpdatePile {
        @SpirePostfixPatch
        public static void update(AbstractPlayer __instance) {
            ProjectedCardManager.update();
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "preBattlePrep")
    @SpirePatch2(clz = AbstractPlayer.class, method = "onVictory")
    public static class EmptyCards {
        @SpirePostfixPatch
        public static void yeet() {
            cards.clear();
            renderQueue.clear();
        }
    }

    @SpirePatch2(clz = UseCardAction.class, method = "update")
    public static class AscendedFix {
        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn<?> yeet(UseCardAction __instance, AbstractCard ___targetCard) {
            if (___targetCard.hasTag(CustomTags.STARLIGHT_ASCENDED)) {
                ProjectedCardManager.addCard(___targetCard, false);
                __instance.isDone = true;
                AbstractDungeon.player.cardInUse = null;
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher m = new Matcher.FieldAccessMatcher(AbstractCard.class, "purgeOnUse");
                return LineFinder.findInOrder(ctBehavior, m);
            }
        }
    }

    @SpirePatch2(clz = ClearCardQueueAction.class, method = "update")
    public static class StopYeetingMyFuckingCards {
        @SpirePostfixPatch
        public static void addCardsBack() {
            for (AbstractCard card : renderQueue.group) {
                Wiz.atb(new NewQueueCardAction(card, true, false, true));
            }
        }
    }
}
