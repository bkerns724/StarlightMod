package Starlight.cards.bookOfSpace;

import Starlight.cards.abstracts.AbstractMagickCard;
import Starlight.cards.interfaces.OnProjectCard;
import Starlight.util.CardArtRoller;
import Starlight.util.CustomTags;
import Starlight.util.Wiz;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.blue.MeteorStrike;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.vfx.combat.WeightyImpactEffect;

import static Starlight.TheStarlightMod.makeID;

public class Meteor extends AbstractMagickCard implements OnProjectCard {
    public final static String ID = makeID(Meteor.class.getSimpleName());

    private static final CardRarity RARITY = CardRarity.UNCOMMON;
    private static final CardTarget TARGET = CardTarget.ENEMY;
    private static final CardType TYPE = CardType.ATTACK;

    private static final int COST = 2;
    private static final int DMG = 13;
    private static final int UP_DMG = 4;
    private static final int VULN = 2;

    private boolean projected;

    public Meteor() {
        super(ID, COST, TYPE, RARITY, TARGET);
        baseDamage = damage = DMG;
        //baseMagicNumber = magicNumber = VULN;
        tags.add(CustomTags.STARLIGHT_SPACE);
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if (m != null) {
            this.addToBot(new VFXAction(new WeightyImpactEffect(m.hb.cX, m.hb.cY)));
        }
        this.addToBot(new WaitAction(0.8F));
        dmg(m, AbstractGameAction.AttackEffect.NONE);
        //Wiz.applyToEnemy(m, new WeakPower(m, magicNumber, false));
        projected = false;
    }

    @Override
    public void applyPowers() {
        super.applyPowers();
        if (projected) {
            this.damage *= 2;
        }
        this.isDamageModified = baseDamage != damage;
    }

    @Override
    public void calculateCardDamage(AbstractMonster mo) {
        super.calculateCardDamage(mo);
        if (projected) {
            this.damage *= 2;
        }
        this.isDamageModified = baseDamage != damage;
    }

    public void upp() {
        upgradeDamage(UP_DMG);
    }

    @Override
    public void onProject() {
        projected = true;
    }

    @Override
    public String cardArtCopy() {
        return MeteorStrike.ID;
    }

    @Override
    public CardArtRoller.ReskinInfo reskinInfo(String ID) {
        return new CardArtRoller.ReskinInfo(ID, 0.12f, 0.5f, 0.5f, 0.5f, false);
    }
}