package io.github.laptop59.concocti.common.multiblock;

import io.github.laptop59.concocti.common.block.ConcoctiBlocks;
import io.github.laptop59.concocti.common.block.HatchPurpose;
import io.github.laptop59.concocti.common.block.HatchType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/** A data structure that helps to test for a correct multiblock structure. */
// Note: the ranges are like [start, end)
public record MultiblockStructure(
    int xStart,
    int yStart,
    int zStart,
    int xEnd,
    int yEnd,
    int zEnd,
    int xLength,
    int yLength,
    int zLength,
    MultiblockBlockPredicate[] predicates
) {
    /** Creates a new {@link MultiblockStructure} from a builder consumer. */
    public static MultiblockStructure from(Consumer<Builder> builderConsumer) {
        return from(Builder.create(), builderConsumer);
    }

    /** Creates a new {@link MultiblockStructure} from a builder consumer. The builder provided is used. */
    public static MultiblockStructure from(Builder builder, Consumer<Builder> builderConsumer) {
        builderConsumer.accept(builder);
        return builder.build();
    }

    /**
     * Gets the predicate at a position.
     * @param pos The RELATIVE position to query
     * @return The predicate at the position, or {@code null} if non-existent.
     */
    public @Nullable MultiblockBlockPredicate at(BlockPos pos) {
        return at(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Gets the predicate at a position.
     * @param x The RELATIVE x-coordinate of the predicate to search for.
     * @param y The RELATIVE y-coordinate of the predicate to search for.
     * @param z The RELATIVE z-coordinate of the predicate to search for.
     * @return The predicate at the position, or {@code null} if non-existent.
     */
    public @Nullable MultiblockBlockPredicate at(int x, int y, int z) {
        assertNotOutOfBounds(x, y, z);
        int unsignedX = x - xStart;
        int unsignedY = y - yStart;
        int unsignedZ = z - zStart;
        return predicates[unsignedX + unsignedY * xLength + unsignedZ * xLength * yLength];
    }

    /**
     * Returns whether a predicate is satisfied at a world's position.
     * @param level The current level (world).
     * @param blockPos The ABSOLUTE block position to test for.
     * @param x The RELATIVE x-coordinate of the predicate to qualify for.
     * @param y The RELATIVE y-coordinate of the predicate to qualify for.
     * @param z The RELATIVE z-coordinate of the predicate to qualify for.
     * @param direction The direction of the structure to qualify for.
     * @return Whether the predicate is satisfied or is {@code null}.
     */
    public boolean matches(Level level, BlockPos blockPos, int x, int y, int z, Direction direction) {
        return result(level, blockPos, x, y, z, direction) == null;
    }

    /**
     * Returns the result of a predicate is satisfied at a world's position.
     * @param level The current level (world).
     * @param blockPos The ABSOLUTE block position to test for.
     * @param x The RELATIVE x-coordinate of the predicate to qualify for.
     * @param y The RELATIVE y-coordinate of the predicate to qualify for.
     * @param z The RELATIVE z-coordinate of the predicate to qualify for.
     * @param direction The direction of the structure to qualify for.
     * @return The result of the predicate.
     */
    public MultiblockResult result(Level level, BlockPos blockPos, int x, int y, int z, Direction direction) {
        MultiblockBlockPredicate predicate = at(x, y, z);
        return predicate == null ? null : predicate.getResult(level, blockPos, direction);
    }

    /**
     * Returns whether the multiblock structure at an origin is COMPLETELY satisfied.
     * @param level The current level (world).
     * @param origin The origin to use for querying.
     * @param direction The direction of the structure.
     * @return Whether ALL the existent predicates are satisfied.
     */
    public boolean match(Level level, BlockPos origin, Direction direction) {
        for (int x = xStart; x < xEnd; x++)
            for (int y = yStart; y < yEnd; y++)
                for (int z = zStart; z < zEnd; z++) {
                    BlockPos rotatedRelativePos = rotateAccordingToNorth(new BlockPos(x, y, z), direction);
                    BlockPos absolutePos = origin.offset(rotatedRelativePos);
                    MultiblockResult result = result(level, absolutePos, x, y, z, direction);
                    if (result != null) return false;
                }
        return true;
    }

    /**
     * Gets the unmatched positions and their results in a structure.
     * @param level The current level (world).
     * @param origin The origin to use for querying.
     * @param direction The direction of the structure.
     * @return The unmatched entries aforementioned <strong>whose keys are ABSOLUTE</strong>, or none if completely satisfied. It is guaranteed that all {@link BlockState}s returned are not {@code null}.
     */
    public Map<BlockPos, MultiblockResult> getUnmatched(Level level, BlockPos origin, Direction direction) {
        HashMap<BlockPos, MultiblockResult> resultHashMap = new HashMap<>();
        for (int x = xStart; x < xEnd; x++)
            for (int y = yStart; y < yEnd; y++)
                for (int z = zStart; z < zEnd; z++) {
                    BlockPos rotatedRelativePos = rotateAccordingToNorth(new BlockPos(x, y, z), direction);
                    BlockPos absolutePos = origin.offset(rotatedRelativePos);
                    MultiblockResult result = result(level, absolutePos, x, y, z, direction);
                    if (result == null) continue;
                    resultHashMap.put(absolutePos, result);
                }
        return resultHashMap;
    }

    public static BlockPos rotateAccordingToNorth(BlockPos relativePos, Direction direction) {
        // Direction |  x |  z |  AAAA       z -2
        //           |    |    |  A          | -1   N
        // NORTH     | +x | -z |  A          v  0   |
        // SOUTH     | -x | +z |  A     x--->       v
        // EAST      | +z | +x |        0 1 2
        // WEST      | -z | -x |
        BlockPos flippedPos = new BlockPos(relativePos.getX(), relativePos.getY(), -relativePos.getZ());
        return switch (direction) {
            case NORTH -> flippedPos;
            case SOUTH -> flippedPos.rotate(Rotation.CLOCKWISE_180);
            case EAST -> flippedPos.rotate(Rotation.CLOCKWISE_90);
            case WEST -> flippedPos.rotate(Rotation.COUNTERCLOCKWISE_90);
            default -> throw new IllegalStateException("Unexpected direction: " + direction);
        };
    }

    /**
     * Gets the positions having extra data in a structure.
     * @param level The current level (world).
     * @param origin The origin to use for querying.
     * @param direction The direction of the structure.
     * @return The entries aforementioned <strong>whose keys are ABSOLUTE</strong>.
     */
    public Map<BlockPos, Object> getExtraData(Level level, BlockPos origin, Direction direction) {
        HashMap<BlockPos, Object> resultHashMap = new HashMap<>();
        for (int x = xStart; x < xEnd; x++)
            for (int y = yStart; y < yEnd; y++)
                for (int z = zStart; z < zEnd; z++) {
                    BlockPos rotatedRelativePos = rotateAccordingToNorth(new BlockPos(x, y, z), direction);
                    BlockPos absolutePos = origin.offset(rotatedRelativePos);
                    MultiblockBlockPredicate predicate = at(x, y, z);
                    if (predicate == null) continue;
                    Object data = predicate.getExtraData(level, absolutePos, direction);
                    if (data == null) continue;
                    resultHashMap.put(absolutePos, data);
                }
        return resultHashMap;
    }

    private void assertNotOutOfBounds(int x, int y, int z) {
        if (x < xStart || x >= xEnd) throw new IllegalArgumentException("X-position provided (" + x + ") was not within the range [" + xStart + "," + xEnd + ")");
        if (y < yStart || y >= yEnd) throw new IllegalArgumentException("Y-position provided (" + y + ") was not within the range [" + yStart + "," + yEnd + ")");
        if (z < zStart || z >= zEnd) throw new IllegalArgumentException("Z-position provided (" + z + ") was not within the range [" + zStart + "," + zEnd + ")");
    }

    public static class Builder {
        int xStart = 0;
        int yStart = 0;
        int zStart = 0;
        int xEnd = 0;
        int yEnd = 0;
        int zEnd = 0;
        HashMap<BlockPos, MultiblockBlockPredicate> predicatesMap = new HashMap<>();

        protected Builder() {}
        protected Builder(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd) {
            assert xStart <= xEnd : "xStart (" + xStart + ") was greater than xEnd (" + xEnd + ")!";
            assert yStart <= yEnd : "yStart (" + yStart + ") was greater than yEnd (" + yEnd + ")!";
            assert zStart <= zEnd : "zStart (" + zStart + ") was greater than zEnd (" + zEnd + ")!";
            this.xStart = xStart;
            this.yStart = yStart;
            this.zStart = zStart;
            this.xEnd = xEnd;
            this.yEnd = yEnd;
            this.zEnd = zEnd;
        }

        /** Creates a new builder of 0x0x0 */
        public static Builder create() {
            return new Builder();
        }

        /**
         * Creates a new builder of the provided dimension.
         * The values of start <strong>must not be less than</strong> the values of end.
         * Start is inclusive, while End is exclusive.
         * The values provided are such that the coordinates in this builder are the following:
         * <p>
         * If (x, y, z) is a possible RELATIVE coordinate of a block in this structure where the machine is the origin, then all the mathematical conditions listed below hold true:
         * <p>
         * x ∈ [{@code xStart}, {@code xEnd}), or {@code xStart} ≤ x < {@code xEnd}.
         * <p>
         * y ∈ [{@code yStart}, {@code yEnd}), or {@code yStart} ≤ y < {@code yEnd}.
         * <p>
         * z ∈ [{@code zStart}, {@code zEnd}), or {@code zStart} ≤ z < {@code zEnd}.
         * <p>
         * Another mathematical way to express this, if B is a set containing all possible points in this structure, is:
         * B = {(x, y, z) ∈ ℤ<sup>3</sup> | x ∈ [{@code xStart}, {@code xEnd}),  y ∈ [{@code yStart}, {@code yEnd}), z ∈ [{@code zStart}, {@code zEnd})}
         */
        public static Builder create(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd) {
            return new Builder(xStart, yStart, zStart, xEnd, yEnd, zEnd);
        }

        /**
         * Set a predicate into a block position. <strong>No more than 1 predicate may be in a particular position.</strong>
         * @param blockPos The block position to put the predicate in.
         * @param predicate The predicate to put.
         */
        public void set(BlockPos blockPos, @Nullable MultiblockBlockPredicate predicate) {
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();
            if (x < xStart) xStart = x;
            if (x >= xEnd) xEnd = x + 1;
            if (y < yStart) yStart = y;
            if (y >= yEnd) yEnd = y + 1;
            if (z < zStart) zStart = z;
            if (z >= zEnd) zEnd = z + 1;
            predicatesMap.put(blockPos, predicate);
        }

        /**
         * Loads predicates based on multiple 2D maps and legend. Make sure to use the {@link Builder#create(int, int, int, int, int, int)} method to create the builder before using this method!
         * @param legend The legend to use for putting predicates.
         * @param schematic The schematic to use, multiple 2D maps which represents a 3D map. This is hard to explain, so look at the following example.
         * Your IDE may add a <strong>...schematic:</strong> label which will offset your first entry. In this case, you can add {@code null}s to relieve this problem.
         * Here's an example on specifying a structure:
         * <pre>
         * {@code
         *      Builder builder = MultiblockStructure.Builder.create(-1, -1, -2, 1, 1, 0);
         *      MultiblockSimpleBlockPredicate brickPredicate =
         *          new MultiblockSimpleBlockPredicate(ConcoctiBlocks.TOUGH_CONCOCTI_BRICKS);
         *      MultiblockToughConcoctiBrickLikePredicate brickLikePredicate =
         *          new MultiblockToughConcoctiBrickLikePredicate(
         *              ConcoctiBlocks.getDeferredHatch(HatchPurpose.INPUT, HatchType.ITEM),
         *              ConcoctiBlocks.getDeferredHatch(HatchPurpose.OUTPUT, HatchType.FLUID)
         *          );
         *      MultiblockSimpleBlockPredicate airPredicate =
         *          new MultiblockBlockTagPredicate(BlockTags.AIR);
         *      MultiHorizontalDirectionalBlockPredicate controllerPredicate =
         *          new MultiHorizontalDirectionalBlockPredicate(...);
         *      builder.load(
         *          Map.of(
         *              'b', brickPredicate,
         *              'B', brickLikePredicate,
         *              'A', airPredicate,
         *              'C', controllerPredicate
         *          ),
         *          // Empty spaces represent a null predicate, which are always satisfied.
         *          // If you want AIR-only ones instead, specify a letter mapping to an AIR-only predicate. Shown here:
         *          //
         *          // Spaces between the layers are non-functional and are only there for parsing & clarity.
         *          // However they must be left.
         *          null, // IntelliJ draws a label before our first string literal which messes up our offset, so we pass it to this null which does nothing.
         *          " b  bbb  b ", // +x --->
         *          "bBb bAb bbb", //        |
         *          " b  bCb  b "  //        |
         *          // y   y   y          +z v
         *          // =   =   =
         *          // -1  0   1        ----> +y
         *      );
         *      MultiblockStructure structure = builder.build();
         * }
         * </pre>
         */
        public void load(Map<Character, MultiblockBlockPredicate> legend, String... schematic) {
            // Removing nulls from the array:
            int actualEntries = schematic.length;
            for (String string : schematic) if (string == null) actualEntries--;
            // Now put the non-nulls in an array.
            String[] newSchematic = new String[actualEntries];
            int i = 0;
            for (String string : schematic)
                if (string != null) {
                    newSchematic[i++] = string;
                }
            schematic = newSchematic;
            //
            assert schematic.length == (zEnd - zStart) && schematic.length > 0;
            assert schematic[0].length() == (xEnd - xStart) * (yEnd - yStart) + (yEnd - yStart - 1);
            // Capital variables are used for relative pos. (xStart -> xEnd - 1)
            // Lowercase ones are used for indexing (0 -> xEnd - xStart - 1)
            for (int Z = zStart, z = 0; Z < zEnd; Z++, z++) {
                String schematicLine = schematic[z];
                for (int Y = yStart, y = 0; Y < yEnd; Y++, y++) {
                    int startingX = y * (xEnd - xStart + 1);
                    for (int X = xStart, x = 0; X < xEnd; X++, x++) {
                        int charIndex = startingX + x;
                        char symbol = schematicLine.charAt(charIndex);
                        MultiblockBlockPredicate predicate = legend.get(symbol);
                        BlockPos blockPos = new BlockPos(X, Y, Z);
                        // System.out.println(x + "," + y + "," + z + " -> " + blockPos + " -> " + symbol);
                        set(blockPos, predicate);
                    }
                }
            }
        }

        /** Build a {@link MultiblockStructure} from the data gathered by this builder. */
        public MultiblockStructure build() {
            // Get the length of all the blocks.
            int xLength = xEnd - xStart;
            int yLength = yEnd - yStart;
            int zLength = zEnd - zStart;
            int size = xLength * yLength * zLength;
            MultiblockBlockPredicate[] predicates = new MultiblockBlockPredicate[size];
            for (var entry : predicatesMap.entrySet()) {
                BlockPos blockPos = entry.getKey();
                int x = blockPos.getX() - xStart;
                int y = blockPos.getY() - yStart;
                int z = blockPos.getZ() - zStart;
                int i = x + y * xLength + z * xLength * yLength;
                predicates[i] = entry.getValue();
            }
            return new MultiblockStructure(xStart, yStart, zStart, xEnd, yEnd, zEnd, xLength, yLength, zLength, predicates);
        }
    }
}
