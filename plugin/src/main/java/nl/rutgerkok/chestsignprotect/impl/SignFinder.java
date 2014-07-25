package nl.rutgerkok.chestsignprotect.impl;

import java.util.ArrayList;
import java.util.Collection;

import nl.rutgerkok.chestsignprotect.SignParser;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.material.Attachable;

class SignFinder {
    private static BlockFace[] MAIN_FACES = { BlockFace.NORTH, BlockFace.EAST,
            BlockFace.SOUTH, BlockFace.WEST };

    private SignParser parser;

    SignFinder(SignParser parser) {
        this.parser = parser;
    }

    /**
     * Finds all valid attached signs to a block.
     *
     * @param block
     *            The block to check attached signs on.
     * @return The signs.
     */
    Collection<Sign> findAttachedSigns(Block block) {
        Collection<Sign> signs = new ArrayList<Sign>();
        for (BlockFace face : MAIN_FACES) {
            Block atPosition = block.getRelative(face);
            Material material = atPosition.getType();
            if (material != Material.WALL_SIGN) {
                continue;
            }
            Sign sign = (Sign) atPosition.getState();
            if (!isValidSign(sign, atPosition, block)) {
                continue;
            }
            signs.add(sign);
        }
        return signs;
    }

    /**
     * Gets the parser for signs.
     *
     * @return The parser.
     */
    SignParser getSignParser() {
        return parser;
    }

    /**
     * Checks if the sign at the given position is a valid sign for the
     * container.
     *
     * @param sign
     *            The sign to check.
     * @param signBlock
     *            The block the sign is on ({@link Block#getState()}
     *            {@code .equals(sign)} must return true)
     * @param attachedTo
     *            The block the sign must be attached to. If this is not the
     *            case, the method returns false.
     * @return True if the direction and header of the sign are valid, false
     *         otherwise.
     */
    private boolean isValidSign(Sign sign, Block signBlock, Block attachedTo) {
        BlockFace requiredFace = signBlock.getFace(attachedTo);
        BlockFace actualFace = ((Attachable) sign.getData()).getAttachedFace();
        if (actualFace != requiredFace) {
            return false;
        }
        return parser.hasValidHeader(sign);
    }
}
