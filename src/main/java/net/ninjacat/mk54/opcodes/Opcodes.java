package net.ninjacat.mk54.opcodes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import net.ninjacat.mk54.exceptions.RuntimeIOException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Opcodes {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_COMMANDS = 105;

    private final Map<String, String> keyToOpcodeMap;


    public Opcodes() {

        try (InputStream is = getClass().getResourceAsStream("/opcodes.json")) {
            final List<Opcode> opcodes = MAPPER.readValue(is, MAPPER.getTypeFactory().constructCollectionType(List.class, Opcode.class));


            // no use of .collect() because of https://bugs.openjdk.java.net/browse/JDK-8040892
            this.keyToOpcodeMap = new HashMap<>();
            Streams.concat(
                    opcodes.stream()
                            .flatMap(oc -> oc.keys.stream()
                                    .map(Utils::normalizeKey)
                                    .distinct()
                                    .map(k -> new StringPair(k, oc.code))),
                    addressCodes())
                    .forEach(sp -> this.keyToOpcodeMap.put(sp.getA(), sp.getB()));

        } catch (final IOException e) {
            throw new RuntimeIOException(e);
        }

    }

    /**
     * Generates stream of address codes. Numbers from 00 to 99 in mnemonics will be copied verbatim to output hex file
     *
     * @return Stream of key->code pairs of program space addresses
     */
    // TODO: Check how to address beyond 99
    private static Stream<StringPair> addressCodes() {
        return IntStream.range(0, MAX_COMMANDS)
                .mapToObj(addr -> String.format("%02d", addr))
                .map(addr -> new StringPair(addr, addr));
    }

    /**
     * Finds opcode based on mnemonic
     *
     * @param key Key mnemonic
     * @return Hex code for the command wrapped in {@link Optional} or {@link Optional#empty()}
     */
    public Optional<String> findOpcode(final String key) {
        final String normalizedKey = Utils.normalizeKey(key);
        return Optional.ofNullable(this.keyToOpcodeMap.getOrDefault(normalizedKey, null));
    }

    private static final class Opcode {
        final String code;
        final List<String> keys;
        final String desc;

        @JsonCreator
        private Opcode(@JsonProperty("code") final String code,
                       @JsonProperty("keys") final List<String> keys,
                       @JsonProperty("desc") final String desc) {
            this.code = code;
            this.keys = keys;
            this.desc = desc;
        }

        public String getCode() {
            return this.code;
        }

        public List<String> getKeys() {
            return this.keys;
        }

        public String getDesc() {
            return this.desc;
        }
    }

    private static final class StringPair {
        private final String a;
        private final String b;

        StringPair(final String a, final String b) {
            this.a = a;
            this.b = b;
        }

        String getA() {
            return this.a;
        }

        String getB() {
            return this.b;
        }
    }
}
