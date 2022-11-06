package Starlight.potions;

import Starlight.TheStarlightMod;
import Starlight.cards.ArcaneArrow;
import Starlight.util.Wiz;
import basemod.abstracts.CustomPotion;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.PotionStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;

public class BottledFists extends CustomPotion {


    public static final String POTION_ID = TheStarlightMod.makeID(BottledFists.class.getSimpleName());
    private static final PotionStrings potionStrings = CardCrawlGame.languagePack.getPotionString(POTION_ID);

    public static final String NAME = potionStrings.NAME;
    public static final String[] DESCRIPTIONS = potionStrings.DESCRIPTIONS;
    public static final int EFFECT = 2;

    public BottledFists() {
        super(NAME, POTION_ID, PotionRarity.UNCOMMON, PotionSize.SPIKY, PotionColor.FIRE);
        isThrown = false;
        targetRequired = false;
    }

    @Override
    public void use(AbstractCreature target) {
        AbstractCard c = new ArcaneArrow();
        c.upgrade();
        c.setCostForTurn(0);
        Wiz.makeInHand(c, potency);
    }

    // This is your potency.
    @Override
    public int getPotency(final int ascensionLevel) {
        return EFFECT;
    }

    @Override
    public void initializeData() {
        potency = getPotency();
        description = potionStrings.DESCRIPTIONS[0] + potency + potionStrings.DESCRIPTIONS[1];
        tips.clear();
        tips.add(new PowerTip(name, description));
        /*tips.add(new PowerTip(
                BaseMod.getKeywordTitle(potionStrings.DESCRIPTIONS[2].toLowerCase()),
                BaseMod.getKeywordDescription(potionStrings.DESCRIPTIONS[2].toLowerCase())
        ));*/
    }

    @Override
    public AbstractPotion makeCopy() {
        return new BottledFists();
    }
}