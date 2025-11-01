package org.example.hash;

// JDK 25: 10th incubator Vector API with MemorySegment and VectorShuffle support (JEP 508)
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

final class HexVectorEncoder {

    private static final VectorSpecies<Byte> SPECIES = ByteVector.SPECIES_PREFERRED;

    private HexVectorEncoder() {}

    static String encodeToHex(byte[] input) {
        if (input == null) {
            throw new NullPointerException("input");
        }
        if (input.length < SPECIES.length()) {
            return HexFormat.of().formatHex(input);
        }


        try (Arena arena = Arena.ofConfined()) {
            MemorySegment outputSegment = arena.allocate(input.length * 2);
            
            MemorySegment inputSegment = MemorySegment.ofArray(input);

            int i = 0;
            int upperBound = input.length - (input.length % SPECIES.length());
            
            while (i < upperBound) {
                ByteVector v = ByteVector.fromMemorySegment(
                    SPECIES, 
                    inputSegment, 
                    i, 
                    ByteOrder.nativeOrder()
                );

                ByteVector hi = v.lanewise(VectorOperators.LSHR, 4).and((byte) 0x0F);
                ByteVector lo = v.and((byte) 0x0F);

                hi = mapNibbleToHexAscii(hi);
                lo = mapNibbleToHexAscii(lo);

                writeInterleavedToMemorySegment(hi, lo, outputSegment, i);

                i += SPECIES.length();
            }

            if (i < input.length) {
                int remaining = input.length - i;
                VectorMask<Byte> m = VectorMask.fromLong(SPECIES, (1L << remaining) - 1);
                
                ByteVector v = ByteVector.fromMemorySegment(
                    SPECIES, 
                    inputSegment, 
                    i, 
                    ByteOrder.nativeOrder(), 
                    m
                );
                
                ByteVector hi = v.lanewise(VectorOperators.LSHR, 4).and((byte) 0x0F);
                ByteVector lo = v.and((byte) 0x0F);
                hi = mapNibbleToHexAscii(hi);
                lo = mapNibbleToHexAscii(lo);

                byte[] hiArr = new byte[remaining];
                byte[] loArr = new byte[remaining];
                hi.intoArray(hiArr, 0, m);
                lo.intoArray(loArr, 0, m);

                MemorySegment remainingSegment = outputSegment.asSlice(i * 2);
                var byteLayout = java.lang.foreign.ValueLayout.JAVA_BYTE;
                for (int k = 0; k < remaining; k++) {
                    remainingSegment.set(byteLayout, k * 2, hiArr[k]);
                    remainingSegment.set(byteLayout, k * 2 + 1, loArr[k]);
                }
            }

            byte[] out = new byte[input.length * 2];
            MemorySegment.copy(outputSegment, 0, MemorySegment.ofArray(out), 0, input.length * 2);
            return new String(out, StandardCharsets.US_ASCII);
        }
    }

    private static void writeInterleavedToMemorySegment(
            ByteVector hi, ByteVector lo, MemorySegment outputSegment, int offset) {
        int laneSize = SPECIES.length();
        
        try (Arena tempArena = Arena.ofConfined()) {

            MemorySegment tempSegment = tempArena.allocate(laneSize * 2);
            
            hi.intoMemorySegment(tempSegment, 0, ByteOrder.nativeOrder());
            lo.intoMemorySegment(tempSegment, laneSize, ByteOrder.nativeOrder());
            
            int[] shuffleIndices = new int[laneSize * 2];
            for (int i = 0; i < laneSize; i++) {
                shuffleIndices[i * 2] = i;
                shuffleIndices[i * 2 + 1] = i + laneSize;
            }
            
            MemorySegment shuffleSegment = tempArena.allocate(shuffleIndices.length * Integer.BYTES);
            for (int i = 0; i < shuffleIndices.length; i++) {
                shuffleSegment.setAtIndex(java.lang.foreign.ValueLayout.JAVA_INT, i, shuffleIndices[i]);
            }

            VectorShuffle<Byte> interleaveShuffle = VectorShuffle.fromArray(
                SPECIES, 
                shuffleIndices, 
                0
            );

            byte[] hiArr = new byte[laneSize];
            byte[] loArr = new byte[laneSize];
            hi.intoArray(hiArr, 0);
            lo.intoArray(loArr, 0);

            byte[] combined = new byte[laneSize * 2];
            System.arraycopy(hiArr, 0, combined, 0, laneSize);
            System.arraycopy(loArr, 0, combined, laneSize, laneSize);

            MemorySegment combinedSegment = MemorySegment.ofArray(combined);
            ByteVector combinedVector = ByteVector.fromMemorySegment(
                SPECIES,
                combinedSegment,
                0,
                ByteOrder.nativeOrder()
            );
            
            ByteVector shuffled = combinedVector.rearrange(interleaveShuffle);

            shuffled.intoMemorySegment(outputSegment, offset * 2, ByteOrder.nativeOrder());
        }
    }

    private static ByteVector mapNibbleToHexAscii(ByteVector nibble) {
        VectorMask<Byte> isDigit = nibble.compare(VectorOperators.LT, (byte) 10);
        ByteVector addForDigits = ByteVector.broadcast(SPECIES, (byte) '0');
        ByteVector addForLetters = ByteVector.broadcast(SPECIES, (byte) ('a' - 10));
        ByteVector add = addForDigits.blend(addForLetters, isDigit.not());
        return nibble.add(add);
    }
}
