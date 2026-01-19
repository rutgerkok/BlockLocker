package nl.rutgerkok.blocklocker;

/**
 * The types of blocks to look out for.
 *
 * <p>A protection consists of three block types:
 *
 * <ul>
 *   <li>Protection blocks, like door or chest blocks.
 *   <li>Blocks supporting the protection, can be of any material. For example, the block under the
 *       door is a supporting block.
 *   <li>Signs.
 * </ul>
 */
public enum SearchMode {
  /** Ignores both {@link #NO_SIGNS signs} and {@link #NO_SUPPORTING_BLOCKS supporting blocks}. */
  MAIN_BLOCKS_ONLY,
  /**
   * Ignores blocks that are solely part of the protection because they are supporting another
   * block.
   */
  NO_SUPPORTING_BLOCKS,
  /** Ignores protection signs. */
  NO_SIGNS,
  /** Includes all blocks (protection blocks, supporting blocks and signs) in the search. */
  ALL;

  /**
   * Gets whether supporting blocks should be searched for. If the block being searched is a
   * supporting block, the protection won't be found if this method returns false.
   *
   * @return True if supporting blocks should be searched for, false otherwise.
   */
  public boolean searchForSupportingBlocks() {
    return this != NO_SUPPORTING_BLOCKS && this != MAIN_BLOCKS_ONLY;
  }

  /**
   * Gets whether signs should be searched for. If the block being searched is a sign, the
   * protection won't be found if this method returns false.
   *
   * @return True if supporting blocks should be searched for, false otherwise.
   */
  public boolean searchForSigns() {
    return this != NO_SIGNS && this != MAIN_BLOCKS_ONLY;
  }
}
