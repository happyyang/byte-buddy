/*
 * Copyright 2014 - 2019 Rafael Winterhalter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bytebuddy.utility;

import net.bytebuddy.utility.privilege.GetSystemPropertyAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.security.AccessController;

/**
 * A {@link ClassReader} that does not apply a class file version check if the {@code net.bytebuddy.experimental} property is set.
 */
public class OpenedClassReader {

    /**
     * Indicates that Byte Buddy should not validate the maximum supported class file version.
     */
    public static final String EXPERIMENTAL_PROPERTY = "net.bytebuddy.experimental";

    /**
     * {@code true} if Byte Buddy is executed in experimental mode.
     */
    public static final boolean EXPERIMENTAL;

    /**
     * Indicates the ASM API version that is used throughout Byte Buddy.
     */
    public static final int ASM_API;

    /*
     * Checks the experimental property.
     */
    static {
        boolean experimental;
        try {
            experimental = Boolean.parseBoolean(AccessController.doPrivileged(new GetSystemPropertyAction(EXPERIMENTAL_PROPERTY)));
        } catch (Exception ignored) {
            experimental = false;
        }
        EXPERIMENTAL = experimental;
        ASM_API = Opcodes.ASM7;
    }

    /**
     * Not intended for construction.
     */
    private OpenedClassReader() {
        throw new UnsupportedOperationException("This class is a utility class and not supposed to be instantiated");
    }

    /**
     * Creates a class reader for the given binary representation of a class file.
     *
     * @param binaryRepresentation The binary representation of a class file to read.
     * @return An appropriate class reader.
     */
    public static ClassReader of(byte[] binaryRepresentation) {
        if (EXPERIMENTAL) {
            byte[] actualVersion = new byte[]{binaryRepresentation[4], binaryRepresentation[5], binaryRepresentation[6], binaryRepresentation[7]};
            binaryRepresentation[4] = (byte) (Opcodes.V11 >>> 24);
            binaryRepresentation[5] = (byte) (Opcodes.V11 >>> 16);
            binaryRepresentation[6] = (byte) (Opcodes.V11 >>> 8);
            binaryRepresentation[7] = (byte) Opcodes.V11;
            ClassReader classReader = new ClassReader(binaryRepresentation);
            System.arraycopy(actualVersion, 0, binaryRepresentation, 4, actualVersion.length);
            return classReader;
        } else {
            return new ClassReader(binaryRepresentation);
        }
    }
}
