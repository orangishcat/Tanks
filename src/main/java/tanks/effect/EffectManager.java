package tanks.effect;

import it.unimi.dsi.fastutil.objects.*;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.event.EventStatusEffectBegin;
import tanks.network.event.EventStatusEffectDeteriorate;
import tanks.network.event.EventStatusEffectEnd;
import tanks.tank.Tank;

import java.util.*;

public class EffectManager
{
    public Movable movable;

    public Object2ObjectOpenHashMap<String, AttributeModifier> attributes = new Object2ObjectOpenHashMap<>();
    public HashSet<String> attributeImmunities = new HashSet<>();
    private final PriorityQueue<AttributeModifier> attributeRemovalQueue =
            new PriorityQueue<>(Comparator.comparingDouble(AttributeModifier::getTimeLeft));

    public HashMap<AttributeModifier.Type, AttributeModifier.Instance> typeInstances = new HashMap<>();
    public Object2ObjectOpenHashMap<String, StatusEffect.Instance> statusEffects = new Object2ObjectOpenHashMap<>();
    private final PriorityQueue<StatusEffect.Instance> statusEffectRemovalQueue =
            new PriorityQueue<>(Comparator.comparingDouble(StatusEffect.Instance::getTimeLeft));

    public BiConsumer<AttributeModifier, Boolean> addAttributeCallback;

    public EffectManager(Movable m)
    {
        this.movable = m;
    }

    public void update()
    {
        updateAttributes();
        updateStatusEffects();
    }

    /**
     * Increment ages of status effects, remove them if past duration,
     * and rely on each effect’s attribute modifiers to auto-update partial intensities.
     */
    public void updateStatusEffects()
    {
        double frameFrequency = movable.affectedByFrameFrequency ? Panel.frameFrequency : 1;

        for (Object2ObjectMap.Entry<String, StatusEffect.Instance> entry : Object2ObjectMaps.fastIterable(statusEffects))
        {
            StatusEffect.Instance inst = entry.getValue();
            double oldAge = inst.age;

            // Check for transition into deterioration
            if (oldAge < inst.deteriorationAge && oldAge + frameFrequency >= inst.deteriorationAge
                    && ScreenPartyHost.isServer && (movable instanceof Bullet || movable instanceof Tank))
                Game.eventsOut.add(new EventStatusEffectDeteriorate(movable, inst.effect, inst.duration - inst.deteriorationAge));

            // Advance age
            if (inst.duration <= 0 || inst.age + frameFrequency <= inst.duration)
            {
                inst.age += frameFrequency;
            }
            else
            {
                statusEffectRemovalQueue.add(inst);

                if (movable instanceof Bullet || movable instanceof Tank)
                    Game.eventsOut.add(new EventStatusEffectEnd(movable, inst.effect));
            }
        }

        // Remove any fully-expired effects
        while (!statusEffectRemovalQueue.isEmpty())
        {
            StatusEffect.Instance top = statusEffectRemovalQueue.peek();
            if (top == null)
                break;

            if (top.age >= top.duration && top.duration > 0)
            {
                statusEffectRemovalQueue.poll();
                // remove all its attribute modifiers
                removeStatusEffect(top.effect.name);
            }
            else
                break;
        }
    }

    /**
     * Returns the final value for a given attribute type,
     * factoring in all active attributes in the attributes map.
     */
    public double getAttributeValue(AttributeModifier.Type type, double baseValue)
    {
        // If you want the type->Instance grouping
        AttributeModifier.Instance inst = typeInstances.get(type);
        if (inst != null)
            baseValue = inst.apply(baseValue);
        return baseValue;
    }

    /**
     * Retrieves the group of attributes for a given Type (if you still want that).
     */
    public AttributeModifier.Instance getAttribute(AttributeModifier.Type type)
    {
        AttributeModifier.Instance instance = typeInstances.get(type);
        if (instance == null || instance.isEmpty())
            return null;
        return instance;
    }

    /**
     * Remove all attributes of a given type (both normal & status-effect attributes).
     */
    public void removeAttribute(AttributeModifier.Type type)
    {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, AttributeModifier> e : attributes.entrySet())
        {
            if (e.getValue().type == type)
                toRemove.add(e.getKey());
        }
        for (String k : toRemove)
            attributes.remove(k);

