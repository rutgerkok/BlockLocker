package nl.rutgerkok.blocklocker.impl.profile;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;

import nl.rutgerkok.blocklocker.ProfileFactory;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.group.CombinedGroupSystem;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;

public class TestPlayerProfile {

    private ProfileFactoryImpl getProfileFactory() {
        return new ProfileFactoryImpl(new CombinedGroupSystem(), new NullTranslator());
    }

    @Test
    public void testIncludes() {
        ProfileFactoryImpl factory = getProfileFactory();
        UUID bobId = UUID.randomUUID();
        String everyoneTag = "[" + new NullTranslator().getWithoutColor(Translation.TAG_EVERYONE) + "]";

        Profile bob = factory.fromNameAndUniqueId("Bob", Optional.of(bobId));
        Profile bobRenamed = factory.fromNameAndUniqueId("Bob2", Optional.of(bobId));
        Profile jane = factory.fromNameAndUniqueId("Jane", Optional.of(UUID.randomUUID()));
        Profile janeWithoutId = factory.fromDisplayText("jane");
        Profile everyone = factory.fromDisplayText(everyoneTag);

        assertTrue(bob.includes(bobRenamed), "Same id");
        assertTrue(bobRenamed.includes(bob), "Same id");
        assertFalse(jane.includes(janeWithoutId), "Known id, not present in other");
        assertTrue(janeWithoutId.includes(jane), "Unknown id, same name");
        assertFalse(bob.includes(jane), "Different id and name");
        assertFalse(bob.includes(janeWithoutId), "Different id and name");

        // Everyone includes everyone, but is never included
        assertTrue(everyone.includes(bob));
        assertTrue(everyone.includes(jane));
        assertTrue(everyone.includes(janeWithoutId));
        assertFalse(bob.includes(everyone));
        assertFalse(jane.includes(everyone));
        assertFalse(janeWithoutId.includes(everyone));
    }

    @Test
    public void testNameAndId() {
        String name = "test";
        UUID uuid = UUID.randomUUID();
        ProfileFactory factory = getProfileFactory();
        Profile profile = factory.fromNameAndUniqueId(name, Optional.of(uuid));

        // Test object properties
        assertEquals(name, profile.getDisplayName());
        assertEquals(uuid, ((PlayerProfile) profile).getUniqueId().get());
    }

    @Test
    public void testNameAndIdJson() {
        String name = "test";
        UUID uuid = UUID.randomUUID();
        ProfileFactory factory = getProfileFactory();
        Profile profile = factory.fromNameAndUniqueId(name, Optional.of(uuid));
        JsonObject object = profile.getSaveObject();

        assertEquals(name, object.get(PlayerProfileImpl.NAME_KEY));
        assertEquals(uuid.toString(), object.get(PlayerProfileImpl.UUID_KEY));
    }

    @Test
    public void testPlayerProfileRoundtrip() {
        String name = "test";
        UUID uuid = UUID.randomUUID();
        ProfileFactoryImpl factory = getProfileFactory();
        Profile profile = factory.fromNameAndUniqueId(name, Optional.of(uuid));

        testRoundtrip(factory, profile);
    }

    private void testRoundtrip(ProfileFactoryImpl factory, Profile profile) {
        JsonObject object = profile.getSaveObject();
        Profile newProfile = factory.fromSavedObject(object).get();
        assertEquals(profile, newProfile);
    }

    @Test
    public void testWithoutId() {
        String name = "test";
        ProfileFactoryImpl factory = getProfileFactory();
        Profile profile = factory.fromDisplayText(name);

        assertEquals(name, profile.getDisplayName());
        assertFalse(((PlayerProfile) profile).getUniqueId().isPresent());
    }

    @Test
    public void testWithoutIdJson() {
        String name = "test";
        ProfileFactoryImpl factory = getProfileFactory();
        Profile profile = factory.fromDisplayText(name);
        JsonObject object = profile.getSaveObject();

        assertEquals(name, object.get(PlayerProfileImpl.NAME_KEY).getAsString());
        assertFalse(object.has(PlayerProfileImpl.UUID_KEY));
    }
}
