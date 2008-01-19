/*
  Copyright (C) 2006-2007 Werner Dittmann

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#ifndef _ZRTPPACKETCONFIRM_H_
#define _ZRTPPACKETCONFIRM_H_

#include <libzrtpcpp/ZrtpPacketBase.h>

/**
 * Implement the Confirm packet.
 *
 * The ZRTP message Confirm. The implementation sends this
 * to confirm the switch to SRTP (encrypted) mode. The contents of
 * the Confirm message are encrypted, thus the implementation can
 * check if SRTP work correctly.
 *
 * @author Werner Dittmann <Werner.Dittmann@t-online.de>
 */

class ZrtpPacketConfirm : public ZrtpPacketBase {

    private:
        Confirm_t* confirmHeader;

    public:
        ZrtpPacketConfirm();                    /* Creates a Confirm packet */
        ZrtpPacketConfirm(uint8_t sl);		/* Creates a Confirm packet with default data */
        ZrtpPacketConfirm(uint8_t* d);          /* Creates a Confirm packet from received data */
        virtual ~ZrtpPacketConfirm();

        const bool isSASFlag()            { return confirmHeader->flags & 0x4; }
        const uint8_t* getFiller()        { return confirmHeader->filler; }
        const uint8_t* getIv()            { return confirmHeader->iv; }
        const uint8_t* getHmac()          { return confirmHeader->hmac; }
        const uint32_t getExpTime()       { return ntohl(confirmHeader->expTime); }
        uint8_t* getNewH3()               { return confirmHeader->newH3; }

        void setSASFlag()            { confirmHeader->flags |= 0x4; }
        void setHmac(uint8_t* text)  { memcpy(confirmHeader->hmac, text, sizeof(confirmHeader->hmac)); }
        void setIv(uint8_t* text)    { memcpy(confirmHeader->iv, text, sizeof(confirmHeader->iv)); }
        void setExpTime(uint32_t t)  { confirmHeader->expTime = htonl(t); }
        void setNewH3(uint8_t* t)    { memcpy(confirmHeader->newH3, t, sizeof(confirmHeader->newH3)); }
        void setSignatureLength(uint8_t sl);

    private:
        void initialize();
     // Confirm packet is of variable length. It maximum size is 268 words:
     // - 11 words fixed size 
     // - up to 257 words variable part, depending if signature is present ant its length 
     // leads to a maximum of 4*268=1072 bytes.
        uint8_t data[1280];       // large enough to hold a full blown Confirm packet

};

#endif // ZRTPPACKETCONFIRM

