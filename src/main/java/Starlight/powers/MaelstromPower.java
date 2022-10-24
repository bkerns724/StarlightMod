package Starlight.powers;

import Starlight.TheStarlightMod;
import Starlight.util.Wiz;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class MaelstromPower extends AbstractPower {

    public static final String POWER_ID = TheStarlightMod.makeID(MaelstromPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public MaelstromPower(AbstractCreature owner, int amount) {
        this.ID = POWER_ID;
        this.name = NAME;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        this.loadRegion("forcefield");
        updateDescription();
    }

    @Override
    public void atStartOfTurnPostDraw() {
        flash();
        Wiz.forAllMonstersLiving(mon -> Wiz.applyToEnemy(mon, new WetPower(mon, amount)));
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1];
    }

}
