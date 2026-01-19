package nl.rutgerkok.blocklocker.impl.profile;

import com.google.common.base.Preconditions;
import java.util.Date;
import nl.rutgerkok.blocklocker.SecretSignEntry;
import nl.rutgerkok.blocklocker.profile.Profile;

class RedstoneProfileImpl implements Profile {

  static final String REDSTONE_KEY = "r";

  private final String translatedTag;

  /**
   * Creates a new [Redstone]-profile.
   *
   * @param translatedTag Usually "Redstone", may be localized.
   */
  RedstoneProfileImpl(String translatedTag) {
    this.translatedTag = translatedTag;
  }

  /** All instances of this object are equal. */
  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    return getClass() == other.getClass();
  }

  @Override
  public String getDisplayName() {
    return '[' + translatedTag + ']';
  }

  @Override
  public void getSaveObject(SecretSignEntry entry) {
    entry.setBoolean(REDSTONE_KEY, true);
  }

  /** All instances of this object are equal. */
  @Override
  public int hashCode() {
    return 4;
  }

  @Override
  public boolean includes(Profile other) {
    Preconditions.checkNotNull(other);
    return other instanceof RedstoneProfileImpl;
  }

  @Override
  public boolean isExpired(Date cutoffDate) {
    // These never expire
    return false;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