        typeInstances.remove(type);
    }

    /**
     * Adds a normal attribute if not immune.
     */
    public void addAttribute(AttributeModifier m)
    {
        if (this.attributeImmunities.contains(m.name))
            return;

        attributes.put(m.name, m);
        typeInstances.computeIfAbsent(m.type, AttributeModifier.Instance::new).attributeList.add(m);
        addAttributeCallback.accept(m, false);
    }

    /**
     * Removes any previous attribute with the same name, then adds the new one if not immune.
     */
    public void addUnduplicateAttribute(AttributeModifier m)
    {
        if (this.attributeImmunities.contains(m.name))
            return;

        AttributeModifier old = attributes.remove(m.name);
        if (old != null)
        {
            AttributeModifier.Instance inst = typeInstances.get(old.type);
            if (inst != null)
                inst.attributeList.remove(old);
        }

        attributes.put(m.name, m);
        addAttributeCallback.accept(m, true);
        typeInstances.computeIfAbsent(m.type, AttributeModifier.Instance::new).attributeList.add(m);
    }

    public void addImmunities(String... immunities)
    {
        Collections.addAll(attributeImmunities, immunities);
    }

    /**
     * Creates an effect instance, also spawns a StatusEffectAttributeModifier
     * for each attribute in the effect, then adds them to the attributes map.
     */
    public void addStatusEffect(StatusEffect s, double warmup, double deterioration, double duration)
    {
        this.addStatusEffect(s, 0, warmup, deterioration, duration);
    }

    public void addStatusEffect(StatusEffect s, double age, double warmup, double deterioration, double duration)
    {
        if (deterioration > duration)
            throw new RuntimeException("Deterioration age > duration");

        // If there's an existing effect in the same "family", remove it
        StatusEffect prevEffect = null;
        for (StatusEffect.Instance inst : this.statusEffects.values())
        {
            if (inst.effect.family != null && inst.effect.family.equals(s.family))
                prevEffect = inst.effect;
        }
        if (prevEffect != null)
            removeStatusEffect(prevEffect.name);

        // Possibly skip if partially overlapping, etc. (same logic as before)
        StatusEffect.Instance existing = this.statusEffects.get(s.name);
        boolean skipAddition = false;
        if (warmup <= age && existing != null)
        {
            if (existing.age >= existing.warmupAge && existing.age < existing.deteriorationAge)
                skipAddition = true;
        }

        // Fire network event
        if (!skipAddition && (movable instanceof Bullet || movable instanceof Tank) && ScreenPartyHost.isServer)
            Game.eventsOut.add(new EventStatusEffectBegin(movable, s, age, warmup));

        // Create the new instance
        StatusEffect.Instance newInst = new StatusEffect.Instance(s, age, warmup, deterioration, duration);
        this.statusEffects.put(s.name, newInst);

        // Insert each attribute from the StatusEffect as a dynamic attribute
        for (AttributeModifier baseMod : s.attributeModifiers)
        {
            StatusEffectAttributeModifier sm = new StatusEffectAttributeModifier(newInst, baseMod);
            // Add it to the normal attributes map so that it is fully integrated
            this.addUnduplicateAttribute(sm);
        }
    }

    /**
     * Removes the status effect and all its associated attribute modifiers from the map.
     */
    public void removeStatusEffect(String effectName)
    {
        StatusEffect.Instance inst = this.statusEffects.remove(effectName);
        if (inst != null)
        {
            // Also remove all attribute modifiers belonging to this effect
            // We know their names start with "statusEffect:<effectName>_"
            List<String> keysToRemove = new ArrayList<>();
            for (Map.Entry<String, AttributeModifier> e : attributes.entrySet())
            {
                if (e.getKey().startsWith("statusEffect:" + effectName + "_"))
                    keysToRemove.add(e.getKey());
            }
            for (String k : keysToRemove)
                attributes.remove(k);

            statusEffectRemovalQueue.remove(inst);
        }
    }

    /**
     * Updates normal attributes: increments age, removes if expired, etc.
     * (Status-effect-based attributes will also get updated if they override update().)
     */
    public void updateAttributes()
    {
        for (Object2ObjectMap.Entry<String, AttributeModifier> entry : Object2ObjectMaps.fastIterable(attributes))
        {
            AttributeModifier a = entry.getValue();
            a.update();
            if (a.expired)
                attributeRemovalQueue.add(a);
        }

        while (!attributeRemovalQueue.isEmpty() && attributeRemovalQueue.peek().expired)
        {
            AttributeModifier expired = attributeRemovalQueue.poll();
            if (attributes.get(expired.name) == expired)
            {
                attributes.remove(expired.name);

                // Also remove from typeInstances if present
                AttributeModifier.Instance inst = typeInstances.get(expired.type);
                if (inst != null)
                    inst.attributeList.remove(expired);
            }
        }
    }

    /**
     * New: a subclass of AttributeModifier that is driven by a StatusEffect.Instance.
     * This allows each effect's partial intensity to be directly updated as the effect ages.
     */
    public static class StatusEffectAttributeModifier extends AttributeModifier
    {
        public StatusEffect.Instance instance;
        public AttributeModifier baseModifier; // the original static data from the StatusEffect

        public StatusEffectAttributeModifier(StatusEffect.Instance inst, AttributeModifier baseMod)
        {
            // We build a name to differentiate it from normal attributes:
            // e.g. "statusEffect:<effectName>_<baseMod.name>"
            super("statusEffect:" + inst.effect.name + "_" + baseMod.name,
                    baseMod.type, baseMod.operation, baseMod.value);
            this.instance = inst;
            this.baseModifier = baseMod;
        }

        @Override
        public void update()
        {
            super.update();
            // Our "age" matches the status effect's age
            this.age = instance.age;

            // Check if effect is expired:
            if (instance.age >= instance.duration && instance.duration > 0)
                this.expired = true;
            else
            {
                // Recompute partial intensity:
                // The partial "value" depends on warmupAge and deteriorationAge
                double warm = instance.warmupAge;
                double det = instance.deteriorationAge;
                double dur = instance.duration;
                double a   = instance.age;

                double originalVal = baseModifier.value;

                double partial;
                if (a < warm)
                    partial = originalVal * a / warm;         // ramp up
                else if (a < det || det <= 0)
                    partial = originalVal;                    // full strength
                else
                    partial = originalVal * (dur - a) / (dur - det);  // ramp down

                // Store the partial result into this.value for usage in getValue()
                this.value = partial;
            }
        }

        @Override
        public double getTimeLeft()
        {
            // If we want to tie attribute lifetime to the effect's lifetime:
            return instance.duration - instance.age;
        }
    }
}
