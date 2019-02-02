/*
 * Copyright © 2019 Peter Doornbosch
 *
 * This file is part of Kwik, a QUIC client Java library
 *
 * Kwik is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Kwik is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.luminis.quic;

import java.nio.ByteBuffer;

public class HandshakePacket extends LongHeaderPacket {

    public HandshakePacket(Version quicVersion) {
        super(quicVersion);
    }

    public HandshakePacket(Version quicVersion, byte[] sourceConnectionId, byte[] destConnectionId, int packetNumber, QuicFrame payload, ConnectionSecrets connectionSecrets) {
        super(quicVersion, sourceConnectionId, destConnectionId, packetNumber, payload, connectionSecrets);
    }

    protected byte getPacketType() {
        if (quicVersion.atLeast(Version.IETF_draft_17)) {
            // https://tools.ietf.org/html/draft-ietf-quic-transport-17#section-17.2
            // "|1|1|T T|R R|P P|"
            // "|  0x2 | Handshake       | Section 17.6 |"
            // https://tools.ietf.org/html/draft-ietf-quic-transport-17#section-17.2
            // "The next two bits (those with a mask of 0x0c) of
            //      byte 0 are reserved.  These bits are protected using header
            //      protection (see Section 5.4 of [QUIC-TLS]).  The value included
            //      prior to protection MUST be set to 0."
            byte flags = (byte) 0xe0;  // 1110 0000
            return encodePacketNumberLength(flags, packetNumber);
        }
        else {
            return (byte) 0xfd;
        }
    }

    @Override
    protected void generateAdditionalFields() {
    }

    @Override
    protected EncryptionLevel getEncryptionLevel() {
        return EncryptionLevel.Handshake;
    }

    @Override
    public void accept(PacketProcessor processor) {
        processor.process(this);
    }

    @Override
    protected void checkPacketType(byte type) {
        if (quicVersion.atLeast(Version.IETF_draft_17)) {
            byte masked = (byte) (type & 0xf0);
            if (masked != (byte) 0xe0) {
                // Programming error: this method shouldn't have been called if packet is not Initial
                throw new RuntimeException();
            }
        }
        else {
            if (type != (byte) 0xfd) {
                // Programming error: this method shouldn't have been called if packet is not Initial
                throw new RuntimeException();
            }
        }
    }

    @Override
    protected void parseAdditionalFields(ByteBuffer buffer) {
    }


}