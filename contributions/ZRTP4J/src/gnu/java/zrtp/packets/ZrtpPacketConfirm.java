/**
 * Copyright (C) 2006-2008 Werner Dittmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors: Werner Dittmann <Werner.Dittmann@t-online.de>
 */

package gnu.java.zrtp.packets;

import gnu.java.zrtp.ZrtpConstants;
import gnu.java.zrtp.utils.ZrtpUtils;


/**
 * @author Werner Dittmann <Werner.Dittmann@t-online.de>
 *
 */
public class ZrtpPacketConfirm extends ZrtpPacketBase {
    
    /*
     * The number of the Confirm specific ZRTP part in words
     */
    private static final int ZRTP_CONFIRM_FIXED_LENGTH = 16;
    
    
    private static final int HMAC_OFFSET = ZRTP_HEADER_LENGTH * ZRTP_WORD_SIZE;         // [2*ZRTP_WORD_SIZE];
    private static final int IV_OFFSET = HMAC_OFFSET + 2*ZRTP_WORD_SIZE;                // [4*ZRTP_WORD_SIZE];
    private static final int HASH_H0_OFFSET = IV_OFFSET + 4*ZRTP_WORD_SIZE;             // [8*ZRTP_WORD_SIZE];
    private static final int FILLER_OFFSET = HASH_H0_OFFSET + 8*ZRTP_WORD_SIZE;         // [2];
    private static final int SIG_LENGTH_OFFSET = FILLER_OFFSET + 2;
    private static final int FLAGS_OFFSET = SIG_LENGTH_OFFSET + 1;
    private static final int EXP_TIME_OFFSET = FLAGS_OFFSET + 1;
    private static final int SIG_DATA_OFFSET = EXP_TIME_OFFSET + ZRTP_WORD_SIZE;

    // required bytes to hold the header, the fix part and the CRC
    private static final int CONFIRM_FIXED_LENGTH = 
        (ZRTP_HEADER_LENGTH + ZRTP_CONFIRM_FIXED_LENGTH) * ZRTP_WORD_SIZE + CRC_SIZE;
    
    private int signatureLength;

    public ZrtpPacketConfirm() {
        super(null);
        setSignatureLength(0);
    }

    public ZrtpPacketConfirm(int sl) {
        super(null);
        setSignatureLength(sl);
    }

    public void setSignatureLength(int sl) {
        if (sl > 255) {
            return;                     // TODO throw exception here ?
        }
        signatureLength = sl;
        // compute total length inlcuding space for signature and CRC
        int length =  CONFIRM_FIXED_LENGTH + (sl * ZRTP_WORD_SIZE);
        
        if (packetBuffer == null) {
            packetBuffer = new byte[length];
        }
        else {
            // allocate new buffer, maybe shorter
            byte[] tmp = new byte[length];
            // copy header data to new place
            System.arraycopy(packetBuffer, 0, tmp, 0, (ZRTP_HEADER_LENGTH + ZRTP_CONFIRM_FIXED_LENGTH) * ZRTP_WORD_SIZE);
            packetBuffer = tmp;
        }
        packetBuffer[SIG_LENGTH_OFFSET] = (byte)sl;
        setLength((length-CRC_SIZE) / 4);
        setZrtpId();
    }

    public ZrtpPacketConfirm(byte[] data) {
        super(data);
        signatureLength = packetBuffer[SIG_LENGTH_OFFSET] & 0xff;
    }

    
    public boolean isSASFlag() {
        return ((packetBuffer[FLAGS_OFFSET] & 0x4) == 0x4); 
    }
    
    public byte[] getIv() {
        return ZrtpUtils.readRegion(packetBuffer, IV_OFFSET, 4*ZRTP_WORD_SIZE);
    }
        
    public byte[] getHmac() {
        return ZrtpUtils.readRegion(packetBuffer, HMAC_OFFSET, 2*ZRTP_WORD_SIZE);
    }
        
    public int getExpTime() {
        return ZrtpUtils.readInt(packetBuffer, EXP_TIME_OFFSET);
    }

    public byte[] getDataToSecure() {
        // 9 is ZRTP_HEADER plus non secure confirm data       
        int length = (getLength() - 9) * ZRTP_WORD_SIZE;
        return ZrtpUtils.readRegion(packetBuffer, HASH_H0_OFFSET, length);
    }
    
    public byte[] getHashH0() { 
        return ZrtpUtils.readRegion(packetBuffer, HASH_H0_OFFSET, 8*ZRTP_WORD_SIZE);
    }

    public byte[] getSignatureData() {
        return ZrtpUtils.readRegion(packetBuffer, SIG_DATA_OFFSET, signatureLength);
    }
    
    public int getSignatureLength() {
        return signatureLength;
    }
    /*
     * Setter methods
     */
    public void setSASFlag() {
        packetBuffer[FLAGS_OFFSET] |= 0x4; 
    }

    public void setHmac(byte[] data) {
        System.arraycopy(data, 0, packetBuffer, HMAC_OFFSET, 2*ZRTP_WORD_SIZE);
    }

    public void setIv(byte[] data) {
        System.arraycopy(data, 0, packetBuffer, IV_OFFSET, 4*ZRTP_WORD_SIZE);
    }
        
    public void setExpTime(int t)  { 
        ZrtpUtils.int32ToArrayInPlace(t, packetBuffer, EXP_TIME_OFFSET);
    }
    
    public void setDataToSecure(byte[] data) {
        // 9 is ZRTP_HEADER plus non secure confirm data       
        int length = (getLength() - 9) * ZRTP_WORD_SIZE;
        System.arraycopy(data, 0, packetBuffer, HASH_H0_OFFSET, length);
    }

    public void setHashH0(byte[] data) {
        System.arraycopy(data, 0, packetBuffer, HASH_H0_OFFSET, 8*ZRTP_WORD_SIZE);
    }

    public void setSignatureData(byte[] data) {
        if (data.length > signatureLength) {
            return;                                 // TODO throw exception here?
        }
        System.arraycopy(data, 0, packetBuffer, SIG_DATA_OFFSET, data.length);       
    }
    
    public static void main(String[] args) {
        ZrtpPacketConfirm pkt = new ZrtpPacketConfirm(0);
        System.err.println("Confirm length: " + pkt.getLength());
        pkt.setMessageType(ZrtpConstants.Confirm1Msg);
        pkt.setSASFlag();
        
        System.err.println("packetBuffer length in bytes: " + pkt.getHeaderBase().length);
        ZrtpUtils.hexdump("Confirm packet", pkt.getHeaderBase(), pkt.getHeaderBase().length);

        pkt = new ZrtpPacketConfirm();
        System.err.println("Confirm length: " + pkt.getLength());
        pkt.setMessageType(ZrtpConstants.Confirm1Msg);
        pkt.setSignatureLength(150);
        pkt.setSASFlag();
        
        System.err.println("packetBuffer length in bytes: " + pkt.getHeaderBase().length);
        System.err.println("Signature length in words: " + pkt.getSignatureLength());
        ZrtpUtils.hexdump("Confirm packet", pkt.getHeaderBase(), pkt.getHeaderBase().length);
    
    }
}
