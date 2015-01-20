package nl.rutgerkok.blocklocker.impl.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import nl.rutgerkok.blocklocker.ProfileFactory;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.group.CombinedGroupSystem;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.profile.Profile;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.base.Optional;

@RunWith(JUnit4.class)
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

        assertTrue("Same id", bob.includes(bobRenamed));
        assertTrue("Same id", bobRenamed.includes(bob));
        assertFalse("Known id, not present in other",
                jane.includes(janeWithoutId));
        assertTrue("Unknown id, same name", janeWithoutId.includes(jane));
        assertFalse("Different id and name", bob.includes(jane));
        assertFalse("Different id and name", bob.includes(janeWithoutId));

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
        JSONObject object = profile.getSaveObject();

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
        JSONObject object = profile.getSaveObject();
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
        JSONObject object = profile.getSaveObject();

        assertEquals(name, object.get(PlayerProfileImpl.NAME_KEY));
        assertFalse(object.containsKey(PlayerProfileImpl.UUID_KEY));
    }
}
