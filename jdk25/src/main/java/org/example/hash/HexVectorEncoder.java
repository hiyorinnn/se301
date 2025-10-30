package org.example.hash;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

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

        byte[] out = new byte[input.length * 2];

        int i = 0;
        int upperBound = input.length - (input.length % SPECIES.length());
        while (i < upperBound) {
            ByteVector v = ByteVector.fromArray(SPECIES, input, i);

            ByteVector hi = v.lanewise(VectorOperators.LSHR, 4).and((byte) 0x0F);
            ByteVector lo = v.and((byte) 0x0F);

            hi = mapNibbleToHexAscii(hi);
            lo = mapNibbleToHexAscii(lo);

            byte[] hiArr = new byte[SPECIES.length()];
            byte[] loArr = new byte[SPECIES.length()];
            hi.intoArray(hiArr, 0);
            lo.intoArray(loArr, 0);

            int outPos = i * 2;
            for (int k = 0; k < SPECIES.length(); k++) {
                out[outPos++] = hiArr[k];
                out[outPos++] = loArr[k];
            }

            i += SPECIES.length();
        }

        if (i < input.length) {
            int remaining = input.length - i;
            VectorMask<Byte> m = VectorMask.fromLong(SPECIES, (1L << remaining) - 1);
            ByteVector v = ByteVector.fromArray(SPECIES, input, i, m);
            ByteVector hi = v.lanewise(VectorOperators.LSHR, 4).and((byte) 0x0F);
            ByteVector lo = v.and((byte) 0x0F);
            hi = mapNibbleToHexAscii(hi);
            lo = mapNibbleToHexAscii(lo);

            byte[] hiArr = new byte[SPECIES.length()];
            byte[] loArr = new byte[SPECIES.length()];
            hi.intoArray(hiArr, 0);
            lo.intoArray(loArr, 0);

            int outPos = i * 2;
            for (int k = 0; k < remaining; k++) {
                out[outPos++] = hiArr[k];
                out[outPos++] = loArr[k];
            }
        }

        return new String(out, StandardCharsets.US_ASCII);
    }

    private static ByteVector mapNibbleToHexAscii(ByteVector nibble) {
        VectorMask<Byte> isDigit = nibble.compare(VectorOperators.LT, (byte) 10);
        ByteVector addForDigits = ByteVector.broadcast(SPECIES, (byte) '0');
        ByteVector addForLetters = ByteVector.broadcast(SPECIES, (byte) ('a' - 10));
        ByteVector add = addForDigits.blend(addForLetters, isDigit.not());
        return nibble.add(add);
    }
}
